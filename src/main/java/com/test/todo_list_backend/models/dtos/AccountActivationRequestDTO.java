package com.test.todo_list_backend.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountActivationRequestDTO {

    @NotNull(message = "Required field")
    @NotBlank(message = "Required field")
    private String code;

}
