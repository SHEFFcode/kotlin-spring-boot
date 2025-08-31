package com.sheffmachine.kotlinbootproject.exception

import com.ninjasquad.springmockk.MockkBean
import com.sheffmachine.kotlinbootproject.config.TestSecurityConfig
import com.sheffmachine.kotlinbootproject.controller.TodoController
import com.sheffmachine.kotlinbootproject.dto.TodoMapper
import com.sheffmachine.kotlinbootproject.service.TodoService
import io.mockk.every
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TodoController::class)
@Import(GlobalExceptionHandler::class, TestSecurityConfig::class)
@ActiveProfiles("test")
@WithMockUser
@Tag("integration")
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean  // Use SpringMockK's @MockkBean instead of Spring's @MockBean
    private lateinit var todoService: TodoService

    @MockkBean  // Use SpringMockK's @MockkBean instead of Spring's @MockBean
    private lateinit var todoMapper: TodoMapper

    @Test
    fun `should handle TodoNotFoundException`() {
        // Given
        every { todoService.getTodoById(999L) } throws TodoNotFoundException(999L)

        // When & Then
        mockMvc.perform(get("/api/todos/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Todo with id 999 not found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/todos/999"))
    }

    @Test
    fun `should handle TodoOperationException`() {
        // Given
        every { todoService.getAllTodos() } throws TodoOperationException("Database connection failed")

        // When & Then
        mockMvc.perform(get("/api/todos"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Operation Error"))
            .andExpect(jsonPath("$.message").value("Database connection failed"))
    }

    @Test
    fun `should handle validation errors`() {
        // When & Then
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "", "description": "test"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").exists())
    }

    @Test
    fun `should handle malformed JSON`() {
        // When & Then
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "test", "invalid": }""")

        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Malformed JSON"))
            .andExpect(jsonPath("$.message").value("Request body contains invalid JSON"))
    }

    @Test
    fun `should handle DataIntegrityViolationException`() {
        // Given
        every { todoService.getAllTodos() } throws DataIntegrityViolationException("Constraint violation")

        // When & Then
        mockMvc.perform(get("/api/todos"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Data Integrity Violation"))
    }

    @Test
    fun `should handle generic exceptions`() {
        // Given
        every { todoService.getAllTodos() } throws RuntimeException("Unexpected error")

        // When & Then
        mockMvc.perform(get("/api/todos"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
    }
}