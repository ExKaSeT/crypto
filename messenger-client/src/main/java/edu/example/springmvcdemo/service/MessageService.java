package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.RestClientConfig;
import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.message.*;
import edu.example.springmvcdemo.dto.rest_dto.message.MessageResponseDto;
import edu.example.springmvcdemo.dto.rest_dto.message.SendMessageRequestDto;
import edu.example.springmvcdemo.dto.rest_dto.message.SendMessageResponseDto;
import edu.example.springmvcdemo.dto.room.RoomDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private static final int DATA_BUFFER_BYTES = 500_000;

    private final RoomRepository roomRepository;
    private final RestClientConfig restClientConfig;
    private final UserSessionService userSessionService;
    private final MessageRepository messageRepository;
    private final StorageService storageService;

    @SneakyThrows
    public ShowMessageDto toShowMessageDto(Message message) {
        var dto = new ShowMessageDto();
        dto.setId(message.getMessageId().getId());
        dto.setRoom(RoomDto.fromRoom(message.getRoom()));
        dto.setTimestamp(message.getTimestamp());
        dto.setIsMine(message.getIsMine());
        dto.setDataType(message.getDataType());
        switch (message.getDataType()) {
            case STRING -> dto.setText(new String(message.getData()));
            case FILE -> {
                var fileInfo = (FileLocalDto) SerializationUtils.deserialize(message.getData());
                if (!fileInfo.getStatus().equals(FileStatus.READY)) {
                    dto.setText(String.format("Файл: %s\nСтатус: %s", fileInfo.getFilename(),
                            fileInfo.getStatus().equals(FileStatus.FAILED) ? "Ошибка загрузки" :
                                    fileInfo.getSizeBytes() + " байт загружено"));
                    dto.setDataType(DataType.STRING);
                    dto.setChanging(true);
                    return dto;
                }

                var extension = ImageExtension.isImage(fileInfo.getFilename());
                if (nonNull(extension) && fileInfo.getSizeBytes() < 2_000_000L) {
                    dto.setImage(true);
                    dto.setImageExtension(extension.name().toLowerCase());
                    dto.setImageBytes(Base64.getEncoder()
                            .encodeToString(storageService.getFile(fileInfo.getLocalFilename()).readAllBytes()));
                } else {
                    dto.setFilename(fileInfo.getFilename());
                }
            }
            default -> throw new IllegalStateException("Can't process data type " + message.getDataType());
        }
        return dto;
    }

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

    @SneakyThrows
    public void sendFileEncrypted(Room room, String filename, InputStream stream) {
        var encryptionPayload = (EncryptionPayload) SerializationUtils.deserialize(room.getEncryptionPayload());

        var fileLocalDto = new FileLocalDto(filename, UUID.randomUUID().toString(),
                FileStatus.IN_PROGRESS, 0L);
        var fileLocalBytes = SerializationUtils.serialize(fileLocalDto);
        var messageDto = new MessageDto();
        messageDto.setDataType(DataType.FILE);
        try (var encryptor = EncryptionUtils.getEncryption(encryptionPayload, room.getKey())) {
            messageDto.setData(encryptor.encrypt(fileLocalBytes));
        }
        Long messageId = sendMessage(room, messageDto);
        saveMessage(messageId, room.getRoomId(), fileLocalBytes,
                DataType.FILE, true, null);

        var buffer = new byte[DATA_BUFFER_BYTES];
        int readBytes;
        long currentBytes = 0;
        try (var localOutputStream = storageService.createNewFile(fileLocalDto.getLocalFilename())) {
            while ((readBytes = stream.readNBytes(buffer, 0, DATA_BUFFER_BYTES)) > 0) {
                currentBytes += readBytes;
                var fileSendDto = new FileSendDto(messageId,
                        readBytes == buffer.length ? buffer : Arrays.copyOf(buffer, readBytes),
                        currentBytes, FileStatus.IN_PROGRESS);

                localOutputStream.write(buffer, 0, readBytes);

                try (var encryptor = EncryptionUtils.getEncryption(encryptionPayload, room.getKey())) {
                    messageDto.setData(encryptor.encrypt(SerializationUtils.serialize(fileSendDto)));
                }
                sendMessage(room, messageDto);
            }
        }

        var fileSendDto = new FileSendDto(messageId, null, currentBytes, FileStatus.READY);
        try (var encryptor = EncryptionUtils.getEncryption(encryptionPayload, room.getKey())) {
            messageDto.setData(encryptor.encrypt(SerializationUtils.serialize(fileSendDto)));
        }
        sendMessage(room, messageDto);

        fileLocalDto.setStatus(FileStatus.READY);
        fileLocalDto.setSizeBytes(currentBytes);
        updateMessageData(messageId, room.getRoomId(), SerializationUtils.serialize(fileLocalDto), DataType.FILE);
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
                    switch (messageDto.getDataType()) {
                        case STRING ->
                                saveMessage(message.getMessageId(), room.getRoomId(), decrypted, messageDto.getDataType(),
                                        false, new Timestamp(message.getEpochTime()));
                        case FILE -> {
                            var fileInfo = SerializationUtils.deserialize(decrypted);
                            if (fileInfo instanceof FileLocalDto fileLocalDto) {
                                storageService.createNewFile(fileLocalDto.getLocalFilename()).close();
                                saveMessage(message.getMessageId(), room.getRoomId(), decrypted, messageDto.getDataType(),
                                        false, new Timestamp(message.getEpochTime()));
                            } else if (fileInfo instanceof FileSendDto fileSendDto) {
                                var firstFileMessage = getById(fileSendDto.getFirstMessageId(), room.getRoomId());
                                var fileLocalDto = (FileLocalDto) SerializationUtils.deserialize(firstFileMessage.getData());
                                if (!fileLocalDto.getStatus().equals(FileStatus.IN_PROGRESS)) {
                                    continue;
                                }
                                long currentPartLength = isNull(fileSendDto.getData()) ? 0 : fileSendDto.getData().length;
                                if (fileSendDto.getStatus().equals(FileStatus.FAILED) ||
                                        fileSendDto.getCurrentBytes() != currentPartLength + fileLocalDto.getSizeBytes()) {
                                    fileLocalDto.setStatus(FileStatus.FAILED);
                                    storageService.deleteFile(fileLocalDto.getLocalFilename());
                                } else if (fileSendDto.getStatus().equals(FileStatus.READY)) {
                                    fileLocalDto.setStatus(FileStatus.READY);
                                } else if (fileSendDto.getStatus().equals(FileStatus.IN_PROGRESS)) {
                                    fileLocalDto.setSizeBytes(currentPartLength + fileLocalDto.getSizeBytes());
                                    storageService.appendToFile(fileLocalDto.getLocalFilename(), fileSendDto.getData());
                                } else {
                                    throw new UnsupportedOperationException();
                                }
                                updateMessageData(fileSendDto.getFirstMessageId(), room.getRoomId(),
                                        SerializationUtils.serialize(fileLocalDto), DataType.FILE);
                            } else {
                                throw new UnsupportedOperationException();
                            }
                        }
                    }

                } else {
                    throw new UnsupportedOperationException();
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

    public Message updateMessageData(long messageId, long roomId, byte[] dataBytes, DataType dataType) {
        var message = getById(messageId, roomId);
        message.setDataType(dataType);
        message.setData(dataBytes);
        return messageRepository.save(message);
    }

    public Message getById(long messageId, long roomId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        return messageRepository.findById(new MessageId(messageId, roomId))
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }
}
