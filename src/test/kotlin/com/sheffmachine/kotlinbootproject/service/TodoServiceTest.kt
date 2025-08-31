package com.sheffmachine.kotlinbootproject.service

import com.sheffmachine.kotlinbootproject.entity.TestDataFactory
import com.sheffmachine.kotlinbootproject.exception.TodoNotFoundException
import com.sheffmachine.kotlinbootproject.exception.TodoOperationException
import com.sheffmachine.kotlinbootproject.repository.TodoRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.DataIntegrityViolationException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TodoServiceTest {

    private val todoRepository = mockk<TodoRepository>()
    private val todoService = TodoService(todoRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAllTodos should return all todos ordered by creation date desc`() {
        // Given
        val todos = TestDataFactory.createTodos(3)
        every { todoRepository.findAllByOrderByCreatedAtDesc() } returns todos

        // When
        val result = todoService.getAllTodos()

        // Then
        assertEquals(3, result.size)
        assertEquals(todos, result)
        verify { todoRepository.findAllByOrderByCreatedAtDesc() }
    }

    @Test
    fun `getAllTodos should throw TodoOperationException when repository fails`() {
        // Given
        every { todoRepository.findAllByOrderByCreatedAtDesc() } throws DataAccessResourceFailureException("DB Error")

        // When & Then
        val exception = assertThrows<TodoOperationException> {
            todoService.getAllTodos()
        }
        assertEquals("Failed to retrieve todos", exception.message)
        verify { todoRepository.findAllByOrderByCreatedAtDesc() }
    }

    @Test
    fun `getTodoById should return todo when exists`() {
        // Given
        val todo = TestDataFactory.createTodo()
        every { todoRepository.findById(1L) } returns Optional.of(todo)

        // When
        val result = todoService.getTodoById(1L)

        // Then
        assertEquals(todo, result)
        verify { todoRepository.findById(1L) }
    }

    @Test
    fun `getTodoById should throw TodoNotFoundException when not exists`() {
        // Given
        every { todoRepository.findById(1L) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<TodoNotFoundException> {
            todoService.getTodoById(1L)
        }
        assertEquals("Todo with id 1 not found", exception.message)
        verify { todoRepository.findById(1L) }
    }

    @Test
    fun `createTodo should save and return todo`() {
        // Given
        val todoToSave = TestDataFactory.createTodo(id = 0L)
        val savedTodo = TestDataFactory.createTodo()
        every { todoRepository.save(todoToSave) } returns savedTodo

        // When
        val result = todoService.createTodo(todoToSave)

        // Then
        assertEquals(savedTodo, result)
        verify { todoRepository.save(todoToSave) }
    }

    @Test
    fun `createTodo should throw TodoOperationException when save fails`() {
        // Given
        val todo = TestDataFactory.createTodo()
        every { todoRepository.save(todo) } throws DataIntegrityViolationException("Save failed")

        // When & Then
        val exception = assertThrows<TodoOperationException> {
            todoService.createTodo(todo)
        }
        assertEquals("Failed to create todo", exception.message)
        verify { todoRepository.save(todo) }
    }

    @Test
    fun `updateTodo should update existing todo`() {
        // Given
        val existingTodo = TestDataFactory.createTodo()
        val updatedTodo = TestDataFactory.createTodo(title = "Updated Title")
        val savedTodo = updatedTodo.copy(updatedAt = java.time.LocalDateTime.now())
        
        every { todoRepository.findById(1L) } returns Optional.of(existingTodo)
        every { todoRepository.save(any()) } returns savedTodo

        // When
        val result = todoService.updateTodo(1L, updatedTodo)

        // Then
        assertEquals("Updated Title", result.title)
        assertNotNull(result.updatedAt)
        verify { todoRepository.findById(1L) }
        verify { todoRepository.save(any()) }
    }

    @Test
    fun `updateTodo should throw TodoNotFoundException when todo not exists`() {
        // Given
        val updatedTodo = TestDataFactory.createTodo()
        every { todoRepository.findById(1L) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<TodoNotFoundException> {
            todoService.updateTodo(1L, updatedTodo)
        }
        assertEquals("Todo with id 1 not found", exception.message)
        verify { todoRepository.findById(1L) }
        verify(exactly = 0) { todoRepository.save(any()) }
    }

    @Test
    fun `updateTodo should throw TodoOperationException when save fails`() {
        // Given
        val existingTodo = TestDataFactory.createTodo()
        val updatedTodo = TestDataFactory.createTodo(title = "Updated Title")
        
        every { todoRepository.findById(1L) } returns Optional.of(existingTodo)
        every { todoRepository.save(any()) } throws DataAccessResourceFailureException("Database connection failed")

        // When & Then
        val exception = assertThrows<TodoOperationException> {
            todoService.updateTodo(1L, updatedTodo)
        }
        assertEquals("Failed to update todo", exception.message)
        verify { todoRepository.findById(1L) }
        verify { todoRepository.save(any()) }
    }

    @Test
    fun `toggleTodoCompletion should toggle completion status`() {
        // Given
        val todo = TestDataFactory.createTodo(completed = false)
        val toggledTodo = todo.copy(completed = true, updatedAt = java.time.LocalDateTime.now())
        
        every { todoRepository.findById(1L) } returns Optional.of(todo)
        every { todoRepository.save(any()) } returns toggledTodo

        // When
        val result = todoService.toggleTodoCompletion(1L)

        // Then
        assertTrue(result.completed)
        assertNotNull(result.updatedAt)
        verify { todoRepository.findById(1L) }
        verify { todoRepository.save(any()) }
    }

    @Test
    fun `toggleTodoCompletion should throw TodoNotFoundException when not exists`() {
        // Given
        every { todoRepository.findById(1L) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<TodoNotFoundException> {
            todoService.toggleTodoCompletion(1L)
        }
        assertEquals("Todo with id 1 not found", exception.message)
        verify { todoRepository.findById(1L) }
        verify(exactly = 0) { todoRepository.save(any()) }
    }

    @Test
    fun `toggleTodoCompletion should throw TodoOperationException when save fails`() {
        // Given
        val todo = TestDataFactory.createTodo(completed = false)
        
        every { todoRepository.findById(1L) } returns Optional.of(todo)
        every { todoRepository.save(any()) } throws DataAccessResourceFailureException("Save failed")

        // When & Then
        val exception = assertThrows<TodoOperationException> {
            todoService.toggleTodoCompletion(1L)
        }
        assertEquals("Failed to toggle todo completion", exception.message)
        verify { todoRepository.findById(1L) }
        verify { todoRepository.save(any()) }
    }

    @Test
    fun `deleteTodo should delete existing todo and return true`() {
        // Given
        every { todoRepository.existsById(1L) } returns true
        every { todoRepository.deleteById(1L) } just Runs

        // When
        val result = todoService.deleteTodo(1L)

        // Then
        assertTrue(result)
        verify { todoRepository.existsById(1L) }
        verify { todoRepository.deleteById(1L) }
    }

    @Test
    fun `deleteTodo should throw TodoNotFoundException when todo not exists`() {
        // Given
        every { todoRepository.existsById(1L) } returns false

        // When & Then
        val exception = assertThrows<TodoNotFoundException> {
            todoService.deleteTodo(1L)
        }
        assertEquals("Todo with id 1 not found", exception.message)
        verify { todoRepository.existsById(1L) }
        verify(exactly = 0) { todoRepository.deleteById(1L) }
    }

    @Test
    fun `deleteTodo should throw TodoOperationException when delete fails`() {
        // Given
        every { todoRepository.existsById(1L) } returns true
        every { todoRepository.deleteById(1L) } throws DataAccessResourceFailureException("Delete failed")

        // When & Then
        val exception = assertThrows<TodoOperationException> {
            todoService.deleteTodo(1L)
        }
        assertEquals("Failed to delete todo", exception.message)
        verify { todoRepository.existsById(1L) }
        verify { todoRepository.deleteById(1L) }
    }

    @Test
    fun `getCompletedTodos should return only completed todos`() {
        // Given
        val completedTodos = listOf(TestDataFactory.createTodo(completed = true))
        every { todoRepository.findByCompletedOrderByCreatedAtDesc(true) } returns completedTodos

        // When
        val result = todoService.getCompletedTodos()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0].completed)
        verify { todoRepository.findByCompletedOrderByCreatedAtDesc(true) }
    }

    @Test
    fun `getCompletedTodos should throw TodoOperationException when repository fails`() {
        // Given
        every { todoRepository.findByCompletedOrderByCreatedAtDesc(true) } throws DataAccessResourceFailureException("DB Error")

        // When & Then
        val exception = assertThrows<TodoOperationException> {
            todoService.getCompletedTodos()
        }
        assertEquals("Failed to retrieve completed todos", exception.message)
        verify { todoRepository.findByCompletedOrderByCreatedAtDesc(true) }
    }

    @Test
    fun `getPendingTodos should return only pending todos`() {
        // Given
        val pendingTodos = listOf(TestDataFactory.createTodo(completed = false))
        every { todoRepository.findByCompletedOrderByCreatedAtDesc(false) } returns pendingTodos

        // When
        val result = todoService.getPendingTodos()

        // Then
        assertEquals(1, result.size)
        assertTrue(!result[0].completed)
        verify { todoRepository.findByCompletedOrderByCreatedAtDesc(false) }
    }

    @Test
    fun `getPendingTodos should throw TodoOperationException when repository fails`() {
        // Given
        every { todoRepository.findByCompletedOrderByCreatedAtDesc(false) } throws DataAccessResourceFailureException("DB Error")

        // When & Then
        val exception = assertThrows<TodoOperationException> {
            todoService.getPendingTodos()
        }
        assertEquals("Failed to retrieve pending todos", exception.message)
        verify { todoRepository.findByCompletedOrderByCreatedAtDesc(false) }
    }
}