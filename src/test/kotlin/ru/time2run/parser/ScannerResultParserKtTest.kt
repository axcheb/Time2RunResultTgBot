package ru.time2run.parser

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import ru.time2run.model.ScannerResult

internal class ScannerResultParserKtTest {

    @Test
    fun parse() {
        val scannerResultStr =
            """Start of File,08/01/2022 09:24:26,virtual_volunteer_android_2.1.0_147-HUAWEI_JSN-L21
,P0003,26/03/2022 09:44:22 AM
A1000001,P0002,08/01/2022 09:24:26
A3000005,,26/03/2022 09:44:22 AM
A2000002,P0001,08/01/2022 09:25:11"""
        val scannerResult = parseScannerResult(scannerResultStr)
        assertIterableEquals(
            listOf(
                ScannerResult(
                    1000001,
                    2,
                ),
                ScannerResult(
                    2000002,
                    1,
                )
            ),
            scannerResult
        )
    }
}