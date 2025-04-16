package com.test.todo_list_backend.models.entities;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
@Document(collection = "users")
public class User implements UserDetails, Principal {

    @Id
    private String id;
    private String fullName;
    private String userName;
    private String email;
    private String password;
    private boolean isActive;
    private Set<Role> roles;
    @DBRef(lazy = true)
    private List<Todo> todos;

    @Override
    public String getName() {
        return this.email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public String getUserName() {
        return this.userName;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

}
