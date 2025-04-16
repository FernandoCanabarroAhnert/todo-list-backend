package com.test.todo_list_backend.services.exceptions;

public class ExpiredActivationCodeException extends RuntimeException {

    public ExpiredActivationCodeException(String email) {
        super("Expired activation code. A new confirmation e-mail will be sent to " + email);	
    }

}
