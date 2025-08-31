package com.sheffmachine.kotlinbootproject.entity

import java.time.LocalDateTime

object TestDataFactory {

    fun createTodo(
        id: Long = 1L,
        title: String = "Test Todo",
        description: String? = "Test Description",
        completed: Boolean = false,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime? = null
    ) = Todo(
        id = id,
        title = title,
        description = description,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun createTodos(count: Int): List<Todo> {
        return (1..count).map { i ->
            createTodo(
                id = i.toLong(),
                title = "Todo $i",
                description = "Description $i",
                completed = i % 2 == 0,
                createdAt = LocalDateTime.now().minusHours(i.toLong())
            )
        }
    }
}