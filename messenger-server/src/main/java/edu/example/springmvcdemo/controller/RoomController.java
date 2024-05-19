package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.room.CreateRoomResponseDto;
import edu.example.springmvcdemo.dto.room.UserRoomDto;
import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.RoomService;
import edu.example.springmvcdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final UserService userService;

    @PostMapping("/{participantUsername}")
    public CreateRoomResponseDto create(@PathVariable String participantUsername, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var participant = userService.getUserByUsername(participantUsername);
        var room = roomService.createRoom(userDetails.getUser(), participant);
        return new CreateRoomResponseDto(room.getId());
    }

    @PostMapping("/agree/{roomId}")
    @PreAuthorize("@roomSecurity.isAllowedToModifyRoom(authentication, #roomId)")
    public void agree(@PathVariable Long roomId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        roomService.setUserAgreedStatus(userDetails.getUser(), roomId, true);
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("@roomSecurity.isAllowedToModifyRoom(authentication, #roomId)")
    public void delete(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
    }

    @GetMapping("/available")
    public List<UserRoomDto> getUserRooms(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.getAllUserRooms(userDetails.getUser());
    }
}