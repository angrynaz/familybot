package space.yaroslav.familybot.repos.ifaces

import java.time.Instant
import java.util.UUID
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.services.scenario.Scenario
import space.yaroslav.familybot.services.scenario.ScenarioMove
import space.yaroslav.familybot.services.scenario.ScenarioPoll
import space.yaroslav.familybot.services.scenario.ScenarioState
import space.yaroslav.familybot.services.scenario.ScenarioWay

interface ScenarioRepository {

    fun getScenarios(): List<Scenario>
    fun findMove(id: UUID): ScenarioMove?
    fun getAllCurrentGames(): Map<Chat, ScenarioMove>
    fun addState(scenarioMove: ScenarioMove, chat: Chat)
    fun getState(chat: Chat): ScenarioState?
    fun addChoice(chat: Chat, user: User, scenarioMove: ScenarioMove, chosenWay: ScenarioWay)
    fun removeChoice(chat: Chat, user: User, scenarioMove: ScenarioMove)
    fun getResultsForMove(chat: Chat, scenarioState: ScenarioState): Map<ScenarioWay, List<User>>
    fun savePoll(scenarioPoll: ScenarioPoll)
    fun getDataByPollId(id: String): ScenarioPoll?
    fun findScenarioPoll(chat: Chat, scenarioMove: ScenarioMove, afterDate: Instant): ScenarioPoll?
}