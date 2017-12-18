package space.yaroslav.familybot.route.executors.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.isToday
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.PidorDictionaryRepository
import space.yaroslav.familybot.route.models.Command
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Component
class PidorExecutor(val repository: CommonRepository, val dictionaryRepository: PidorDictionaryRepository) : CommandExecutor() {


    private final val log = LoggerFactory.getLogger(PidorExecutor::class.java)

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        log.info("Getting pidor from chat $chat")
        val pidor = repository
                .getPidorsByChat(chat).find { it.date.isToday() }
        val message = pidor
                ?.let { SendMessage(update.message.chatId, "Сегодняшний пидор уже обнаружен: @${it.user.nickname}") }
        if (message != null) {
            log.info("Pidor is already founded: $pidor")
            return { it.execute(message) }
        } else {
            log.info("Pidor is not found, initiating search procedure")
            val users = repository.getUsers(chat)
            val id = ThreadLocalRandom.current().nextInt(0, users.size)
            val nextPidor = users[id]
            log.info("Pidor is rolled to $nextPidor")
            repository.addPidor(Pidor(nextPidor, LocalDateTime.now()))
            val start = dictionaryRepository.getStart().random()
            val middle = dictionaryRepository.getMiddle().random()
            val finisher = dictionaryRepository.getFinish().random()
            return {
                val chatId = update.message.chatId
                it.execute(SendMessage(chatId, start))
                Thread.sleep(1000)
                it.execute(SendMessage(chatId, middle))
                Thread.sleep(1000)
                it.execute(SendMessage(chatId, finisher))
                Thread.sleep(1000)
                it.execute(SendMessage(chatId, "@${nextPidor.nickname}")) }
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }
}