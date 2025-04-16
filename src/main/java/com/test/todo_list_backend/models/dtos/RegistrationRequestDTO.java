package com.test.todo_list_backend.models.dtos;

import com.test.todo_list_backend.validators.RegistrationRequestDTOValid;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@RegistrationRequestDTOValid
public class RegistrationRequestDTO {

    @NotBlank(message = "Required field")
    private String fullName;
    @NotBlank(message = "Required field")
    private String userName;
    @Email(message = "Invalid e-mail format")
    @NotBlank(message = "Required field")
    private String email;
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

}
