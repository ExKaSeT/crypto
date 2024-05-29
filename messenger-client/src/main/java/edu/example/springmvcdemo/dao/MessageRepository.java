package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.Message;
import edu.example.springmvcdemo.model.MessageId;
import edu.example.springmvcdemo.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, MessageId> {
    List<Message> getMessagesByRoomAndMessageId_IdGreaterThan(Room room, Long messageId);
}
