package ru.time2run.model

import java.time.LocalTime

private const val UNKNOWN_ATHLETE_NAME = "Неизвестный"
const val HEADER = "Позиция,Участник,Время\n"

data class Time2RunResult(
    val position: Int,
    val time: LocalTime,
    val athleteName: String?
) {
    private fun athleteName(): String = athleteName ?: UNKNOWN_ATHLETE_NAME
    fun toCsvString() = "$position,${athleteName()},$time"
}