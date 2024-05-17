package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dao.RoomUserRepository;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.Room;
import edu.example.springmvcdemo.model.RoomUser;
import edu.example.springmvcdemo.model.RoomUserId;
import edu.example.springmvcdemo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomUserRepository roomUserRepository;

    public Room getById(long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
    }

    public Room createRoom(User creator, User participant) {
        var room = new Room();
        room.setDeleted(false);
        room.setLastMessageId(0L);

        var creatorInfo = new RoomUser();
        creatorInfo.setRoom(room);
        creatorInfo.setUser(creator);
        creatorInfo.setMessageOffset(0L);
        creatorInfo.setAgreed(true);

        var participantInfo = new RoomUser();
        participantInfo.setRoom(room);
        participantInfo.setUser(participant);
        participantInfo.setMessageOffset(0L);
        participantInfo.setAgreed(false);

        return roomRepository.save(room);
    }

    public void setUserAgreedStatus(User user, long roomId, boolean agreed) {
        var userRoomInfo = roomUserRepository.findById(new RoomUserId(roomId, user.getUsername()))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Can't find user %s in room %d", user.getUsername(), roomId)));
        userRoomInfo.setAgreed(agreed);
        roomUserRepository.save(userRoomInfo);
    }

    public void deleteRoom(long roomId) {
        roomRepository.deleteById(roomId);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long getNextMessageIdInRoom(long roomId) {
        var room = getById(roomId);
        long messageId = room.getLastMessageId() + 1;
        room.setLastMessageId(messageId);
        roomRepository.save(room);
        return messageId;
    }
}
