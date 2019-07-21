package space.yaroslav.familybot.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class UpdateBuilder(private val data: MutableMap<String, Any> = HashMap()) {

    init {
        data.putAll(
            mapOf(
                "update_id" to randomInt(),
                "date" to Instant.now().epochSecond
            )
        )
    }

    private val objectMapper = ObjectMapper()

    fun message(build: MessageBuilder.() -> MessageBuilder): UpdateBuilder {
        val messageBuilder = build(MessageBuilder())
        data["message"] = messageBuilder.data
        data.remove("edited_message")
        return this
    }

    fun withEditedMessage(build: MessageBuilder.() -> MessageBuilder): UpdateBuilder {
        val messageBuilder = build(MessageBuilder())
        data["edited_message"] = messageBuilder.data
        data.remove("message")
        return this
    }

    fun simpleTextMessageFromUser(text: String): Update {
        return message {
            text { text }
            chat { ChatBuilder() }
            from { UserBuilder() }
        }.build()
    }

    fun build(): Update = objectMapper.readValue(objectMapper.writeValueAsString(data), Update::class.java)
}

class MessageBuilder(val data: MutableMap<String, Any> = HashMap()) {
    init {
        data.putAll(
            mapOf(
                "message_id" to randomInt(),
                "date" to Instant.now().epochSecond,
                "text" to UUID.randomUUID().toString()
            )
        )
    }

    fun text(text: () -> String): MessageBuilder {
        data["text"] = text()
        return this
    }

    fun chat(chat: () -> ChatBuilder): MessageBuilder {
        data["chat"] = chat().data
        return this
    }

    fun from(from: () -> UserBuilder): MessageBuilder {
        data["from"] = from().data
        return this
    }

    fun to(messageTo: MessageBuilder.() -> MessageBuilder): MessageBuilder {
        data["reply_to_message"] = messageTo(MessageBuilder()).data
        return this
    }
}

class ChatBuilder(val data: MutableMap<String, Any> = HashMap()) {
    init {
        val chatId = randomIntFrom1to3()
        data.putAll(
            mapOf(
                "id" to chatId,
                "title" to "Test chat #$chatId",
                "type" to "supergroup"
            )
        )
    }

    fun becomeUser(username: String): ChatBuilder {
        data.putAll(
            mapOf(
                "type" to "private",
                "username" to username
            )
        )
        return this
    }
}

class UserBuilder(val data: MutableMap<String, Any> = HashMap()) {
    init {
        val userId = randomIntFrom1to3()
        data.putAll(
            mapOf(
                "id" to userId,
                "username" to "user$userId"
            )
        )
    }

    fun toBot(name: String): UserBuilder {
        data.putAll(
            mapOf(
                "username" to name,
                "is_bot" to true
            )
        )
        return this
    }
}

private fun randomInt() = ThreadLocalRandom.current().nextInt()
private fun randomIntFrom1to3() = ThreadLocalRandom.current().nextInt(1, 3)

