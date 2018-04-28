package space.yaroslav.familybot.route.executors.eventbased.askworld

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Priority

@Component
class AskWorldSendReplyExecutor(val askWorldRepository: AskWorldRepository) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val replyToDeliver = askWorldRepository
            .getQuestionsFromChat(update.message.chat.toChat())
            .flatMap { askWorldRepository.getReplies(it) }
            .filterNot { askWorldRepository.isReplyDelivered(it) }
        val chat = update.toChat()
        val question =
            askWorldRepository.findQuestionByMessageId(update.message.replyToMessage.messageId + chat.id, chat)
        return { sender ->
            replyToDeliver.forEach {
                val questionMessage = question.message.takeIf { it.length < 100 } ?: question.message.take(100)+"..."
                val message = SendMessage(
                    update.toChat().id, "Ответ из чата ${it.chat.name.bold()} от ${it.user.getGeneralName()} " +
                        "на вопрсос \"$questionMessage\" : ${it.message.italic()}"
                )
                    .enableHtml(true)
                sender.execute(message)
                Thread.sleep(1000)
                askWorldRepository.addReplyDeliver(it)
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return askWorldRepository
            .getQuestionsFromChat(message.chat.toChat())
            .flatMap { askWorldRepository.getReplies(it) }
            .any { !askWorldRepository.isReplyDelivered(it) }
    }

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }
}