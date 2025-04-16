package com.test.todo_list_backend.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class UpdateUserInfosRequestDTO {

    @NotBlank(message = "Required field")
    private String fullName;
    @NotBlank(message = "Required field")
    private String userName;
    @Email(message = "Invalid e-mail format")
    @NotBlank(message = "Required field")
    private String email;

}
