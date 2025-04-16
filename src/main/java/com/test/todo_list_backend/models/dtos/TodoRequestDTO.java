package com.test.todo_list_backend.models.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class TodoRequestDTO {

    @NotBlank(message = "Required field")
    private String title;
    @NotBlank(message = "Required field")
    private String description;
    @NotNull(message = "Required field")
    private Integer priority;
    @NotNull(message = "Required field")
    private LocalDateTime expiresAt;

}
