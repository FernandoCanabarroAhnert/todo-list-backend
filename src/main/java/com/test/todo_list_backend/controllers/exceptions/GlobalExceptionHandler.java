package com.test.todo_list_backend.controllers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.test.todo_list_backend.models.dtos.exceptions.StandardError;
import com.test.todo_list_backend.models.dtos.exceptions.ValidationError;
import com.test.todo_list_backend.services.exceptions.AccountNotActivatedException;
import com.test.todo_list_backend.services.exceptions.AlreadyExistingEmailException;
import com.test.todo_list_backend.services.exceptions.DefaultValidationError;
import com.test.todo_list_backend.services.exceptions.EmailException;
import com.test.todo_list_backend.services.exceptions.ExpiredActivationCodeException;
import com.test.todo_list_backend.services.exceptions.ForbiddenException;
import com.test.todo_list_backend.services.exceptions.IncorrectCurrentPasswordException;
import com.test.todo_list_backend.services.exceptions.ResourceNotFoundException;
import com.test.todo_list_backend.services.exceptions.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<StandardError> emailSending(UnauthorizedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(status.value(), "Email exception", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ExpiredActivationCodeException.class)
    public ResponseEntity<StandardError> expiredActivationCode(ExpiredActivationCodeException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(status.value(), "Expired Activation Code", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<StandardError> unauthorized(UnauthorizedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandardError err = new StandardError(status.value(), "Unauthorized", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<StandardError> forbidden(ForbiddenException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardError err = new StandardError(status.value(), "Forbidden", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<StandardError> accountNotActivated(AccountNotActivatedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardError err = new StandardError(status.value(), "Account not activated", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(status.value(), "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AlreadyExistingEmailException.class)
    public ResponseEntity<StandardError> alreadyExistingEmail(AlreadyExistingEmailException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        StandardError err = new StandardError(status.value(), "Email already exists", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(IncorrectCurrentPasswordException.class)
    public ResponseEntity<StandardError> incorrectCurrentPassword(IncorrectCurrentPasswordException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        StandardError err = new StandardError(status.value(), "Incorrect current password", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> invalidData(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationError err = new ValidationError(status.value(), "Validation error", "All fields are required", request.getRequestURI());
        for (FieldError f : ex.getBindingResult().getFieldErrors()) {
            err.addError(f.getField(), f.getDefaultMessage());
        }
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DefaultValidationError.class)
    public ResponseEntity<StandardError> defaultValidationError(DefaultValidationError ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        StandardError err = new StandardError(status.value(), "Validation error", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

}
