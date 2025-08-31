package com.sheffmachine.kotlinbootproject.repository

import com.sheffmachine.kotlinbootproject.entity.TestDataFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@Testcontainers
@Tag("integration")
class TodoRepositoryIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var todoRepository: TodoRepository

    @BeforeEach
    fun setUp() {
        entityManager.clear()
    }

    @Test
    fun `should work with PostgreSQL via TestContainers`() {
        // Given
        val todo = TestDataFactory.createTodo(id = 0L, title = "PostgreSQL Test")

        // When
        val savedTodo = todoRepository.save(todo)

        // Then
        assertNotNull(savedTodo.id)
        assertEquals("PostgreSQL Test", savedTodo.title)
    }
}