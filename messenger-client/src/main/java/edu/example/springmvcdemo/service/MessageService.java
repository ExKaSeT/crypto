package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.RestClientConfig;
import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.message.MessageDto;
import edu.example.springmvcdemo.dto.message.OpenKeyExchangeDto;
import edu.example.springmvcdemo.dto.rest_dto.message.MessageResponseDto;
import edu.example.springmvcdemo.dto.rest_dto.message.SendMessageRequestDto;
import edu.example.springmvcdemo.dto.rest_dto.message.SendMessageResponseDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.DataType;
import edu.example.springmvcdemo.model.Message;
import edu.example.springmvcdemo.model.MessageId;
import edu.example.springmvcdemo.model.RoomStatus;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.encryption.assymetric.DiffieHellmanEncryption;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RoomRepository roomRepository;
    private final RestClientConfig restClientConfig;
    private final UserSessionService userSessionService;
    private final MessageRepository messageRepository;

    public void sendMessage(long roomId, Object data, @Nullable DataType dataType) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        var dataBytes = SerializationUtils.serialize(data);
        var result = restClientConfig.getRestClient().post()
                .uri(restClientConfig.getUri("/message/send"))
                .body(new SendMessageRequestDto(roomId, dataBytes))
                .accept(MediaType.APPLICATION_JSON)
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve()
                .toEntity(SendMessageResponseDto.class);

        if (isNull(dataType)) {
            return;
        }

        var messageId = result.getBody().getMessageId();
        var message = new Message();
        message.setMessageId(new MessageId(messageId, roomId));
        message.setRoom(room);
        message.setData(dataBytes);
        message.setDataType(dataType);
        messageRepository.save(message);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void pullMessages() {
        var result = restClientConfig.getRestClient().get()
                .uri(restClientConfig.getUri("/message/pull"))
                .header("Cookie", userSessionService.getAccessCookieString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<MessageResponseDto>>() {});

        var messages = result.getBody();
        assert messages != null;

        for (var message : messages) {
            try {
                var payload = SerializationUtils.deserialize(message.getData());
                var room = roomRepository.findById(message.getRoomId()).orElse(null);
                if (payload instanceof OpenKeyExchangeDto) {
                    if (isNull(room)) {
                        log.info("Message ignored. Room not exists: " + message);
                        continue;
                    }
                    var openKeyDto = (OpenKeyExchangeDto) payload;
                    var openKey = new BigInteger(openKeyDto.getOpenKey());
                    var encryption = new DiffieHellmanEncryption();

                    if (isNull(openKeyDto.getEncryptionPayload())) { // participant agreed
                        var myOpenKey = encryption.generateSecretAndOpenKey();
                        var sharedSecret = encryption.generateSharedSecret(openKey).toString();
                        room.setKey(sharedSecret);
                        room.setStatus(RoomStatus.AGREED);
                        var encryptionInfo = (EncryptionPayload) SerializationUtils.deserialize(room.getEncryptionPayload());
                        sendMessage(room.getRoomId(), new OpenKeyExchangeDto(myOpenKey.toString(), encryptionInfo), null);
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
                } else if (payload instanceof MessageDto) {
                    // TODO:
                    continue;
                }

            } catch (Exception ex) {
                log.error("Can't process message: " + message + ex.getMessage());
            }
        }
    }
}
