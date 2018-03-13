package space.yaroslav.familybot.route.executors.continious

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.executors.command.ROULETTE_MESSAGE
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.services.PidorCompetitionService
import space.yaroslav.familybot.telegram.BotConfig
import java.time.*
import java.util.concurrent.ThreadLocalRandom

@Component
class RouletteContinious(val historyRepository: HistoryRepository,
                         override val botConfig: BotConfig,
                         val pidorRepository: CommonRepository,
                         val pidorCompetitionService: PidorCompetitionService) : ContiniousConversation {

    private val log = LoggerFactory.getLogger(RouletteContinious::class.java)

    override fun getDialogMessage(): String {
        return ROULETTE_MESSAGE
    }

    override fun command(): Command {
        return Command.ROULETTE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val now = LocalDate.now()
        val user = update.toUser()
        val chatId = update.message.chatId
        val commands = historyRepository.get(user, LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                .toInstant(ZoneOffset.UTC))
        if (commands.filter { it.command == command() }.size > 1) {
            return {
                it.execute(SendMessage(chatId, "Ты уже крутил рулетку."))
                Thread.sleep(2000)
                it.execute(SendMessage(chatId, "Пидор."))
            }
        }
        val number = update.message.text.split(" ")[0].toIntOrNull()
        if (number != null && number !in 1..6) {
            return {
                it.execute(SendMessage(chatId, "Мушку спили и в следующий раз играй по правилам"))
                pidorRepository.addPidor(Pidor(user, Instant.now()))
                it.execute(SendMessage(chatId, "В наказание твое пидорское очко уходит к остальным"))
            }
        }
        val rouletteNumber = ThreadLocalRandom.current().nextInt(1, 7)
        log.info("Roulette win number is $rouletteNumber and guessed number is $number")
        return {
            if (rouletteNumber == number) {
                pidorRepository.removePidorRecord(user)
                it.execute(SendMessage(chatId, "Ты ходишь по охуенно тонкому льду"))
                Thread.sleep(2000)
                it.execute(SendMessage(chatId, "Но он пока не треснул. Свое пидорское очко можешь забрать. "))
            } else {
                it.execute(SendMessage(chatId, "Ты ходишь по охуенно тонкому льду"))
                repeat(3, { pidorRepository.addPidor(Pidor(user, Instant.now())) })
                Thread.sleep(2000)
                it.execute(SendMessage(chatId, "Сорян, но ты проиграл. Твое пидорское очко уходит в зрительный зал трижды."))
            }
            Thread.sleep(2000)
            pidorCompetitionService.pidorCompetition(update)?.invoke(it)
        }
    }
}
