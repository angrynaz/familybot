package space.yaroslav.familybot.common

import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.repos.ifaces.CommandByUser
import space.yaroslav.familybot.route.models.Command
import java.nio.charset.Charset
import java.sql.ResultSet
import java.time.*
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern


fun org.telegram.telegrambots.api.objects.Chat.toChat(): Chat = Chat(this.id, this.title)

fun org.telegram.telegrambots.api.objects.User.toUser(chat: Chat? = null, telegramChat: org.telegram.telegrambots.api.objects.Chat? = null): User {
    val internalChat = telegramChat?.toChat() ?: chat
    val format = (this.firstName?.let { it + " " } ?: "") + (this.lastName ?: "")
    return User(this.id.toLong(), internalChat!!, format, this.userName)
}

fun Instant.isToday(): Boolean {
    return LocalDate.now().atTime(0, 0).isBefore(LocalDateTime.ofInstant(this, ZoneId.of("UTC")))
}

fun <T> List<T>.random(): T? {
    return this[ThreadLocalRandom.current().nextInt(0, this.size)]
}

fun <T> Set<T>.random(): T? {
    return this.toList().random()
}

fun String?.removeEmoji(): String? {
    if (this == null) return null

    val utf8Bytes = this.toByteArray(charset(
            "UTF-8"))

    val utf8tweet = String(
            utf8Bytes, Charset.forName("UTF-8"))


    val unicodeOutliers = Pattern.compile(
            "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
            Pattern.UNICODE_CASE or Pattern.CANON_EQ or Pattern.CASE_INSENSITIVE
    )
    val unicodeOutlierMatcher = unicodeOutliers.matcher(utf8tweet)

    return unicodeOutlierMatcher.replaceAll("")
}


fun String?.bold(): String? {
    if (this == null) return null

    return "<b>$this</b>"
}

fun String?.italic(): String? {
    if (this == null) return null

    return "<i>$this</i>"
}
val monthMap = mapOf(
        Month.JANUARY to "январь",
        Month.FEBRUARY to "февраль",
        Month.MARCH to "март",
        Month.APRIL to "апрель",
        Month.MAY to "май",
        Month.JUNE to "июнь",
        Month.JULY to "июль",
        Month.AUGUST to "август",
        Month.SEPTEMBER to "сентябрь",
        Month.OCTOBER to "октябрь",
        Month.NOVEMBER to "ноябрь",
        Month.DECEMBER to "декабрь"
)
fun Month.toRussian(): String {
    return monthMap[this]!!
}

fun String?.dropLastDelimiter(): String? {
    if(this == null){
        return null
    }
    return if(!this.last().isLetterOrDigit()){
        this.dropLast(1)
    } else {
        this
    }
}

fun <T> ResultSet.map(action: (ResultSet) -> T): List<T> {
    val result = ArrayList<T>()
    while (next()) {
        result.add(action.invoke(this))
    }
    return result
}

fun formatTopList(pidors: List<User>): List<String> {
    fun format(index: Int, stats: Pair<User, Int>): String {
        val generalName = stats.first.name ?: stats.first.nickname
        val i = "${index + 1}.".bold()
        val stat = "${stats.second} раз(а)".italic()
        return "$i $generalName — $stat"
    }
    return pidors.groupBy { it }
            .map { it.key to it.value.size }
            .sortedByDescending { it.second }
            .mapIndexed { index, pair -> format(index, pair) }
}

fun ResultSet.toUser(): User = User(
        this.getLong("id"),
        Chat(this.getLong("chat_id"), ""),
        this.getString("name"),
        this.getString("username"))

fun ResultSet.toChat(): Chat = Chat(
        this.getLong("id"),
        this.getString("name")
)

fun ResultSet.toPidor(): Pidor = Pidor(
        this.toUser(),
        this.getTimestamp("pidor_date").toInstant())

fun ResultSet.toCommandByUser(user: User?): CommandByUser {
    val userInternal = user ?: this.toUser()
    return CommandByUser(userInternal, Command.values().find { it.id == this.getInt("command_id") }!!,
            this.getTimestamp("command_date").toInstant())
}

fun Update.toChat(): Chat{
    if(this.message == null){
        throw RuntimeException("Cant process ${this}")
    }
    return Chat(this.message.chat.id, this.message.chat.title)
}

fun Update.toUser(): User{
    val user = this.message.from
    val formatedName = (user.firstName?.let { it + " " } ?: "") + (user.lastName ?: "")
    return User(user.id.toLong(), this.toChat(), formatedName, user.userName)
}

