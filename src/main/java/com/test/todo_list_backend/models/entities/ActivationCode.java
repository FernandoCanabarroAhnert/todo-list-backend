package com.test.todo_list_backend.models.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "activation_codes")
public class ActivationCode {

    @Id
    private String id;
    @DBRef(lazy = true)
    private User user;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isValidated;
    private LocalDateTime validatedAt;

    public boolean isValid() {
        return this.expiresAt.isAfter(LocalDateTime.now());
    }

}
