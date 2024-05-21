package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.RestClientConfig;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.message.OpenKeyExchangeDto;
import edu.example.springmvcdemo.dto.rest_dto.room.CreateRoomResponseDto;
import edu.example.springmvcdemo.dto.rest_dto.room.UserRoomDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
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
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        restClientConfig.getRestClient().post()
                .uri(restClientConfig.getUri("/room/agree/" + roomId))
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve();

        var encryption = new DiffieHellmanEncryption();
        var openKey = encryption.generateSecretAndOpenKey();

        messageService.sendMessage(roomId, new OpenKeyExchangeDto(openKey.toString(), null), null);

        room.setKey(encryption.getSecret().toString());
        room.setStatus(RoomStatus.CREATED);
        roomRepository.save(room);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteRoom(long roomId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        restClientConfig.getRestClient().delete()
                .uri(restClientConfig.getUri("/room/" + roomId))
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve();

        roomRepository.delete(room);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Room createRoom(String participantUsername, EncryptionPayload encryptionPayload) {
        var result = restClientConfig.getRestClient().post()
                .uri(restClientConfig.getUri("/room/" + participantUsername))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Cookie", userSessionService.getAccessCookieString())
                .retrieve()
                .toEntity(CreateRoomResponseDto.class);

        var roomId = result.getBody().getRoomId();

        var room = new Room();
        room.setStatus(RoomStatus.CREATED);
        room.setRoomId(roomId);
        room.setParticipantUsername(participantUsername);
        room.setEncryptionPayload(SerializationUtils.serialize(encryptionPayload));
        return roomRepository.save(room);
    }
}
