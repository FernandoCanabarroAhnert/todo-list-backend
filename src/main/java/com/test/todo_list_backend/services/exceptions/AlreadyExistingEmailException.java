package com.test.todo_list_backend.services.exceptions;

public class AlreadyExistingEmailException extends RuntimeException {

    public AlreadyExistingEmailException(String message) {
        super(message);
    }

}
