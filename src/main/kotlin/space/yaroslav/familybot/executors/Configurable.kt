package space.yaroslav.familybot.executors

import space.yaroslav.familybot.models.router.FunctionId

interface Configurable {

    fun getFunctionId(): FunctionId
}
