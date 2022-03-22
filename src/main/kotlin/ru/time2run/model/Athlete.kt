package ru.time2run.model

import kotlite.annotations.Id
import java.time.LocalDateTime

data class Athlete(
    @Id
    val barcodeId: Int,
    val name: String,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
