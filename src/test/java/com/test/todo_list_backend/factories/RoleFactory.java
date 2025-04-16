package com.test.todo_list_backend.factories;

import com.test.todo_list_backend.models.entities.Role;

public class RoleFactory {

    public static Role create() {
        return Role.builder()
            .id("id")
            .authority("authority")
            .build();
    }

}
