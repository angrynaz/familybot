package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.delay
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.sendRandomSticker
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.models.telegram.CommandByUser
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.models.telegram.stickers.StickerPack
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.telegram.BotConfig
import java.time.LocalDate
import java.time.ZoneOffset

abstract class SendRandomStickerExecutor(
    botConfig: BotConfig,
    private val historyRepository: CommandHistoryRepository
) :
    CommandExecutor(botConfig) {

    override fun execute(update: Update): suspend (AbsSender) -> Unit {

        if (isInvokedToday(update.toUser())) {
            return {}
        }

        return {
            it.send(update, getMessage())
            delay(1000)
            it.sendRandomSticker(update, getStickerPack())
        }
    }

    private fun isInvokedToday(user: User): Boolean {
        val commandsFromUserToday = historyRepository.get(
            user,
            from = LocalDate.now().atTime(0, 0).toInstant(ZoneOffset.UTC)
        ).map(CommandByUser::command)
        return commandsFromUserToday.any { it == command() }
    }

    abstract fun getMessage(): String
    abstract fun getStickerPack(): StickerPack
}
