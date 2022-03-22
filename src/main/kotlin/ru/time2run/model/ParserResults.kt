package ru.time2run.model

import ru.time2run.AppProps

class ParserResults {
    private var lastUpdate: Long = System.currentTimeMillis()
    private var isScrapingData = false
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

    private fun checkLifetime() {
        if (System.currentTimeMillis() - lastUpdate > AppProps.storageLifetime) {
            scannerResults = emptyList()
            timerResults = emptyList()
        }
        lastUpdate = System.currentTimeMillis()
    }

    fun startScrape() {
        isScrapingData = true
    }

    fun stopScrape() {
        scannerResults = emptyList()
        timerResults = emptyList()
        isScrapingData = false
    }

}