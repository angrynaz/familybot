package space.yaroslav.familybot.common.extensions

import space.yaroslav.familybot.models.askworld.AskWorldQuestion
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.CommandByUser
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.telegram.FamilyBot
import java.sql.ResultSet
import java.util.UUID

fun ResultSet.toUser(): User = User(
    this.getLong("id"),
    Chat(this.getLong("chat_id"), ""),
    this.getString("name"),
    this.getString("username")
)

fun ResultSet.getUuid(columnLabel: String): UUID = UUID.fromString(getString(columnLabel))

fun ResultSet.toChat(): Chat = Chat(
    this.getLong("id"),
    this.getString("name")
)

fun ResultSet.toPidor(): Pidor = Pidor(
    this.toUser(),
    this.getTimestamp("pidor_date").toInstant()
)

fun ResultSet.toCommandByUser(user: User?): CommandByUser {
    val userInternal = user ?: this.toUser()
    val command = Command
        .values()
        .find { it.id == this.getInt("command_id") }
        ?: throw FamilyBot.InternalException("Command id should exist")
    return CommandByUser(
        userInternal,
        command,
        this.getTimestamp("command_date").toInstant()
    )
}

fun ResultSet.toAskWorldQuestion(): AskWorldQuestion {
    val chat = Chat(this.getLong("chat_id"), this.getString("chat_name"))
    return AskWorldQuestion(
        this.getLong("id"),
        this.getString("question"),
        User(
            this.getLong("user_id"),
            chat,
            this.getString("common_name"),
            this.getString("username")
        ),
        chat,
        this.getTimestamp("date").toInstant(),
        null
    )
}

fun <T> ResultSet.map(action: (ResultSet) -> T): List<T> {
    val result = ArrayList<T>()
    while (next()) {
        result.add(action.invoke(this))
    }
    return result
}
