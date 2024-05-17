package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.RoomUser;
import edu.example.springmvcdemo.model.RoomUserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomUserRepository extends JpaRepository<RoomUser, RoomUserId> {
}
