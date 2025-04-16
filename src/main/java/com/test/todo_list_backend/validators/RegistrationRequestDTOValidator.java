package com.test.todo_list_backend.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.test.todo_list_backend.models.dtos.RegistrationRequestDTO;
import com.test.todo_list_backend.models.dtos.exceptions.FieldMessage;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegistrationRequestDTOValidator implements ConstraintValidator<RegistrationRequestDTOValid, RegistrationRequestDTO> {

    @Override
    public void initialize(RegistrationRequestDTOValid ann) {}

    @Override
    public boolean isValid(RegistrationRequestDTO request, ConstraintValidatorContext context) {
        List<FieldMessage> errors = new ArrayList<>();
        String password = request.getPassword();

        if (!Pattern.matches(".*[A-Z].*", password)) {
            errors.add(new FieldMessage("password", "Password must contain at least one uppercase letter"));
        }
        if (!Pattern.matches(".*[a-z].*", password)) {
            errors.add(new FieldMessage("password", "Password must contain at least one lowercase letter"));
        }
        if (!Pattern.matches(".*[0-9].*", password)) {
            errors.add(new FieldMessage("password", "Password must contain at least one number"));
        }
        if (!Pattern.matches(".*[\\W].*", password)) {
            errors.add(new FieldMessage("password", "Password must contain at least one special character"));
        }

        for (FieldMessage f : errors){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(f.getMessage())
                .addPropertyNode(f.getFieldName())
                .addConstraintViolation();
        }

        return errors.isEmpty();
    }

}
