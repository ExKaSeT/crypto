package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.RoomUser;
import edu.example.springmvcdemo.model.RoomUserId;
import edu.example.springmvcdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomUserRepository extends JpaRepository<RoomUser, RoomUserId> {
    List<RoomUser> getAllByRoomId(long roomId);

    List<RoomUser> getAllByUser(User user);
}
