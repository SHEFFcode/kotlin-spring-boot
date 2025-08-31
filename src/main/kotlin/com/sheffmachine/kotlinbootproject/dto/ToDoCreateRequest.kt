package com.sheffmachine.kotlinbootproject.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class TodoCreateRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null
)
