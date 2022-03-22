package ru.time2run.parser

import ru.time2run.model.TimerResult
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

fun isTimerResult(csv: String): Boolean {
    return try {
        val parts = csv.lineSequence().drop(1).first().split(",")
        parts[0].trim().toInt()
        LocalDateTime.parse(parts[1].trim(), DATE_TIME_FORMAT)
        true
    } catch (e: NumberFormatException) {
        false
    } catch (e: NoSuchElementException) {
        false
    }
}

fun parseTimerResult(csv: String): List<TimerResult> {
    val result = mutableListOf<TimerResult>()
    for (line in csv.lineSequence().drop(1)) {
        val parseResult = parseTimerLine(line)
        if (parseResult != null) {
            result.add(parseResult)
        }
    }
    return result
}

private fun parseTimerLine(line: String): TimerResult? {
    return try {
        val parts = line.split(",")
        if (parts.size == 3) {
            TimerResult(
                parts[0].trim().toInt(),
                LocalTime.parse(parts[2].trim()),
//                LocalDateTime.parse(parts[1].trim(), DATE_TIME_FORMAT),
            )
        } else null
    } catch (e: NumberFormatException) {
        null
    } catch (e: DateTimeParseException) {
        null
    }
}