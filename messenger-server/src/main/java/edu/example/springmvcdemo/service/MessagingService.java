package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.KafkaConfig;
import edu.example.springmvcdemo.dto.messages_kafka.MessageDto;
import edu.example.springmvcdemo.model.User;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@AllArgsConstructor
public class MessagingService {

    private final RoomService roomService;
    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;

    public static int getPartitionNumber(String username) {
        return username.hashCode() % KafkaConfig.MESSAGES_PARTITION_COUNT;
    }

    public void sendMessage(User sender, Long roomId) {
        var room = roomService.getById(roomId);
        if (room.isDeleted()) {
            throw new AccessDeniedException("Room is not available");
        }

        var roomUsers = room.getRoomUsers();
        if (roomUsers.size() != 2) {
            throw new IllegalStateException("User count in room not equals 2");
        }

        for (var roomUser : roomUsers) {
            if (!roomUser.isAgreed()) {
                throw new AccessDeniedException("Messaging not agreed");
            }
        }

        var receiverInfo = roomUsers.get(0).getUser().equals(sender) ? roomUsers.get(1) : roomUsers.get(0);
        var receiver = receiverInfo.getUser();

        var message = new MessageDto();
        message.setMessageId(roomService.getNextMessageIdInRoom(roomId));
        message.setRoomId(roomId);
        message.setReceiverUsername(receiver.getUsername());

        try {
            allAcksKafkaTemplate.send(KafkaConfig.MESSAGES_TOPIC_NAME, getPartitionNumber(receiver.getUsername()),
                    null, message).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
