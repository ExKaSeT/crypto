package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
