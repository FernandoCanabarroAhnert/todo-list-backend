package com.test.todo_list_backend.services.exceptions;

public class AccountNotActivatedException extends RuntimeException {

    public AccountNotActivatedException(String message) {
        super(message);
    }

}
