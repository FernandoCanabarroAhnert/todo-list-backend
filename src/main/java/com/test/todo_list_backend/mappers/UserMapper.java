package com.test.todo_list_backend.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.test.todo_list_backend.models.dtos.RegistrationRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdateUserInfosRequestDTO;
import com.test.todo_list_backend.models.entities.User;

public class UserMapper {

    public static User convertRequestDTOToEntity(RegistrationRequestDTO request, PasswordEncoder passwordEncoder) {
        return User.builder()
            .fullName(request.getFullName())
            .userName(request.getUserName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .isActive(false)
            .roles(new HashSet<>(Arrays.asList()))
            .todos(new ArrayList<>(Arrays.asList()))
            .build(); 
    }

    public static void updateUserEntity(User user, UpdateUserInfosRequestDTO request) {
        user.setFullName(request.getFullName());
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
    }

}
