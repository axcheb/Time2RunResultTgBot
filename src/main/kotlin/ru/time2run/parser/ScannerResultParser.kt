package ru.time2run.parser

import mu.KotlinLogging
import ru.time2run.model.ScannerResult
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

fun isScannerResult(csv: String): Boolean {
    return try {
        val line = csv.lineSequence().drop(1).first()
        parseScannerLine(line) != null
    } catch (e: NoSuchElementException) {
        logger.catching(e)
        false
    }
}

fun parseScannerResult(csv: String): List<ScannerResult> {
    val result = mutableListOf<ScannerResult>()
    for (line in csv.lineSequence().drop(1)) {
        val parseResult = parseScannerLine(line)
        if (parseResult != null) {
            result.add(parseResult)
        }
    }
    return result
}

private fun parseScannerLine(line: String): ScannerResult? {
    return try {
        val parts = line.split(",")
        if (parts.size == 3
            && parts[0].length > 1
            && parts[1].length > 1
            && parts[0][0] == 'A'
            && parts[1][0] == 'P'
        ) {
            ScannerResult(
                parts[0].trim().drop(1).toInt(),
                parts[1].trim().drop(1).toInt(),
            )
        } else null
    } catch (e: NumberFormatException) {
        logger.catching(e)
        null
    } catch (e: DateTimeParseException) {
        logger.catching(e)
        null
    }
}
