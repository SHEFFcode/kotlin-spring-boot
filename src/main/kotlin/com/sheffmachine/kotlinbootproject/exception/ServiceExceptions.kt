package com.sheffmachine.kotlinbootproject.exception

sealed class TodoException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class TodoNotFoundException(id: Long) : TodoException("Todo with id $id not found")

class TodoValidationException(
    message: String,
    val fieldErrors: Map<String, String> = emptyMap()
) : TodoException(message)

class TodoOperationException(
    message: String,
    cause: Throwable? = null
) : TodoException(message, cause)