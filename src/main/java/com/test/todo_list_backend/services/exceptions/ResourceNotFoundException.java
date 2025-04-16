package com.test.todo_list_backend.services.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String object, String id) {
        super(object + " with id " + id + " not found");
    }

}
