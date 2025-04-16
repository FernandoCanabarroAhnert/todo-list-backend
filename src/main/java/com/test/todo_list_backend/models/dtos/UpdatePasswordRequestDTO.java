package com.test.todo_list_backend.models.dtos;

import com.test.todo_list_backend.validators.UpdatePasswordRequestDTOValid;

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
@UpdatePasswordRequestDTOValid
public class UpdatePasswordRequestDTO {

    @NotBlank(message = "Required field")
    private String currentPassword;
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;

}
