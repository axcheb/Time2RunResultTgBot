package ru.time2run.parser

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import ru.time2run.model.TimerResult
import java.time.LocalTime

internal class TimerResultParserKtTest {

    @Test
    fun parseTimerResult() {
        val timerResultStr = """STARTOFEVENT,12/03/2022 09:05:46,virtual_volunteer_ios_2.0.8_64
0,12/03/2022 09:05:46
1,12/03/2022 09:24:42, 00:18:56
2,12/03/2022 09:24:55, 00:19:09
ENDOFEVENT,20/03/2022 22:07:12"""
        val timerResult = parseTimerResult(timerResultStr)
        assertIterableEquals(
            listOf(
                TimerResult(
                    1,
                    LocalTime.of(0, 18, 56),
//                    LocalDateTime.of(2022, 3, 12, 9, 24, 42),
                ),
                TimerResult(
                    2,
                    LocalTime.of(0, 19, 9),
//                    LocalDateTime.of(2022, 3, 12, 9, 24, 55)
                )
            ),
            timerResult
        )
    }
}