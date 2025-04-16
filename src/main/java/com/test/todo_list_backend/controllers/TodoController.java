package com.test.todo_list_backend.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.test.todo_list_backend.models.dtos.TodoResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoUpdateRequestDTO;
import com.test.todo_list_backend.models.dtos.TodosSummaryResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoRequestDTO;
import com.test.todo_list_backend.services.TodoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<TodoResponseDTO>> findAllNotCompletedTodosFromConnectedUser(Pageable pageable) {
        Page<TodoResponseDTO> response = this.todoService.findAllNotCompletedTodosFromConnectedUser(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/completed")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<TodoResponseDTO>> findAllCompletedTodosFromConnectedUser(Pageable pageable) {
        Page<TodoResponseDTO> response = this.todoService.findAllCompletedTodosFromConnectedUser(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<TodoResponseDTO> findTodoById(@PathVariable String id) {
        return ResponseEntity.ok(this.todoService.findTodoById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> createTodo(@RequestPart("request") @Valid TodoRequestDTO request,
                                        @RequestPart("image") MultipartFile image) {
        this.todoService.createTodo(request, image);
        return ResponseEntity.status(201).build();
    }

    @PutMapping(value = "/{id}", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> updateTodo(@PathVariable String id, 
                                        @RequestPart("request") @Valid TodoUpdateRequestDTO request,
                                        @RequestPart(name = "image", required = false ) MultipartFile image) {
        this.todoService.updateTodo(id, request, image);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteTodo(@PathVariable String id) {
        this.todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<TodosSummaryResponseDTO> getTodosSummaryFromConnectedUser() {
        TodosSummaryResponseDTO todosSummary = this.todoService.getTodosSummaryFromConnectedUser();
        return ResponseEntity.ok(todosSummary);
    }

}
