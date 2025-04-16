package com.test.todo_list_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.test.todo_list_backend.models.dtos.AccountActivationRequestDTO;
import com.test.todo_list_backend.models.dtos.LoginRequestDTO;
import com.test.todo_list_backend.models.dtos.LoginResponseDTO;
import com.test.todo_list_backend.models.dtos.RegistrationRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdatePasswordRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdateUserInfosRequestDTO;
import com.test.todo_list_backend.models.dtos.VerifyExistingEmailResponseDTO;
import com.test.todo_list_backend.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegistrationRequestDTO request) {
        this.userService.register(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        LoginResponseDTO response = this.userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestBody @Valid AccountActivationRequestDTO request) {
        this.userService.activateAccount(request);
        return ResponseEntity.status(200).build();
    } 

    @GetMapping("/verify-token")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> verifyToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        this.userService.verifyToken(token);
        return ResponseEntity.status(200).build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<VerifyExistingEmailResponseDTO> verifyExistingEmail(@RequestParam(name = "email") String email) {
        VerifyExistingEmailResponseDTO response = this.userService.verifyExistingEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-infos")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> updateUserInfos(@RequestBody @Valid UpdateUserInfosRequestDTO request) {
        this.userService.updateUserInfos(request);
        return ResponseEntity.status(200).build();
    }

    @PutMapping("/update-password")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> updatePassword(@RequestBody @Valid UpdatePasswordRequestDTO request) {
        this.userService.updatePassword(request);
        return ResponseEntity.status(200).build();
    }
    
}
