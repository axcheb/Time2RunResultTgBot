package ru.time2run.model

/**
 * Результат парсинга одной строки csv сканера.
 * Строка вида:
 * A0000000,P0165,19/03/2022 17:24:03
 */
data class ScannerResult(
    val athleteId: Int,
    val position: Int,
)