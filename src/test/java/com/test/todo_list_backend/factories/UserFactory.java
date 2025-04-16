package com.test.todo_list_backend.factories;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.test.todo_list_backend.models.dtos.LoginRequestDTO;
import com.test.todo_list_backend.models.dtos.RegistrationRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdatePasswordRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdateUserInfosRequestDTO;
import com.test.todo_list_backend.models.entities.ActivationCode;
import com.test.todo_list_backend.models.entities.User;

public class UserFactory {

    public static User create() {
        return User.builder()
            .id("id")
            .fullName("fullname")
            .userName("username")
            .email("email")
            .password("12345")
            .isActive(true)
            .roles(new HashSet<>(Arrays.asList(RoleFactory.create())))
            .todos(new ArrayList<>(Arrays.asList()))
            .build();
    }

    public static RegistrationRequestDTO createRegistrationRequest() {
        return RegistrationRequestDTO.builder()
            .fullName("fullname")
            .userName("username")
            .email("email")
            .password("12345")
            .build();
    }

    public static LoginRequestDTO createLoginRequest() {
        return LoginRequestDTO.builder()
            .email("email")
            .password("12345")
            .build();
    }

    public static UpdatePasswordRequestDTO createUpdatePasswordRequest() {
        return UpdatePasswordRequestDTO.builder()
            .currentPassword("12345")
            .newPassword("123456")
            .build();
    }

    public static UpdateUserInfosRequestDTO createUpdateUserInfosRequest() {
        return UpdateUserInfosRequestDTO.builder()
            .fullName("fullname")
            .userName("username")
            .email("email")
            .build();
    }

    public static ActivationCode createActivationCode() {
        return ActivationCode.builder()
            .id("id")
            .code("code")
            .user(create())
            .expiresAt(LocalDateTime.now().plusMinutes(30L))
            .build();
    }

}
