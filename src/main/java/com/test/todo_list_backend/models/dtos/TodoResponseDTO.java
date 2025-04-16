package com.test.todo_list_backend.models.dtos;

import java.time.LocalDateTime;

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
public class TodoResponseDTO {

    private String id;
    private String title;
    private String description;
    private Integer priority;
    private Integer status;
    private String image;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

}
