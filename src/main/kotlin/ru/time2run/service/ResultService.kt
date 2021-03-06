package ru.time2run.service

import kotlinx.coroutines.delay
import mu.KotlinLogging
import ru.time2run.AppProps
import ru.time2run.DB
import ru.time2run.model.Athlete
import ru.time2run.model.ParserResults
import ru.time2run.model.Time2RunResult
import ru.time2run.skrapeit.scrapeAthlete

private val logger = KotlinLogging.logger {}

class ResultService(private val parserResults: ParserResults, private val lost: List<Int>, private val db: DB) {

    suspend fun handle(): List<Time2RunResult> {
        val athleteIds = parserResults.getAthleteIds()
        val cachedAthletes =
            db.transaction {
                athleteRepository.selectWhere(athleteIds)
            }
        val idsToScrapeIds = athleteIds - cachedAthletes.map { it.barcodeId }.toSet()
        val scrapedAthletes = scrape(idsToScrapeIds)
        val athletes = cachedAthletes + scrapedAthletes
        return getResult(athletes)
    }

    private suspend fun scrape(athleteIds: List<Int>): List<Athlete> {
        val athletes = mutableListOf<Athlete>()
        for (athleteId in athleteIds) {
            try {
                val athlete = scrapeAthlete(athleteId)
                athletes.add(athlete)
                db.transaction {
                    athleteRepository.save(athlete)
                }
                delay(AppProps.requestDelay)
            } catch (e: Exception) {
                logger.catching(e)
            }
        }
        return athletes
    }

    private fun getResult(athletes: List<Athlete>): List<Time2RunResult> {
        val athletesMap = athletes.associateBy { it.barcodeId }
        val scannerMap = parserResults.scannerResults.associateBy { it.position }
        val isFirstPosZero = parserResults.isFirstPositionIsZero()
        val positionShift = if (isFirstPosZero) 1 else 0
        var positionAlter = if (isFirstPosZero) 1 else 0
        val lostSet = lost.toSet()
        return parserResults.timerResults.map {
            val shiftedPosition = it.position + positionShift
            if (shiftedPosition in lostSet) {
                positionAlter ++
            }
            val position = it.position + positionAlter
            val scannerResult = scannerMap[position]
            val athlete = if (scannerResult != null) {
                athletesMap[scannerResult.athleteId]
            } else null
            Time2RunResult(
                shiftedPosition,
                it.time,
                athlete?.name
            )
        }
    }

}