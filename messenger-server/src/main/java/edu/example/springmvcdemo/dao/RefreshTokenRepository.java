package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteAllByUserUsername(String username);

    void deleteAllByValidUntilUtc0IsLessThan(LocalDateTime date);
}
