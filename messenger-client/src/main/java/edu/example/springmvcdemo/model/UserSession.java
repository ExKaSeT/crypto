package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_session")
@Data
public class UserSession {

    @Id
    @Column(nullable = false, length = 30, name = "username")
    private String username;

    @Column(name = "refresh_token")
    private String refreshToken;
}