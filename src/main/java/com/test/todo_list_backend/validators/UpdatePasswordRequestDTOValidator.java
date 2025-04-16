package com.test.todo_list_backend.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.test.todo_list_backend.models.dtos.UpdatePasswordRequestDTO;
import com.test.todo_list_backend.models.dtos.exceptions.FieldMessage;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UpdatePasswordRequestDTOValidator implements ConstraintValidator<UpdatePasswordRequestDTOValid, UpdatePasswordRequestDTO> {

    @Override
    public void initialize(UpdatePasswordRequestDTOValid ann) {}

    @Override
    public boolean isValid(UpdatePasswordRequestDTO request, ConstraintValidatorContext context) {
        List<FieldMessage> errors = new ArrayList<>();
        String password = request.getNewPassword();

        if (!Pattern.matches(".*[A-Z].*", password)) {
            errors.add(new FieldMessage("newPassword", "Password must contain at least one uppercase letter"));
        }
        if (!Pattern.matches(".*[a-z].*", password)) {
            errors.add(new FieldMessage("newPassword", "Password must contain at least one lowercase letter"));
        }
        if (!Pattern.matches(".*[0-9].*", password)) {
            errors.add(new FieldMessage("newPassword", "Password must contain at least one number"));
        }
        if (!Pattern.matches(".*[\\W].*", password)) {
            errors.add(new FieldMessage("newPassword", "Password must contain at least one special character"));
        }

        for (FieldMessage f : errors) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(f.getMessage())
                .addPropertyNode(f.getFieldName())
                .addConstraintViolation();
        }

        return errors.isEmpty();
    }

}
