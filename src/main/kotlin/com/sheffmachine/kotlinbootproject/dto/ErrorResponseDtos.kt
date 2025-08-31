package com.sheffmachine.kotlinbootproject.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: Map<String, Any>? = null
) {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    fun getFormattedTimestamp(): LocalDateTime = timestamp
}

data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int = 400,
    val error: String = "Validation Failed",
    val message: String,
    val path: String,
    val fieldErrors: Map<String, String> = emptyMap()
) {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    fun getFormattedTimestamp(): LocalDateTime = timestamp
}