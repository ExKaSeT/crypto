package edu.example.springmvcdemo.security.method;

import edu.example.springmvcdemo.model.RoomUser;
import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("roomSecurity")
public class RoomSecurity {

    private final RoomService roomService;

    public boolean isAllowedToModifyRoom(Authentication auth, Long roomId) {
        var roomUsers = roomService.getRoomUsers(roomId);
        var user = ((UserDetailsImpl) auth.getPrincipal()).getUser();
        return roomUsers.stream().anyMatch(roomUser -> roomUser.getUser().equals(user));
    }

    public boolean isAllowedToSendMessage(Authentication auth, Long roomId) {
        var roomUsers = roomService.getRoomUsers(roomId);
        var user = ((UserDetailsImpl) auth.getPrincipal()).getUser();

        return roomUsers.stream().anyMatch(roomUser -> roomUser.getUser().equals(user)) &&
                roomUsers.stream().allMatch(RoomUser::isAgreed);
    }
}
