package com.sheffmachine.kotlinbootproject.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sheffmachine.kotlinbootproject.entity.TestDataFactory
import com.sheffmachine.kotlinbootproject.dto.TodoCreateRequest
import com.sheffmachine.kotlinbootproject.dto.TodoUpdateRequest
import com.sheffmachine.kotlinbootproject.repository.TodoRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@Testcontainers
@WithMockUser
@Transactional
class TodoControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var todoRepository: TodoRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        todoRepository.deleteAll()
    }

    @Test
    fun `POST api_todos should create new todo successfully`() {
        // Given
        val createRequest = TodoCreateRequest(
            title = "New Todo",
            description = "New Description"
        )

        // When & Then
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("New Todo"))
            .andExpect(jsonPath("$.description").value("New Description"))
            .andExpect(jsonPath("$.completed").value(false))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdAt").exists())
    }

    @Test
    fun `POST api_todos should return validation error for blank title`() {
        // Given
        val createRequest = TodoCreateRequest(
            title = "",
            description = "Description"
        )

        // When & Then
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Request validation failed"))
            .andExpect(jsonPath("$.fieldErrors.title").exists())
    }

    @Test
    fun `POST api_todos should return validation error for too long title`() {
        // Given
        val longTitle = "a".repeat(256)
        val createRequest = TodoCreateRequest(
            title = longTitle,
            description = "Description"
        )

        // When & Then
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors.title").value("Title must not exceed 255 characters"))
    }

    @Test
    fun `GET api_todos should return all todos ordered by creation date desc`() {
        // Given
        val now = LocalDateTime.now()
        val todo1 = todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Todo 1", createdAt = now.minusMinutes(2)))
        val todo2 = todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Todo 2", createdAt = now.minusMinutes(1)))

        // When & Then
        mockMvc.perform(get("/api/todos"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("Todo 2"))
            .andExpect(jsonPath("$[1].title").value("Todo 1"))
    }

    @Test
    fun `GET api_todos should return empty list when no todos exist`() {
        // When & Then
        mockMvc.perform(get("/api/todos"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `GET api_todos_id should return todo when exists`() {
        // Given
        val todo = todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Test Todo"))

        // When & Then
        mockMvc.perform(get("/api/todos/${todo.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(todo.id))
            .andExpect(jsonPath("$.title").value("Test Todo"))
            .andExpect(jsonPath("$.completed").value(false))
    }

    @Test
    fun `GET api_todos_id should return 404 when todo not exists`() {
        // When & Then
        mockMvc.perform(get("/api/todos/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Todo with id 999 not found"))
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `PUT api_todos_id should update existing todo`() {
        // Given
        val todo = todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Original Title"))
        val updateRequest = TodoUpdateRequest(
            title = "Updated Title",
            description = "Updated Description",
            completed = true
        )

        // When & Then
        mockMvc.perform(
            put("/api/todos/${todo.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.description").value("Updated Description"))
            .andExpect(jsonPath("$.completed").value(true))
            .andExpect(jsonPath("$.updatedAt").exists())
    }

    @Test
    fun `PUT api_todos_id should return 404 when todo not exists`() {
        // Given
        val updateRequest = TodoUpdateRequest(title = "Updated Title")

        // When & Then
        mockMvc.perform(
            put("/api/todos/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Todo with id 999 not found"))
    }

    @Test
    fun `PATCH api_todos_id_toggle should toggle completion status`() {
        // Given
        val todo = todoRepository.save(TestDataFactory.createTodo(id = 0L, completed = false))

        // When & Then
        mockMvc.perform(patch("/api/todos/${todo.id}/toggle"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))
            .andExpect(jsonPath("$.updatedAt").exists())

        // Verify second toggle
        mockMvc.perform(patch("/api/todos/${todo.id}/toggle"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(false))
    }

    @Test
    fun `DELETE api_todos_id should delete existing todo`() {
        // Given
        val todo = todoRepository.save(TestDataFactory.createTodo(id = 0L))

        // When & Then
        mockMvc.perform(delete("/api/todos/${todo.id}"))
            .andExpect(status().isNoContent)

        // Verify deletion
        mockMvc.perform(get("/api/todos/${todo.id}"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `DELETE api_todos_id should return 404 when todo not exists`() {
        // When & Then
        mockMvc.perform(delete("/api/todos/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Todo with id 999 not found"))
    }

    @Test
    fun `GET api_todos_completed should return only completed todos`() {
        // Given
        todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Completed Todo", completed = true))
        todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Pending Todo", completed = false))

        // When & Then
        mockMvc.perform(get("/api/todos/completed"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Completed Todo"))
            .andExpect(jsonPath("$[0].completed").value(true))
    }

    @Test
    fun `GET api_todos_pending should return only pending todos`() {
        // Given
        todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Completed Todo", completed = true))
        todoRepository.save(TestDataFactory.createTodo(id = 0L, title = "Pending Todo", completed = false))

        // When & Then
        mockMvc.perform(get("/api/todos/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Pending Todo"))
            .andExpect(jsonPath("$[0].completed").value(false))
    }

    @Test
    fun `should handle malformed JSON gracefully`() {
        // When & Then
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Malformed JSON"))
            .andExpect(jsonPath("$.message").value("Request body contains invalid JSON"))
    }

    @Test
    fun `should handle invalid path variable type gracefully`() {
        // When & Then
        mockMvc.perform(get("/api/todos/invalid-id"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Type Mismatch"))
    }
}