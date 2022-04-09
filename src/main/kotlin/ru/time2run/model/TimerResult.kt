package ru.time2run.model

import java.time.LocalTime

/**
 * Результат парсинга одной строки csv таймера.
 * Строка вида:
 * 1,12/03/2022 09:24:42, 00:18:56
 *
 * Нумерация в таких файлах идет с 0.
 */
data class TimerResult (
    val position: Int,
    val time: LocalTime,
)