package edu.example.springmvcdemo.dao;

import edu.example.springmvcdemo.model.Message;
import edu.example.springmvcdemo.model.MessageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, MessageId> {
}
