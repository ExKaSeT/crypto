package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.RestClientConfig;
import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.message.FileLocalDto;
import edu.example.springmvcdemo.dto.message.FileStatus;
import edu.example.springmvcdemo.dto.message.MessageDto;
import edu.example.springmvcdemo.dto.message.OpenKeyExchangeDto;
import edu.example.springmvcdemo.dto.rest_dto.message.MessageResponseDto;
import edu.example.springmvcdemo.dto.rest_dto.message.SendMessageRequestDto;
import edu.example.springmvcdemo.dto.rest_dto.message.SendMessageResponseDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.encryption.assymetric.DiffieHellmanEncryption;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RoomRepository roomRepository;
    private final RestClientConfig restClientConfig;
    private final UserSessionService userSessionService;
    private final MessageRepository messageRepository;
    private final StorageService storageService;

    public Long sendMessage(Room room, Object data) {
        var dataBytes = SerializationUtils.serialize(data);
        var result = restClientConfig.getRestClient().post()
                .uri(restClientConfig.getUri("/message/send"))
                .body(new SendMessageRequestDto(room.getRoomId(), dataBytes))
                .accept(MediaType.APPLICATION_JSON)
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve()
                .toEntity(SendMessageResponseDto.class);

        return result.getBody().getMessageId();
    }

    public void sendFileEncrypted(Room room, String filename, InputStream stream) {
        var encryptionPayload = (EncryptionPayload) SerializationUtils.deserialize(room.getEncryptionPayload());
        var fileLocalDto = new FileLocalDto(filename, UUID.randomUUID().toString(),
                FileStatus.IN_PROGRESS, null);
        var fileLocalBytes = SerializationUtils.serialize(fileLocalDto);
        Long messageId = sendMessage(room, new MessageDto(DataType.FILE, fileLocalBytes));
        saveMessage(messageId, room.getRoomId(), fileLocalBytes,
                DataType.FILE, true, null);

        // TODO:
        var fileDto =
        var fileDtoBytes = SerializationUtils.serialize(fileDto);
        var messageDto = new MessageDto();
        messageDto.setDataType(DataType.FILE);
        try (var encryptor = EncryptionUtils.getEncryption(encryptionPayload, room.getKey())) {
            messageDto.setData(encryptor.encrypt(fileDtoBytes));
        }
        Long messageId = sendMessage(room, messageDto);
        saveMessage(messageId, room.getRoomId(), fileDtoBytes,
                DataType.FILE, true, null);
    }

    public void sendTextEncrypted(Room room, String text) {
        var encryptionPayload = (EncryptionPayload) SerializationUtils.deserialize(room.getEncryptionPayload());
        var textBytes = text.getBytes();
        var messageDto = new MessageDto();
        messageDto.setDataType(DataType.STRING);
        try (var encryptor = EncryptionUtils.getEncryption(encryptionPayload, room.getKey())) {
            messageDto.setData(encryptor.encrypt(textBytes));
        }
        Long messageId = sendMessage(room, messageDto);
        saveMessage(messageId, room.getRoomId(), textBytes, DataType.STRING, true, null);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void pullMessages() {
        var result = restClientConfig.getRestClient().get()
                .uri(restClientConfig.getUri("/message/pull"))
                .header("Cookie", userSessionService.getAccessCookieString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<MessageResponseDto>>() {
                });

        var messages = result.getBody();
        assert messages != null;

        for (var message : messages) {
            try {
                var payload = SerializationUtils.deserialize(message.getData());
                var room = roomRepository.findById(message.getRoomId()).orElse(null);
                if (payload instanceof OpenKeyExchangeDto openKeyDto) {
                    if (isNull(room)) {
                        log.info("Message ignored. Room not exists: " + message);
                        continue;
                    }
                    var openKey = new BigInteger(openKeyDto.getOpenKey());
                    var encryption = new DiffieHellmanEncryption();

                    if (isNull(openKeyDto.getEncryptionPayload())) { // participant agreed
                        var myOpenKey = encryption.generateSecretAndOpenKey();
                        var sharedSecret = encryption.generateSharedSecret(openKey).toString();
                        room.setKey(sharedSecret);
                        room.setStatus(RoomStatus.AGREED);
                        var encryptionInfo = (EncryptionPayload) SerializationUtils.deserialize(room.getEncryptionPayload());
                        sendMessage(room, new OpenKeyExchangeDto(myOpenKey.toString(), encryptionInfo));
                        roomRepository.save(room);
                    } else { // receive from room creator
                        var mySecretKey = room.getKey();
                        encryption.setSecret(new BigInteger(mySecretKey));
                        var sharedSecret = encryption.generateSharedSecret(openKey).toString();
                        room.setKey(sharedSecret);
                        room.setStatus(RoomStatus.AGREED);
                        room.setEncryptionPayload(SerializationUtils.serialize(openKeyDto.getEncryptionPayload()));
                        roomRepository.save(room);
                    }
                } else if (payload instanceof MessageDto messageDto) {
                    var encryptionPayload = (EncryptionPayload) SerializationUtils.deserialize(room.getEncryptionPayload());
                    byte[] decrypted;
                    try (var encryptor = EncryptionUtils.getEncryption(encryptionPayload, room.getKey())) {
                        decrypted = encryptor.decrypt(messageDto.getData());
                    }
                    saveMessage(message.getMessageId(), room.getRoomId(), decrypted, messageDto.getDataType(),
                            false, new Timestamp(message.getEpochTime()));
                }
//                log.info("Receive message: " + message);
            } catch (Exception ex) {
                log.error("Can't process message: " + message + "\n" + ex.getMessage() + Arrays.toString(ex.getStackTrace()));
            }
        }
    }

    public Message saveMessage(long messageId, long roomId, byte[] dataBytes, DataType dataType, boolean isMine, @Nullable Timestamp timestamp) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        var message = new Message();
        message.setMessageId(new MessageId(messageId, roomId));
        message.setRoom(room);
        message.setData(dataBytes);
        message.setDataType(dataType);
        message.setIsMine(isMine);
        if (isNull(timestamp)) {
            message.setTimestamp(new Timestamp(Instant.now().toEpochMilli()));
        } else {
            message.setTimestamp(timestamp);
        }
        return messageRepository.save(message);
    }
}
