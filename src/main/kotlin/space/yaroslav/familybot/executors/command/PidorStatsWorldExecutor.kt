package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.common.utils.PluralizedWordsProvider
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset

@Component
class PidorStatsWorldExecutor(
    private val repository: CommonRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config), Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.STATS_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val pidorsByChat = repository.getAllPidors(
            startDate = LocalDateTime.of(
                LocalDate.of(2000, Month.JANUARY, 1),
                LocalTime.MIDNIGHT
            ).toInstant(ZoneOffset.UTC)
        )
            .map(Pidor::user)
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.get(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { context.get(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { context.get(Phrase.PLURALIZED_COUNT_MANY) }
                )
            )
            .take(100)

        val title = "${context.get(Phrase.PIDOR_STAT_WORLD)}:\n".bold()
        return { it.send(update, title + pidorsByChat.joinToString("\n"), enableHtml = true) }
    }
}
