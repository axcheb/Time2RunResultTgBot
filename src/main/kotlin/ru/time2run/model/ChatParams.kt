package ru.time2run.model

import kotlite.annotations.Id
import java.time.LocalDateTime

data class ChatParams(
    @Id
    val chatId: Long,
    val lostPositions: List<Int>,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
