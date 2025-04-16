package com.test.todo_list_backend.models.dtos.exceptions;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError extends StandardError {

    private List<FieldMessage> fieldMessages = new ArrayList<>();

    public ValidationError(Integer status, String error, String message, String path) {
        super(status, error, message, path);
    }

    public void addError(String fieldName, String message) {
        this.fieldMessages.add(new FieldMessage(fieldName, message));
    }

}
