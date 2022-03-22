package ru.time2run.parser

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import ru.time2run.model.ScannerResult

internal class ScannerResultParserKtTest {

    @Test
    fun parse() {
        val scannerResultStr =
            """Start of File,08/01/2022 09:24:26,virtual_volunteer_android_2.1.0_147-HUAWEI_JSN-L21
A1000001,P0002,08/01/2022 09:24:26
A2000002,P0001,08/01/2022 09:25:11"""
        val scannerResult = parseScannerResult(scannerResultStr)
        assertIterableEquals(
            listOf(
                ScannerResult(
                    1000001,
                    2,
//                    LocalDateTime.of(2022, Month.JANUARY, 8, 9, 24, 26)
                ),
                ScannerResult(
                    2000002,
                    1,
//                    LocalDateTime.of(2022, Month.JANUARY, 8, 9, 25, 11)
                )
            ),
            scannerResult
        )
    }
}