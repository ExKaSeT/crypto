package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.message.MessageResponseDto;
import edu.example.springmvcdemo.dto.message.SendMessageRequestDto;
import edu.example.springmvcdemo.dto.message.SendMessageResponseDto;
import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.MessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessagingService messagingService;

    @PostMapping("/send")
    @PreAuthorize("@roomSecurity.isAllowedToSendMessage(authentication, #requestDto.roomId)")
    public SendMessageResponseDto send(@RequestBody SendMessageRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return messagingService.sendMessage(userDetails.getUser(), requestDto.getRoomId(), requestDto.getData());
    }

    @GetMapping("/pull")
    public List<MessageResponseDto> pullMessages(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return messagingService.getMessages(userDetails.getUser());
    }
}