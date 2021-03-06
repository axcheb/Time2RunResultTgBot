package ru.time2run

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.get.getFileAdditionalInfo
import dev.inmo.tgbotapi.extensions.api.send.media.sendDocument
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.command
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.commandWithArgs
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMedia
import dev.inmo.tgbotapi.requests.abstracts.MultipartFile
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.abstracts.MediaContent
import dev.inmo.tgbotapi.utils.StorageFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import ru.time2run.log.TgLogger
import ru.time2run.model.ChatParams
import ru.time2run.model.ChatStorage
import ru.time2run.model.HEADER
import ru.time2run.model.ParserResults
import ru.time2run.parser.isScannerResult
import ru.time2run.parser.isTimerResult
import ru.time2run.parser.parseScannerResult
import ru.time2run.parser.parseTimerResult
import ru.time2run.service.ResultService
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

class Time2RunBot(private val db: DB) {

    suspend fun runBot() {
        telegramBotWithBehaviourAndLongPolling(AppProps.botToken, CoroutineScope(Dispatchers.IO)) {
            onMedia {
                try {
                    onMediaCommand(bot, it)
                } catch (e: Exception) {
                    logger.error(e) { "Error while processing media" }
                    TgLogger.error(bot, e)
                }
            }
            command("start") {
                sendMessage(
                    it.chat.id,
                    "Пришли мне два csv файла от таймера и сканера. Я пришлю тебе таблицу результатов."
                )
            }
            commandWithArgs("lost") { commonMessage: CommonMessage<TextContent>, strings: Array<String> ->
                onLostCommand(this, commonMessage, strings)
            }
        }.second.join()
    }

    private suspend fun onMediaCommand(bot: TelegramBot, mediaMessage: CommonMessage<MediaContent>) {
        val attachedFile = bot.getFileAdditionalInfo(mediaMessage.content.media)
        val fileSizeBytes = attachedFile.fileSize
        if (fileSizeBytes != null && fileSizeBytes > AppProps.maxFileSizeBytes) {
            bot.sendMessage(
                mediaMessage.chat.id,
                "Файл слишком большой. Обратитесь к администратору бота, чтобы увеличить размер обрабатываемого файла."
            )
            return
        }
        val csvFile = String(bot.downloadFile(attachedFile))
        TgLogger.forward(bot, mediaMessage)
        if (isScannerResult(csvFile)) {
            val parserResults = ChatStorage.scannerResults(mediaMessage.chat.id.chatId, parseScannerResult(csvFile))
            if (parserResults.canHandle()) {
                handleResults(bot, mediaMessage.chat.id, parserResults)
            } else {
                bot.sendMessage(mediaMessage.chat.id, "Результат сканера принят. Жду результат таймера.")
            }
        } else if (isTimerResult(csvFile)) {
            val parserResults = ChatStorage.timerResults(mediaMessage.chat.id.chatId, parseTimerResult(csvFile))
            if (parserResults.canHandle()) {
                handleResults(bot, mediaMessage.chat.id, parserResults)
            } else {
                bot.sendMessage(mediaMessage.chat.id, "Результат таймера принят. Жду результат сканера.")
            }
        } else {
            bot.sendMessage(mediaMessage.chat.id, "Не могу обработать этот файл.")
        }
    }

    private suspend fun onLostCommand(
        bot: TelegramBot,
        commonMessage: CommonMessage<TextContent>,
        strings: Array<String>
    ) {
        val chatId = commonMessage.chat.id
        if (strings.isEmpty()) {
            val chatParams = db.transaction {
                chatParamsRepository.selectBy(chatId.chatId)
            }
            if (chatParams == null || chatParams.lostPositions.isEmpty()) {
                bot.sendMessage(chatId, "Потерянных карточек нет!")
            } else {
                bot.sendMessage(chatId, "Потерянные карточки: ${chatParams.lostPositions.joinToString(", ")}")
            }
            return
        }

        var message = "Запомнил! Потеряны карточки: ${strings.joinToString(", ")}"
        db.transaction {
            try {
                chatParamsRepository.save(ChatParams(chatId.chatId, strings.map { it.toInt() }))
            } catch (_: NumberFormatException) {
                chatParamsRepository.save(ChatParams(chatId.chatId, listOf()))
                message = "Запомнил! Потерянных карточек нет!"
            }
        }
        bot.sendMessage(chatId, message)
    }

    private suspend fun handleResults(bot: TelegramBot, chatId: ChatId, parserResults: ParserResults) {
        bot.sendMessage(chatId, "Обрабатыаю результаты. Это может занять какое-то время...")

        val chatParams = db.transaction {
            chatParamsRepository.selectBy(chatId.chatId)
        }
        val lost = chatParams?.lostPositions ?: emptyList()
        val service = ResultService(parserResults, lost, db)
        try {
            val results = service.handle()
            val csv = HEADER + results.joinToString("\n") { it.toCsvString() }
            val multipartFile = MultipartFile(StorageFile("results-${LocalDate.now()}.csv", csv.toByteArray()))
            val text = "Результаты были сформированы с учетом потерянных карточек позиций с номерами: ${
                lost.joinToString(
                    ", "
                )
            }\n" +
                    "Чтобы изменить список потерянных карточек, используйте команду /lost\n" +
                    "Например, чтобы указать, что потеряны карточки 26, 31 и 42, наберите:\n" +
                    "/lost 26 31 42\n" +
                    "Чтобы удалить все потерянные карточки, наберите любой текст после команды /lost"

            bot.sendDocument(
                chatId,
                multipartFile,
                text = text
            )
            TgLogger.sendDocumentToAdmin(
                bot,
                multipartFile,
                text
            )
        } catch (e: Exception) {
            logger.catching(e)
            TgLogger.error(bot, e)
        } finally {
            parserResults.stopScrape()
        }
    }

}