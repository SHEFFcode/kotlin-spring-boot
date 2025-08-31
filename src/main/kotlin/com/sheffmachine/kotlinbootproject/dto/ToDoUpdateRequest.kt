package com.sheffmachine.kotlinbootproject.dto

import jakarta.validation.constraints.Size

data class TodoUpdateRequest(
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String? = null,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    val completed: Boolean? = null
)