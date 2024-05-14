package edu.example.springmvcdemo.model;

import lombok.Getter;
import java.util.Set;

@Getter
public enum Role {
    USER,
    ADMIN;

    private Set<Role> roles;

    static {
        USER.roles = Set.of(USER);
        ADMIN.roles = Set.of(USER, ADMIN);
    }
}
