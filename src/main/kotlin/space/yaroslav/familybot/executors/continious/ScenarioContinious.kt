package space.yaroslav.familybot.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.isFromAdmin
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.scenario.ScenarioService
import space.yaroslav.familybot.services.scenario.ScenarioSessionManagementService
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class ScenarioContinious(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig
) :
    ContiniousConversation(botConfig) {
    override fun getDialogMessage(message: Message) = "Какую игру выбрать?"

    override fun command() = Command.SCENARIO

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            val callbackQuery = update.callbackQuery

            if (!it.isFromAdmin(update, botConfig)) {
                it.execute(
                    AnswerCallbackQuery(callbackQuery.id)
                        .apply {
                            showAlert = true
                            text = dictionary.get(Phrase.ACCESS_DENIED, update)
                        }
                )
            } else {
                val scenarioToStart = scenarioService.getScenarios()
                    .find { scenario -> scenario.id.toString() == callbackQuery.data }
                    ?: throw FamilyBot.InternalException("Can't find a scenario ${callbackQuery.data}")
                scenarioSessionManagementService.startGame(update, scenarioToStart).invoke(it)
            }
        }
    }
}
