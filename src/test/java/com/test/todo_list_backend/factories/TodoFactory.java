package com.test.todo_list_backend.factories;

import java.time.LocalDateTime;

import com.test.todo_list_backend.models.dtos.TodoRequestDTO;
import com.test.todo_list_backend.models.dtos.TodoUpdateRequestDTO;
import com.test.todo_list_backend.models.entities.Todo;
import com.test.todo_list_backend.models.enums.TodoPriority;
import com.test.todo_list_backend.models.enums.TodoStatus;

public class TodoFactory {

    public static Todo create() { 
        return Todo.builder()
            .id("id")
            .title("title")
            .description("description")
            .priority(TodoPriority.LOW)
            .status(TodoStatus.NOT_STARTED)
            .image("image")
            .user(UserFactory.create())
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();
    }

    public static TodoRequestDTO createRequest() {
        return TodoRequestDTO.builder()
            .title("title")
            .description("description")
            .priority(3)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();
    }

    public static TodoUpdateRequestDTO createUpdateRequest() {
        TodoUpdateRequestDTO request = new TodoUpdateRequestDTO();
        request.setTitle("title");
        request.setDescription("description");
        request.setPriority(3);
        request.setStatus(1);
        request.setExpiresAt(LocalDateTime.now().plusDays(1));
        return request;
    }
}
