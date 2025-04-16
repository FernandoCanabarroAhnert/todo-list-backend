package com.test.todo_list_backend.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyExistingEmailResponseDTO {

    @JsonProperty("isAlreadyInUse")
    private boolean isAlreadyInUse;

}
