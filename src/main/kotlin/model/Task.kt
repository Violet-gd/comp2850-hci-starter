package model

import java.time.LocalDateTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
