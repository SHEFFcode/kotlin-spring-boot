package com.sheffmachine.kotlinbootproject.dto

import java.time.LocalDateTime

data class TodoResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)