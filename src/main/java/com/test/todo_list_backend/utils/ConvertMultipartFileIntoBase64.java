package com.test.todo_list_backend.utils;

import java.io.IOException;
import java.util.Base64;

import org.springframework.web.multipart.MultipartFile;

import com.test.todo_list_backend.services.exceptions.DefaultValidationError;

public class ConvertMultipartFileIntoBase64 {

    public static String convertMultipartFileIntoBase64(MultipartFile image) {
        try {
            String base64Prefix = "data:image/png;base64,";
            return base64Prefix + Base64.getEncoder().encodeToString(image.getBytes());
        }
        catch (IOException e) {
            throw new DefaultValidationError("Error while converting image to Base64");
        }
    }

}
