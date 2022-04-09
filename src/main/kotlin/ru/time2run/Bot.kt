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
import dev.inmo.tgbotapi.utils.StorageFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
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

private val chatStorage = ChatStorage()

private val logger = KotlinLogging.logger {}

suspend fun runBot(db: DB) {
    telegramBotWithBehaviourAndLongPolling(AppProps.botToken, CoroutineScope(Dispatchers.IO)) {
        onMedia(initialFilter = null) {
            try {
                val pathedFile = bot.getFileAdditionalInfo(it.content.media)
                val fileSizeBytes = pathedFile.fileSize
                if (fileSizeBytes != null && fileSizeBytes > 30_000L) {
                    sendMessage(
                        it.chat.id,
                        "Файл слишком большой. Обратитесь к администратору бота, чтобы увеличить размер обрабатываемого файла."
                    )
                    return@onMedia
                }
                val csvFile = String(bot.downloadFile(pathedFile))
                if (isScannerResult(csvFile)) {
                    val parserResults = chatStorage.scannerResults(it.chat.id.chatId, parseScannerResult(csvFile))
                    if (parserResults.canHandle()) {
                        handleResults(this, db, it.chat.id, parserResults)
                    } else {
                        sendMessage(it.chat.id, "Результат сканера принят. Жду результат таймера.")
                    }
                } else if (isTimerResult(csvFile)) {
                    val parserResults = chatStorage.timerResults(it.chat.id.chatId, parseTimerResult(csvFile))
                    if (parserResults.canHandle()) {
                        handleResults(this, db, it.chat.id, parserResults)
                    } else {
                        sendMessage(it.chat.id, "Результат таймера принят. Жду результат сканера.")
                    }
                } else {
                    sendMessage(it.chat.id, "Не могу обработать этот файл.")
                }
            } catch (e: Exception) {
                logger.error(e) { "Error while processing media" }
            }
        }
        command("start") {
            sendMessage(it.chat.id, "Пришли мне два csv файла от таймера и сканера. Я пришлю тебе таблицу результатов.")
        }
        commandWithArgs("lost") { commonMessage: CommonMessage<TextContent>, strings: Array<String> ->
            val chatId = commonMessage.chat.id
            if (strings.isEmpty()) {
                val chatParams = db.transaction {
                    chatParamsRepository.selectBy(chatId.chatId)
                }
                if (chatParams == null || chatParams.lostPositions.isEmpty()) {
                    sendMessage(chatId, "Потерянных карточек нет!")
                } else {
                    sendMessage(chatId, "Потерянные карточки: ${chatParams.lostPositions.joinToString(", ")}")
                }
                return@commandWithArgs
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
            sendMessage(chatId, message)
        }
    }.second.join()
}

suspend fun handleResults(bot: TelegramBot, db: DB, chatId: ChatId, parserResults: ParserResults) {
    bot.sendMessage(chatId, "Обрабатыаю результаты. Это может занять какое-то время...")

    val chatParams = db.transaction {
        chatParamsRepository.selectBy(chatId.chatId)
    }
    val lost = chatParams?.lostPositions ?: emptyList()
    val service = ResultService(parserResults, lost, db)
    parserResults.startScrape()
    try {
        val results = service.handle()
        val csv = HEADER + results.joinToString("\n") { it.toCsvString() }
        bot.sendDocument(
            chatId,
            MultipartFile(StorageFile("results-${LocalDate.now()}.csv", csv.toByteArray())),
            text = "Результаты были сформированы с учетом потерянных карточек позиций с номерами: ${lost.joinToString(", ")}\n" +
                    "Чтобы изменить список потерянных карточек, используйте команду /lost\n" +
                    "Например, чтобы указать, что потеряны карточки 26, 31 и 42, наберите:\n" +
                    "/lost 26 31 42\n" +
                    "Чтобы удалить все потерянные карточки, наберите любой текст после команды /lost"
        )
    } catch (e: Exception) {
        logger.catching(e)
    } finally {
        parserResults.stopScrape()
    }
}