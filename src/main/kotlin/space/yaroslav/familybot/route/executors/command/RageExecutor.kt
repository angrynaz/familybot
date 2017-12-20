package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.KeywordConfig
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.ifaces.ConfigRepository
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.models.Command
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class RageExecutor(val historyRepository: HistoryRepository,
                   val configRepository: ConfigRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.RAGE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val commands = historyRepository.get(update.message.from.toUser(telegramChat = update.message.chat),
                from = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant())
        if (commands.map { it.command }.contains(command())) {
            return { it.execute(SendMessage(update.message.chatId, "Да похуй мне на тебя, чертила")) }
        }
        configRepository.set(KeywordConfig(0, true, Instant.now().plus(1, ChronoUnit.MINUTES)))
        return {it.execute(SendMessage(update.message.chatId, "НУ ВЫ ОХУЕВШИЕ"))
        }
    }

}