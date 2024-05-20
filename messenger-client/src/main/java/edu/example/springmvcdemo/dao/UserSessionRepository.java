package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
}
