package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.message.ShowMessageDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;

    @GetMapping("/rooms/{roomId}/messages/{pullFromIdExcluded}")
    public List<ShowMessageDto> pullMessages(@PathVariable Long roomId, @PathVariable Long pullFromIdExcluded) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        return messageRepository
                .getMessagesByRoomAndMessageId_IdGreaterThan(room, pullFromIdExcluded)
                .stream().map(ShowMessageDto::fromMessage).toList();
    }
}
