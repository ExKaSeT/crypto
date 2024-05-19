package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.KafkaConfig;
import edu.example.springmvcdemo.dto.message.SendMessageResponseDto;
import edu.example.springmvcdemo.dto.messages_kafka.MessageDto;
import edu.example.springmvcdemo.dto.message.MessageResponseDto;
import edu.example.springmvcdemo.mapper.MessageMapper;
import edu.example.springmvcdemo.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RoomService roomService;
    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;
    private final KafkaConfig kafkaConfig;
    private final MessageMapper messageMapper;
    private final UserService userService;
    private final RecordMessageConverter converter;

    public static int getPartitionNumber(String username) {
        return username.hashCode() % KafkaConfig.MESSAGES_PARTITION_COUNT;
    }

    public SendMessageResponseDto sendMessage(User sender, Long roomId, byte[] data) {
        var room = roomService.getById(roomId);

        var roomUsers = room.getRoomUsers();
        if (roomUsers.size() != 2) {
            throw new IllegalStateException("User count in room not equals 2");
        }

        var receiverInfo = roomUsers.get(0).getUser().equals(sender) ? roomUsers.get(1) : roomUsers.get(0);
        var receiver = receiverInfo.getUser();

        var messageId = roomService.getNextMessageIdInRoom(roomId);
        var message = new MessageDto();
        message.setMessageId(messageId);
        message.setRoomId(roomId);
        message.setReceiverUsername(receiver.getUsername());
        message.setData(data);

        try {
            allAcksKafkaTemplate.send(KafkaConfig.MESSAGES_TOPIC_NAME, getPartitionNumber(receiver.getUsername()),
                    null, message).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return new SendMessageResponseDto(messageId);
    }

    public List<MessageResponseDto> getMessages(User user) {
        int partition = getPartitionNumber(user.getUsername());
        long offset = isNull(user.getMessageOffset()) ? 0 : user.getMessageOffset();
        var response = new ArrayList<MessageResponseDto>();
        long lastOffset = offset;
        try (var consumer = new KafkaConsumer<Object, byte[]>(kafkaConfig.consumerFactory().getConfigurationProperties())) {
            TopicPartition topicPartition = new TopicPartition(KafkaConfig.MESSAGES_TOPIC_NAME, partition);
            consumer.assign(Collections.singletonList(topicPartition));
            consumer.seek(topicPartition, offset);

            boolean keepOnReading = true;

            while (keepOnReading) {
                ConsumerRecords<Object, byte[]> records = consumer.poll(Duration.ofMillis(100));
                if (records.isEmpty()) {
                    keepOnReading = false;
                } else {
                    for (ConsumerRecord<Object, byte[]> record : records) {
                        lastOffset = record.offset();
                        var messageInfo = (MessageDto) converter.toMessage(record, null, null, MessageDto.class).getPayload();
                        if (!messageInfo.getReceiverUsername().equals(user.getUsername())) {
                            continue;
                        }
                        response.add(messageMapper
                                .messageDtoToMessageResponseDto(messageInfo, record.timestamp()));
                    }
                }
            }
        }

        userService.setMessageOffset(user.getUsername(), lastOffset + 1);
        return response;
    }
}
