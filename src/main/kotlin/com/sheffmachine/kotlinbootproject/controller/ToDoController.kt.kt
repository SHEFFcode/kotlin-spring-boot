package com.sheffmachine.kotlinbootproject.controller

import com.sheffmachine.kotlinbootproject.dto.*
import com.sheffmachine.kotlinbootproject.service.TodoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = ["http://localhost:3000"])
class TodoController(
    private val todoService: TodoService,
    private val todoMapper: TodoMapper
) {

    @GetMapping
    fun getAllTodos(): List<TodoResponse> {
        val todos = todoService.getAllTodos()
        return todoMapper.toResponseList(todos)
    }

    @GetMapping("/{id}")
    fun getTodoById(@PathVariable id: Long): TodoResponse {
        val todo = todoService.getTodoById(id)
        return todoMapper.toResponse(todo)
    }

    @PostMapping
    fun createTodo(@Valid @RequestBody request: TodoCreateRequest): ResponseEntity<TodoResponse> {
        val todo = todoMapper.toEntity(request)
        val createdTodo = todoService.createTodo(todo)
        return ResponseEntity(todoMapper.toResponse(createdTodo), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: Long, 
        @Valid @RequestBody request: TodoUpdateRequest
    ): TodoResponse {
        val existingTodo = todoService.getTodoById(id)
        val updatedEntity = todoMapper.updateEntity(existingTodo, request)
        val savedTodo = todoService.updateTodo(id, updatedEntity)
        return todoMapper.toResponse(savedTodo)
    }

    @PatchMapping("/{id}/toggle")
    fun toggleTodoCompletion(@PathVariable id: Long): TodoResponse {
        val updatedTodo = todoService.toggleTodoCompletion(id)
        return todoMapper.toResponse(updatedTodo)
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(@PathVariable id: Long): ResponseEntity<Void> {
        todoService.deleteTodo(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/completed")
    fun getCompletedTodos(): List<TodoResponse> {
        val todos = todoService.getCompletedTodos()
        return todoMapper.toResponseList(todos)
    }

    @GetMapping("/pending")
    fun getPendingTodos(): List<TodoResponse> {
        val todos = todoService.getPendingTodos()
        return todoMapper.toResponseList(todos)
    }
}