package com.sheffmachine.kotlinbootproject.dto

import com.sheffmachine.kotlinbootproject.entity.Todo
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TodoMapper {

    fun toEntity(request: TodoCreateRequest): Todo {
        return Todo(
            title = request.title,
            description = request.description,
            completed = false,
            createdAt = LocalDateTime.now()
        )
    }

    fun toResponse(entity: Todo): TodoResponse {
        return TodoResponse(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            completed = entity.completed,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toResponseList(entities: List<Todo>): List<TodoResponse> {
        return entities.map { toResponse(it) }
    }

    fun updateEntity(entity: Todo, request: TodoUpdateRequest): Todo {
        request.title?.let { entity.title = it }
        request.description?.let { entity.description = it }
        request.completed?.let { entity.completed = it }
        entity.updatedAt = LocalDateTime.now()
        return entity
    }
}