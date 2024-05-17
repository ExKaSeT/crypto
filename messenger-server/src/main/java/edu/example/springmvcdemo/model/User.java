package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @Column(nullable = false, length = 30, name = "username")
    private String username;

    @Column(nullable = false, length = 50, name = "password")
    private String password;

    @Column(nullable = false, length = 50, name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, name = "is_banned")
    private boolean isBanned;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoomUser> roomUsers;
}
