package codes.chrishorner.personasns

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<StoredChatMessage>,
    val selectedCharacter: Sender,
    val timestamp: Long = System.currentTimeMillis()
)

data class StoredChatMessage(
    val role: String,
    val content: String,
    val sender: Sender? = null
)

class ChatHistoryManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveChatSession(session: ChatSession) {
        val existingSessions = getAllChatSessions().toMutableList()
        existingSessions.removeAll { it.id == session.id }
        existingSessions.add(0, session)
        val sessionsToKeep = existingSessions.take(50)

        val json = gson.toJson(sessionsToKeep)
        sharedPreferences.edit()
            .putString("chat_sessions", json)
            .apply()
    }

    fun getAllChatSessions(): List<ChatSession> {
        val json = sharedPreferences.getString("chat_sessions", null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<ChatSession>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun deleteChatSession(sessionId: String) {
        val existingSessions = getAllChatSessions().toMutableList()
        existingSessions.removeAll { it.id == sessionId }

        val json = gson.toJson(existingSessions)
        sharedPreferences.edit()
            .putString("chat_sessions", json)
            .apply()
    }

    fun getChatSession(sessionId: String): ChatSession? {
        return getAllChatSessions().find { it.id == sessionId }
    }

    fun generateChatTitle(messages: List<StoredChatMessage>): String {
        val userMessages = messages.filter { it.role == "user" }
        return if (userMessages.isNotEmpty()) {
            val firstMessage = userMessages.first().content
            if (firstMessage.length > 30) {
                firstMessage.take(30) + "..."
            } else {
                firstMessage
            }
        } else {
            "New Chat"
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    fun chatMessageToStored(chatMessage: ChatMessage, sender: Sender? = null): StoredChatMessage {
        return StoredChatMessage(
            role = chatMessage.role,
            content = chatMessage.content,
            sender = sender
        )
    }
    fun storedToChatMessage(storedMessage: StoredChatMessage): ChatMessage {
        return ChatMessage(role = storedMessage.role, content = storedMessage.content)
    }
}