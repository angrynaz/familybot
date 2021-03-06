package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.services.settings.EasyKeyValueRepository
import space.yaroslav.familybot.services.settings.FuckOffOverride

@Component
class FunctionsConfigureRepository(
    private val keyValueRepository: EasyKeyValueRepository
) {

    private val fuckOffFunctions = listOf(
        FunctionId.CHATTING,
        FunctionId.GREETINGS,
        FunctionId.HUIFICATE,
        FunctionId.TALK_BACK
    )

    @Timed("repository.RedisFunctionsConfigureRepository.isEnabled")
    fun isEnabled(id: FunctionId, chat: Chat): Boolean {
        if (id in fuckOffFunctions &&
            keyValueRepository.get(FuckOffOverride, chat.key()) == true
        ) {
            return false
        }
        return isEnabledInternal(id, chat)
    }

    @Timed("repository.RedisFunctionsConfigureRepository.switch")
    suspend fun switch(id: FunctionId, chat: Chat) {
        switchInternal(id, chat)
    }

    suspend fun setStatus(id: FunctionId, chat: Chat, isEnabled: Boolean) {
        if (isEnabledInternal(id, chat) != isEnabled) {
            switchInternal(id, chat)
        }
    }

    private fun isEnabledInternal(
        id: FunctionId,
        chat: Chat
    ): Boolean {
        return keyValueRepository.get(id.easySetting, chat.key()) ?: true
    }

    private fun switchInternal(
        id: FunctionId,
        chat: Chat
    ) {
        val currentValue = keyValueRepository.get(id.easySetting, chat.key()) ?: true
        keyValueRepository.put(id.easySetting, chat.key(), currentValue.not())
    }
}
