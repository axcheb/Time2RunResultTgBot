package ru.time2run.model

import java.util.concurrent.ConcurrentHashMap

object ChatStorage {
    private val chatId2Result: ConcurrentHashMap<Long, ParserResults> = ConcurrentHashMap()

    fun scannerResults(chatId: Long, scannerResults: List<ScannerResult>): ParserResults {
        val parserResults = chatId2Result.getOrPut(chatId) {
            ParserResults()
        }
        parserResults.setScannerResults(scannerResults)
        return parserResults
    }

    fun timerResults(chatId: Long, timerResults: List<TimerResult>): ParserResults {
        val parserResults = chatId2Result.getOrPut(chatId) {
            ParserResults()
        }
        parserResults.setTimerResults(timerResults)
        return parserResults
    }
}