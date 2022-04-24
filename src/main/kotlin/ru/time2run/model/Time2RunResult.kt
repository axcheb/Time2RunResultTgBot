package ru.time2run.model

import java.time.LocalTime

private const val UNKNOWN_ATHLETE_NAME = "Неизвестный"
private const val UTF_BOM = "\uFEFF"
const val HEADER = UTF_BOM + "Позиция,Участник,Время\n"

data class Time2RunResult(
    val position: Int,
    val time: LocalTime,
    val athleteName: String?
) {
    private fun athleteName(): String = athleteName ?: UNKNOWN_ATHLETE_NAME
    fun toCsvString() = "$position,${athleteName()},$time"
}