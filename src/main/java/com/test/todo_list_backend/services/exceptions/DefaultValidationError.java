package com.test.todo_list_backend.services.exceptions;

public class DefaultValidationError extends RuntimeException {

    public DefaultValidationError(String message) {
         super(message);
    }

}
