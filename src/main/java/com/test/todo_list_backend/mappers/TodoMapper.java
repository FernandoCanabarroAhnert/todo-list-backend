package com.test.todo_list_backend.mappers;

import java.time.LocalDateTime;

import com.test.todo_list_backend.models.dtos.TodoResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoUpdateRequestDTO;
import com.test.todo_list_backend.models.dtos.TodoRequestDTO;
import com.test.todo_list_backend.models.entities.Todo;
import com.test.todo_list_backend.models.entities.User;
import com.test.todo_list_backend.models.enums.TodoPriority;
import com.test.todo_list_backend.models.enums.TodoStatus;

public class TodoMapper {

    public static Todo convertRequestDTOToEntity(TodoRequestDTO request, User user) {
        return Todo.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(TodoPriority.fromValue(request.getPriority()))
            .status(TodoStatus.NOT_STARTED)
            .user(user)
            .createdAt(LocalDateTime.now())
            .expiresAt(request.getExpiresAt())
            .build();
    }

    public static TodoResponseDTO convertEntityToResponseDTO(Todo todo) {
        return TodoResponseDTO.builder()
            .id(todo.getId())
            .title(todo.getTitle())
            .description(todo.getDescription())
            .priority(todo.getPriority().getPriority())
            .status(todo.getStatus().getStatus())
            .image(todo.getImage())
            .userId(todo.getUser().getId())
            .createdAt(todo.getCreatedAt())
            .expiresAt(todo.getExpiresAt())
            .build();
    }

    public static void updateTodoEntity(Todo todo, TodoUpdateRequestDTO request) {
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setPriority(TodoPriority.fromValue(request.getPriority()));
        todo.setStatus(TodoStatus.fromValue(request.getStatus()));
        todo.setExpiresAt(request.getExpiresAt());
    }

}
