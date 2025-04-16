package com.test.todo_list_backend.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TodoUpdateRequestDTO extends TodoRequestDTO {

    @NotNull(message = "Required field")
    private Integer status;

}
