package ru.time2run.log

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import ru.time2run.AppProps

object TgLogger {
    suspend fun error(bot: TelegramBot, e: Exception) {
        sendMessageToAdmin(bot, "Error:\n${e.message}\n${e.stackTraceToString()}" )
    }

    suspend fun forward(bot: TelegramBot, message: CommonMessage<*>) {
        val chatId = AppProps.adminChatId ?: return
        bot.forwardMessage(message.chat.id, ChatId(chatId), message.messageId, true)
    }

    private suspend fun sendMessageToAdmin(bot: TelegramBot, message: String) {
        val chatId = AppProps.adminChatId ?: return
        bot.sendMessage(ChatId(chatId), message)
    }
}
