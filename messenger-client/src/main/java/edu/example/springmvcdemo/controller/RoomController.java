package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.model.Room;
import edu.example.springmvcdemo.model.RoomStatus;
import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @GetMapping
    public String getRooms(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Room> createdRooms = roomRepository.getRoomsByStatus(RoomStatus.CREATED);
        List<Room> toAgreeRooms = roomRepository.getRoomsByStatus(RoomStatus.TO_AGREE);
        List<Room> agreedRooms = roomRepository.getRoomsByStatus(RoomStatus.AGREED);

        model.addAttribute("username", userDetails.getUser().getUsername());
        model.addAttribute("createdRooms", createdRooms);
        model.addAttribute("toAgreeRooms", toAgreeRooms);
        model.addAttribute("agreedRooms", agreedRooms);

        return "rooms";
    }

    @GetMapping("/create")
    public String createRoomForm() {
        // Вернуть представление с формой создания комнаты
        return "createRoom";
    }

    @PostMapping("/create")
    public String createRoom(Room room) {

        return "redirect:/rooms";
    }

    @PostMapping("/accept/{roomId}")
    @ResponseBody
    public void acceptRoom(@PathVariable Long roomId) {
        roomService.agreeRoom(roomId);
    }

    @DeleteMapping("/{roomId}")
    @ResponseBody
    public void deleteRoom(@PathVariable Long roomId) {
        try {
            roomService.deleteRoom(roomId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}