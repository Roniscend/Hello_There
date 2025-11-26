package codes.chrishorner.personasns

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ChatViewModel(private val apiKey: String, private val context: Context) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    private val _typingMessage = MutableStateFlow("")
    val typingMessage: StateFlow<String> = _typingMessage

    // Selected character state
    private val _selectedCharacter = MutableStateFlow(Sender.Ann)
    val selectedCharacter: StateFlow<Sender> = _selectedCharacter

    private val chatHistoryManager = ChatHistoryManager(context)
    private var currentSessionId: String? = null

    private val _sessionResetTrigger = MutableStateFlow(0)
    val sessionResetTrigger: StateFlow<Int> = _sessionResetTrigger

    private val characterNames = listOf("Ann", "Ryuji", "Yusuke")
    private var messageCount = 0

    fun selectCharacter(character: Sender) {
        _selectedCharacter.value = character
        saveCurrentSession()
    }

    fun startNewChat() {
        saveCurrentSession()
        currentSessionId = null
        _messages.value = emptyList()
        _error.value = null
        _isTyping.value = false
        _typingMessage.value = ""
        _selectedCharacter.value = Sender.Ann
        messageCount = 0
        _sessionResetTrigger.value = _sessionResetTrigger.value + 1
    }

    fun loadChatSession(sessionId: String) {
        saveCurrentSession()
        val session = chatHistoryManager.getChatSession(sessionId)
        if (session != null) {
            currentSessionId = sessionId
            _selectedCharacter.value = session.selectedCharacter
            _messages.value = session.messages.map { chatHistoryManager.storedToChatMessage(it) }
            _error.value = null
            _isTyping.value = false
            _typingMessage.value = ""
        }
    }

    fun getChatSessions(): List<ChatSession> {
        return chatHistoryManager.getAllChatSessions()
    }

    fun deleteChatSession(sessionId: String) {
        chatHistoryManager.deleteChatSession(sessionId)
        if (currentSessionId == sessionId) {
            currentSessionId = null
            _messages.value = emptyList()
            _error.value = null
            _isTyping.value = false
            _typingMessage.value = ""
            _selectedCharacter.value = Sender.Ann
            messageCount = 0
            _sessionResetTrigger.value = _sessionResetTrigger.value + 1
        }
    }

    private fun saveCurrentSession() {
        val messages = _messages.value
        if (messages.isNotEmpty()) {
            val storedMessages = messages.map { message ->
                val sender = if (message.role == "assistant") _selectedCharacter.value else null
                chatHistoryManager.chatMessageToStored(message, sender)
            }

            val session = ChatSession(
                id = currentSessionId ?: java.util.UUID.randomUUID().toString(),
                title = chatHistoryManager.generateChatTitle(storedMessages),
                messages = storedMessages,
                selectedCharacter = _selectedCharacter.value
            )

            chatHistoryManager.saveChatSession(session)
            if (currentSessionId == null) {
                currentSessionId = session.id
            }
        }
    }

    fun sendMessage(userText: String) {
        if (_isLoading.value) {
            _error.value = "Please wait for the current response."
            return
        }

        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val nameResponse = getNameResponse(userText)

                if (nameResponse != null) {
                    val currentMessages = _messages.value.toMutableList()
                    currentMessages.add(ChatMessage(role = "user", content = userText))
                    _messages.value = currentMessages

                    _isLoading.value = false
                    startTypingAnimation(nameResponse)
                } else {
                    val characterContext = getCurrentCharacterContext()
                    val contextualPrompt = "$characterContext\n\nUser: $userText"

                    val request = GeminiRequest(
                        contents = listOf(
                            Content(
                                parts = listOf(Part(text = contextualPrompt))
                            )
                        )
                    )

                    val maxRetries = 3
                    var attempt = 0
                    var lastResponse: retrofit2.Response<GeminiResponse>? = null
                    var retryDelay = 1000L

                    while (attempt < maxRetries) {
                        lastResponse = RetrofitClient.api.generateContent(apiKey, request)
                        if (lastResponse.isSuccessful || lastResponse.code() != 429) {
                            break
                        }

                        attempt++
                        if (attempt >= maxRetries) break

                        val retryAfterSeconds = lastResponse.headers()["Retry-After"]?.toLongOrNull()
                        val waitMs = retryAfterSeconds?.times(1000) ?: retryDelay
                        delay(waitMs)
                        retryDelay *= 2
                    }

                    if (lastResponse != null && lastResponse.isSuccessful) {
                        val aiResponse =
                            lastResponse.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                ?: "I'm sorry, I couldn't generate a response."
                        val currentMessages = _messages.value.toMutableList()
                        currentMessages.add(ChatMessage(role = "user", content = userText))
                        _messages.value = currentMessages

                        _isLoading.value = false
                        startTypingAnimation(aiResponse)
                    } else {
                        if (lastResponse?.code() == 429) {
                            _error.value = "Rate limit reached. Please wait a moment and try again."
                        } else {
                            _error.value = "Error ${lastResponse?.code()}: ${lastResponse?.message()}"
                        }
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.localizedMessage ?: "Unknown error"}"
                _isLoading.value = false
            }
        }
    }

    private fun getNameResponse(userText: String): String? {
        val lowerText = userText.lowercase()
        val nameKeywords =
            listOf("name", "who are you", "what's your name", "who is this", "introduce yourself")

        return if (nameKeywords.any { lowerText.contains(it) }) {
            val currentCharacter = getCurrentCharacterName()
            "Hi! My name is $currentCharacter. Nice to meet you!"
        } else {
            null
        }
    }

    private fun getCurrentCharacterName(): String {
        return when (_selectedCharacter.value) {
            Sender.Ann -> "Ann"
            Sender.Ryuji -> "Ryuji"
            Sender.Yusuke -> "Yusuke"
            Sender.Ren -> "Ann"
        }
    }

    fun getCurrentCharacterSender(): Sender {
        return _selectedCharacter.value
    }

    private fun getCurrentCharacterContext(): String {
        return when (_selectedCharacter.value) {
            Sender.Ann -> "You are Ann Takamaki from Persona 5. You're a kind, confident, and fashionable girl who cares deeply about your friends. Respond as Ann would, with her personality and speaking style."
            Sender.Ryuji -> "You are Ryuji Sakamoto from Persona 5. You're energetic, loyal, and sometimes hot-headed but always have your friends' backs. Respond as Ryuji would, with his casual and enthusiastic speaking style."
            Sender.Yusuke -> "You are Yusuke Kitagawa from Persona 5. You're an artistic, eccentric, and thoughtful person who often speaks in a refined manner. Respond as Yusuke would, with his elegant and sometimes dramatic speaking style."
            Sender.Ren -> "You are a helpful AI assistant."
        }
    }

    private suspend fun startTypingAnimation(fullText: String) {
        _isTyping.value = true
        _typingMessage.value = ""
        val totalAnimationTime = 1500L // 1.5 seconds
        val characterCount = fullText.length
        val delayPerCharacter = if (characterCount > 0) totalAnimationTime / characterCount else 50L
        for (i in fullText.indices) {
            _typingMessage.value = fullText.substring(0, i + 1)
            delay(delayPerCharacter)
        }
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(role = "assistant", content = fullText))
        _messages.value = currentMessages

        _isTyping.value = false
        _typingMessage.value = ""
        saveCurrentSession()
        messageCount++
    }

    fun resetConversation() {
        _messages.value = emptyList()
        _error.value = null
        _isTyping.value = false
        _typingMessage.value = ""
        _selectedCharacter.value = Sender.Ann
        messageCount = 0
        currentSessionId = null
        _sessionResetTrigger.value = _sessionResetTrigger.value + 1
    }
}
