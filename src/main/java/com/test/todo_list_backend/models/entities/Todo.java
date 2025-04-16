package com.test.todo_list_backend.models.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.test.todo_list_backend.models.enums.TodoPriority;
import com.test.todo_list_backend.models.enums.TodoStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "todos")
public class Todo {

    @Id
    private String id;
    private String title;
    private String description;
    private TodoPriority priority;
    private TodoStatus status;
    private String image;
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;


}
