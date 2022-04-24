package ru.time2run.model

import ru.time2run.AppProps

class ParserResults {
    private var lastUpdate: Long = System.currentTimeMillis()
    private var _scannerResults: List<ScannerResult> = emptyList()
    val scannerResults: List<ScannerResult>
        get() = _scannerResults
    fun setScannerResults(value: List<ScannerResult>) {
        checkLifetime()
        _scannerResults = value
    }

    private var _timerResults: List<TimerResult> = emptyList()
    val timerResults: List<TimerResult>
        get() = _timerResults
    fun setTimerResults(value: List<TimerResult>) {
        checkLifetime()
        _timerResults = value
    }

    fun getAthleteIds() = _scannerResults.map { it.athleteId }

    fun canHandle(): Boolean {
        return (_scannerResults.isNotEmpty() && _timerResults.isNotEmpty())
    }

    /**
     * Проверка значения первой позиции.
     * Мобильные приложения разных версий начинают отсчет позиций либо с 0, либо с 1.
     */
    fun isFirstPositionIsZero(): Boolean {
        val tr = _timerResults
        return (tr.isNotEmpty() && tr.first().position == 0)
    }

    private fun checkLifetime() {
        if (System.currentTimeMillis() - lastUpdate > AppProps.storageLifetime) {
            _scannerResults = emptyList()
            _timerResults = emptyList()
        }
        lastUpdate = System.currentTimeMillis()
    }

    fun stopScrape() {
        _scannerResults = emptyList()
        _timerResults = emptyList()
    }

}