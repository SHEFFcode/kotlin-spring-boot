package com.sheffmachine.kotlinbootproject.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.sheffmachine.kotlinbootproject.dto.TodoCreateRequest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
class TodoApplicationIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `full todo lifecycle should work end to end`() {
        val baseUrl = "http://localhost:$port/api/todos"

        // Create a todo
        val createRequest = TodoCreateRequest(
            title = "Integration Test Todo",
            description = "End to end test"
        )

        val createResponse = restTemplate.postForEntity(
            baseUrl,
            createRequest,
            Map::class.java
        )

        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        assertNotNull(createResponse.body)

        val todoId = (createResponse.body!!["id"] as Number).toLong()
        assertNotNull(todoId)

        // Get the created todo
        val getResponse = restTemplate.getForEntity(
            "$baseUrl/$todoId",
            Map::class.java
        )

        assertEquals(HttpStatus.OK, getResponse.statusCode)
        assertEquals("Integration Test Todo", getResponse.body!!["title"])

        // Get all todos
        val getAllResponse = restTemplate.getForEntity(
            baseUrl,
            Array::class.java
        )

        assertEquals(HttpStatus.OK, getAllResponse.statusCode)
        assertEquals(1, getAllResponse.body!!.size)

        // Toggle completion
        restTemplate.patchForObject(
            "$baseUrl/$todoId/toggle",
            null,
            Map::class.java
        )

        // Verify completion was toggled
        val toggledTodo = restTemplate.getForEntity(
            "$baseUrl/$todoId",
            Map::class.java
        )
        assertEquals(true, toggledTodo.body!!["completed"])

        // Delete the todo
        restTemplate.delete("$baseUrl/$todoId")

        // Verify deletion
        val deletedResponse = restTemplate.getForEntity(
            "$baseUrl/$todoId",
            Map::class.java
        )
        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.statusCode)
    }

    @Test
    fun `application context should load successfully`() {
        // This test verifies that the Spring context loads without errors
        // If the application fails to start, this test will fail
        assert(postgres.isRunning)
    }
}