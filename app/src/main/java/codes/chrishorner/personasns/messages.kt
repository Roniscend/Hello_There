package codes.chrishorner.personasns

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class Message(
  val sender: Sender,
  val text: String,
  val id: String,
)

enum class Sender(@DrawableRes val image: Int, val color: Color) {
  Ann(image = R.drawable.ann, color = Color(0xFFFE93C9)),
  Ryuji(image = R.drawable.ryuji, color = Color(0xFFF0EA40)),
  Yusuke(image = R.drawable.yusuke, color = Color(0xFF1BC8F9)),

  Ren(image = -1, color = Color.Unspecified),
}


class MessagesState {
  private var count = 0
  private val dynamicMessages = mutableListOf<Message>()
  private var idCounter = 0

  fun advance(): ImmutableList<Message> {
    count++

    if (dynamicMessages.isNotEmpty()) {
      return dynamicMessages.take(count.coerceAtMost(dynamicMessages.size)).toImmutableList()
    }
      if (count > DefaultMessages.size) {
      count = 1
    }

    return DefaultMessages.take(count).toImmutableList()
  }

  fun getCurrentMessages(): ImmutableList<Message> {
    if (dynamicMessages.isNotEmpty()) {
      return dynamicMessages.take(count.coerceAtMost(dynamicMessages.size)).toImmutableList()
    }

    val currentCount = if (count > DefaultMessages.size) 1 else count
    return DefaultMessages.take(currentCount).toImmutableList()
  }

  fun addMessage(sender: Sender, text: String) {
    dynamicMessages.add(Message(sender = sender, text = text, id = "dynamic_${idCounter++}"))
  }

  fun addUserMessage(text: String) {
    addMessage(Sender.Ren, text)
  }

  fun addBotMessage(text: String) {
    val botSenders = listOf(Sender.Ann, Sender.Ryuji, Sender.Yusuke)
    val selectedSender = botSenders[dynamicMessages.size % botSenders.size]
    addMessage(selectedSender, text)
  }

  fun addBotMessageWithSender(text: String, sender: Sender) {
    addMessage(sender, text)
  }

  fun reset() {
    count = 0
    dynamicMessages.clear()
    idCounter = 0
  }
}private val DefaultMessages = persistentListOf(
  Message(
    sender = Sender.Ann,
    text = "We have to find them tomorrow for sure. This is the only lead we have right now.",
    id = "default_1"
  ),
  Message(
    sender = Sender.Yusuke,
    text = "Yes. It is highly likely that this part-time solicitor is somehow related to the mafia.",
    id = "default_2"
  ),
  Message(
    sender = Sender.Yusuke,
    text = "If we tail him, he may lead us straight back to his boss.",
    id = "default_3"
  ),
  Message(
    sender = Sender.Ryuji,
    text = "He talked to Iida and Nishiyama over at Central Street, right?",
    id = "default_4"
  ),
  Message(
    sender = Sender.Yusuke,
    text = "Indeed, it seems that is where our target waits. But then... who should be the one to go?",
    id = "default_5"
  ),
  Message(
    sender = Sender.Ren,
    text = "Morgana, I choose you.",
    id = "default_6"
  ),
  Message(
    sender = Sender.Ann,
    text = "That's not a bad idea. Cats have nine lives, right? Morgana can spare one for this.",
    id = "default_7"
  ),
  Message(
    sender = Sender.Ryuji,
    text = "Wouldn't the mafia get caught off guard if they had a cat coming to deliver for 'em?",
    id = "default_8"
  ),
  Message(
    sender = Sender.Yusuke,
    text = "In other words, Maaku will be going. I have no objections.",
    id = "default_9"
  ),
  Message(
    sender = Sender.Yusuke,
    text = "Tricking people and using that as blackmail… These bastards are true cowards.",
    id = "default_10"
  ),
  Message(
    sender = Sender.Ann,
    text = "It's kinda scary to think people like that are all around us in this city...",
    id = "default_11"
  ),
  Message(
    sender = Sender.Ryuji,
    text = "Well guys, we gotta brace ourselves. We're up against a serious criminal here.",
    id = "default_12"
  ),
)