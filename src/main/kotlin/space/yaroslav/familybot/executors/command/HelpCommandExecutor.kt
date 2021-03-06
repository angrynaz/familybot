package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class HelpCommandExecutor(private val dictionary: Dictionary, config: BotConfig) : CommandExecutor(config) {

    override fun command(): Command {
        return Command.HELP
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            it.send(
                update,
                dictionary.get(Phrase.HELP_MESSAGE, update),
                enableHtml = true,
                customization = {
                    disableWebPagePreview = true
                    disableNotification = true
                }
            )
        }
    }
}
