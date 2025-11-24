package model

import java.time.LocalDateTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MIN_TITLE_LENGTH = 3
        const val MAX_TITLE_LENGTH = 100

        fun validate(title: String): ValidationResult =
            when {
                title.isBlank() ->
                    ValidationResult.Error("Title is required. Please enter a task description.")
                title.length < MIN_TITLE_LENGTH ->
                    ValidationResult.Error(
                        "Title must be at least $MIN_TITLE_LENGTH characters. Currently: ${title.length} characters."
                    )
                title.length > MAX_TITLE_LENGTH ->
                    ValidationResult.Error(
                        "Title must be less than $MAX_TITLE_LENGTH characters. Currently: ${title.length} characters."
                    )
                else -> ValidationResult.Success
            }
    }
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
