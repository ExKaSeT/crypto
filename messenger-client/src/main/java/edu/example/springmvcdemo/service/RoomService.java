package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.RestClientConfig;
import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.message.FileLocalDto;
import edu.example.springmvcdemo.dto.message.OpenKeyExchangeDto;
import edu.example.springmvcdemo.dto.rest_dto.room.CreateRoomResponseDto;
import edu.example.springmvcdemo.dto.rest_dto.room.UserRoomDto;
import edu.example.springmvcdemo.dto.room.RoomForm;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.DataType;
import edu.example.springmvcdemo.model.EncryptionType;
import edu.example.springmvcdemo.model.Room;
import edu.example.springmvcdemo.model.RoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.encryption.assymetric.DiffieHellmanEncryption;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RestClientConfig restClientConfig;
    private final UserSessionService userSessionService;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final StorageService storageService;

    public Room getById(long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateRooms() {
        if (!userSessionService.isUserLoggedIn()) {
            return;
        }

        var result = restClientConfig.getRestClient().get()
                .uri(restClientConfig.getUri("/room/available"))
                .header("Cookie", userSessionService.getAccessCookieString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference <List<UserRoomDto>>() {});

        var rooms = result.getBody();
        assert rooms != null;
        var roomsId = rooms.stream().map(UserRoomDto::getRoomId).toList();
        var localRooms = roomRepository.findAll();
        var localRoomsMap = localRooms.stream()
                .collect(Collectors.toMap(Room::getRoomId, room -> room));

        for (var localRoom : localRooms) {
            if (!roomsId.contains(localRoom.getRoomId())) {
                roomRepository.delete(localRoom);
            }
        }

        for (var room : rooms) {
            var localRoom = localRoomsMap.get(room.getRoomId());
            if (isNull(localRoom)) {
                var newRoom = new Room();
                newRoom.setRoomId(room.getRoomId());
                newRoom.setStatus(RoomStatus.TO_AGREE);
                newRoom.setParticipantUsername(room.getParticipant());
                roomRepository.save(newRoom);
            }
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void agreeRoom(long roomId) {
        var room = getById(roomId);

        restClientConfig.getRestClient().post()
                .uri(restClientConfig.getUri("/room/agree/" + roomId))
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve();

        var encryption = new DiffieHellmanEncryption();
        var openKey = encryption.generateSecretAndOpenKey();

        messageService.sendMessage(room, new OpenKeyExchangeDto(openKey.toString(), null));

        room.setKey(encryption.getSecret().toString());
        room.setStatus(RoomStatus.CREATED);
        roomRepository.save(room);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteRoom(long roomId) {
        var room = getById(roomId);

        restClientConfig.getRestClient().delete()
                .uri(restClientConfig.getUri("/room/" + roomId))
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve();

        for (var message : messageRepository.findAllByRoom_RoomId(roomId)) {
            if (!message.getDataType().equals(DataType.FILE)) {
                continue;
            }
            var fileLocalDto = (FileLocalDto) SerializationUtils.deserialize(message.getData());
            storageService.deleteFile(fileLocalDto.getLocalFilename());
        }
        roomRepository.delete(room);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Room createRoom(String participantUsername, RoomForm roomForm) {
        var result = restClientConfig.getRestClient().post()
                .uri(restClientConfig.getUri("/room/" + participantUsername))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve()
                .toEntity(CreateRoomResponseDto.class);

        var roomId = result.getBody().getRoomId();

        var encryptionTypePayload = roomForm.getEncryptionType()
                .equals(EncryptionType.CAMELLIA) ? roomForm.getCamelliaPayload() : roomForm.getRc5Payload();
        var encryptionPayload = new EncryptionPayload(roomForm.getEncryptionType(), encryptionTypePayload,
                roomForm.getMode(), roomForm.getPadding(), null);
        encryptionPayload.setInitialVector(EncryptionUtils.generateInitVector(encryptionPayload));

        var room = new Room();
        room.setStatus(RoomStatus.CREATED);
        room.setRoomId(roomId);
        room.setParticipantUsername(participantUsername);
        room.setEncryptionPayload(SerializationUtils.serialize(encryptionPayload));
        return roomRepository.save(room);
    }
}
