package com.sheffmachine.kotlinbootproject.service

import com.sheffmachine.kotlinbootproject.entity.Todo
import com.sheffmachine.kotlinbootproject.exception.TodoNotFoundException
import com.sheffmachine.kotlinbootproject.exception.TodoOperationException
import com.sheffmachine.kotlinbootproject.repository.TodoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TodoService(private val todoRepository: TodoRepository) {

    private val logger = LoggerFactory.getLogger(TodoService::class.java)

    fun getAllTodos(): List<Todo> {
        return try {
            todoRepository.findAllByOrderByCreatedAtDesc()
        } catch (ex: Exception) {
            logger.error("Error fetching all todos", ex)
            throw TodoOperationException("Failed to retrieve todos", ex)
        }
    }

    fun getTodoById(id: Long): Todo {
        return todoRepository.findById(id).orElseThrow {
            TodoNotFoundException(id)
        }
    }

    fun createTodo(todo: Todo): Todo {
        return try {
            todoRepository.save(todo)
        } catch (ex: Exception) {
            logger.error("Error creating todo: ${todo.title}", ex)
            throw TodoOperationException("Failed to create todo", ex)
        }
    }

    fun updateTodo(id: Long, updatedTodo: Todo): Todo {
        return try {
            val existingTodo = todoRepository.findById(id).orElseThrow {
                TodoNotFoundException(id)
            }
            
            val todoToUpdate = existingTodo.copy(
                title = updatedTodo.title,
                description = updatedTodo.description,
                completed = updatedTodo.completed,
                updatedAt = LocalDateTime.now()
            )
            
            todoRepository.save(todoToUpdate)
        } catch (ex: TodoNotFoundException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Error updating todo with id: $id", ex)
            throw TodoOperationException("Failed to update todo", ex)
        }
    }

    fun toggleTodoCompletion(id: Long): Todo {
        return try {
            val todo = todoRepository.findById(id).orElseThrow {
                TodoNotFoundException(id)
            }
            
            val updatedTodo = todo.copy(
                completed = !todo.completed,
                updatedAt = LocalDateTime.now()
            )
            
            todoRepository.save(updatedTodo)
        } catch (ex: TodoNotFoundException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Error toggling completion for todo with id: $id", ex)
            throw TodoOperationException("Failed to toggle todo completion", ex)
        }
    }

    fun deleteTodo(id: Long): Boolean {
        return try {
            if (!todoRepository.existsById(id)) {
                throw TodoNotFoundException(id)
            }
            todoRepository.deleteById(id)
            true
        } catch (ex: TodoNotFoundException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Error deleting todo with id: $id", ex)
            throw TodoOperationException("Failed to delete todo", ex)
        }
    }

    fun getCompletedTodos(): List<Todo> {
        return try {
            todoRepository.findByCompletedOrderByCreatedAtDesc(true)
        } catch (ex: Exception) {
            logger.error("Error fetching completed todos", ex)
            throw TodoOperationException("Failed to retrieve completed todos", ex)
        }
    }

    fun getPendingTodos(): List<Todo> {
        return try {
            todoRepository.findByCompletedOrderByCreatedAtDesc(false)
        } catch (ex: Exception) {
            logger.error("Error fetching pending todos", ex)
            throw TodoOperationException("Failed to retrieve pending todos", ex)
        }
    }
}