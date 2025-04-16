package com.test.todo_list_backend.services.exceptions;

public class IncorrectCurrentPasswordException extends RuntimeException {

    public IncorrectCurrentPasswordException() {
        super("Current password is incorrect.");
    }

}
