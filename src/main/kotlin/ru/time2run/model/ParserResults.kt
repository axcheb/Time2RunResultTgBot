package ru.time2run.model

import ru.time2run.AppProps

class ParserResults {
    private var lastUpdate: Long = System.currentTimeMillis()
    var scannerResults: List<ScannerResult> = arrayListOf()
        set(value) {
            checkLifetime()
            field = value
        }
    var timerResults: List<TimerResult> = arrayListOf()
        set(value) {
            checkLifetime()
            field = value
        }

    fun getAthleteIds() = scannerResults.map { it.athleteId }

    fun canHandle(): Boolean {
        return (scannerResults.isNotEmpty() && timerResults.isNotEmpty())
    }

    /**
     * Проверка значения первой позиции.
     * Мобильные приложения разных версий начинают отсчет позиций либо с 0, либо с 1.
     */
    fun isFirstPositionIsZero(): Boolean {
        val tr = timerResults
        return (tr.isNotEmpty() && tr.first().position == 0)
    }

    private fun checkLifetime() {
        if (System.currentTimeMillis() - lastUpdate > AppProps.storageLifetime) {
            scannerResults = emptyList()
            timerResults = emptyList()
        }
        lastUpdate = System.currentTimeMillis()
    }

    fun stopScrape() {
        scannerResults = emptyList()
        timerResults = emptyList()
    }

}