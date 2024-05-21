package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.Room;
import edu.example.springmvcdemo.model.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> getRoomsByStatus(RoomStatus status);
}
