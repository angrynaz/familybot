package space.yaroslav.familybot.telegram

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("settings")
class BotConfig {
    var token: String? = null
    var botname: String? = null
    var developer: String? = null
    var developerId: String? = null
    var yandexKey: String? = null
}
