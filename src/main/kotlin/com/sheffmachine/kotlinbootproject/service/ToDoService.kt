package com.sheffmachine.kotlinbootproject.service

import com.sheffmachine.kotlinbootproject.entity.Todo
import com.sheffmachine.kotlinbootproject.repository.TodoRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TodoService(private val todoRepository: TodoRepository) {

    fun getAllTodos(): List<Todo> {
        return todoRepository.findAllByOrderByCreatedAtDesc()
    }

    fun getTodoById(id: Long): Todo? {
        return todoRepository.findById(id).orElse(null)
    }

    fun createTodo(todo: Todo): Todo {
        return todoRepository.save(todo)
    }

    fun updateTodo(id: Long, updatedTodo: Todo): Todo? {
        return todoRepository.findById(id).map { existingTodo ->
            val todoToUpdate = existingTodo.copy(
                title = updatedTodo.title,
                description = updatedTodo.description,
                completed = updatedTodo.completed,
                updatedAt = LocalDateTime.now()
            )
            todoRepository.save(todoToUpdate)
        }.orElse(null)
    }

    fun toggleTodoCompletion(id: Long): Todo? {
        return todoRepository.findById(id).map { todo ->
            val updatedTodo = todo.copy(
                completed = !todo.completed,
                updatedAt = LocalDateTime.now()
            )
            todoRepository.save(updatedTodo)
        }.orElse(null)
    }

    fun deleteTodo(id: Long): Boolean {
        return if (todoRepository.existsById(id)) {
            todoRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    fun getCompletedTodos(): List<Todo> {
        return todoRepository.findByCompletedOrderByCreatedAtDesc(true)
    }

    fun getPendingTodos(): List<Todo> {
        return todoRepository.findByCompletedOrderByCreatedAtDesc(false)
    }
}