package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.encryption.RC5WordLengthBytes;
import edu.example.springmvcdemo.dto.message.FileDto;
import edu.example.springmvcdemo.dto.message.MessageForm;
import edu.example.springmvcdemo.dto.room.RoomDto;
import edu.example.springmvcdemo.dto.room.RoomForm;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.*;
import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.MessageService;
import edu.example.springmvcdemo.service.RoomService;
import edu.example.springmvcdemo.service.SerializationUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.encryption.symmetric.encryptor.Padding;
import org.example.encryption.symmetric.mode.Mode;
import org.example.round_key.CamelliaKeyGenerator;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Controller
@Slf4j
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final Validator validator;
    private final MessageService messageService;
    private final MessageRepository messageRepository;

    @GetMapping("{roomId}/message/{messageId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long roomId, @PathVariable Long messageId) {
        var message = messageRepository.findById(new MessageId(messageId, roomId))
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        if (!message.getDataType().equals(DataType.FILE)) {
            throw new AccessDeniedException("Object is not FILE");
        }
        var fileDto = (FileDto) SerializationUtils.deserialize(message.getData());

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileDto.getData()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        URLEncoder.encode(fileDto.getFilename(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/{roomId}/send")
    public String sendMessage(@PathVariable Long roomId, @ModelAttribute MessageForm messageForm, @RequestParam(value = "file", required = false) MultipartFile file) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        if (nonNull(messageForm) && nonNull(messageForm.getText()) && !messageForm.getText().isBlank()) {
            messageService.sendTextEncrypted(room, messageForm.getText());
        }
        if (nonNull(file) && nonNull(file.getOriginalFilename()) && !file.getOriginalFilename().isBlank()) {
            messageService.sendFileEncrypted(room, file);
        }
        return "redirect:/rooms/" + roomId;
    }

    @GetMapping("/{roomId}")
    public String getRoom(@PathVariable Long roomId, Model model) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        model.addAttribute("room", room);
        model.addAttribute("newMessage", new MessageForm());
        return "room";
    }

    @GetMapping
    public String getRooms(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var createdRooms = roomRepository.getRoomsByStatus(RoomStatus.CREATED)
                .stream().map(RoomDto::fromRoom).toList();
        var toAgreeRooms = roomRepository.getRoomsByStatus(RoomStatus.TO_AGREE)
                .stream().map(RoomDto::fromRoom).toList();
        var agreedRooms = roomRepository.getRoomsByStatus(RoomStatus.AGREED)
                .stream().map(RoomDto::fromRoom).toList();

        model.addAttribute("username", userDetails.getUser().getUsername());
        model.addAttribute("createdRooms", createdRooms);
        model.addAttribute("toAgreeRooms", toAgreeRooms);
        model.addAttribute("agreedRooms", agreedRooms);

        return "rooms";
    }

    @GetMapping("/create")
    public String showCreateRoomForm(Model model) {
        model.addAttribute("roomForm", new RoomForm());
        model.addAttribute("modes", Mode.values());
        model.addAttribute("paddings", Padding.values());
        model.addAttribute("encryptionTypes", EncryptionType.values());
        model.addAttribute("camelliaKeySizes", CamelliaKeyGenerator.CamelliaKeySize.values());
        model.addAttribute("rc5WordLengths", RC5WordLengthBytes.values());
        return "createRoom";
    }

    @PostMapping("/create")
    public String createRoom(@Valid RoomForm roomForm, BindingResult bindingResult, Model model) {
        var encryptionPayload = roomForm.getEncryptionType()
                .equals(EncryptionType.CAMELLIA) ? roomForm.getCamelliaPayload() : roomForm.getRc5Payload();
        var modeValidErrors = isNull(encryptionPayload) ?
                List.of("Необходимо заполнить параметры шифрования") :
                validator.validate(encryptionPayload, Default.class).stream()
                        .map(ConstraintViolation::getMessage).toList();
        if (bindingResult.hasErrors() || !modeValidErrors.isEmpty()) {
            model.addAttribute("modes", Mode.values());
            model.addAttribute("paddings", Padding.values());
            model.addAttribute("encryptionTypes", EncryptionType.values());
            model.addAttribute("camelliaKeySizes", CamelliaKeyGenerator.CamelliaKeySize.values());
            model.addAttribute("rc5WordLengths", RC5WordLengthBytes.values());
            var errors = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage).collect(Collectors.toList());
            errors.addAll(modeValidErrors);
            model.addAttribute("errors", errors);
            return "createRoom";
        }

        try {
            roomService.createRoom(roomForm.getParticipantUsername(), roomForm);
        } catch (Exception ex) {
            model.addAttribute("modes", Mode.values());
            model.addAttribute("paddings", Padding.values());
            model.addAttribute("encryptionTypes", EncryptionType.values());
            model.addAttribute("camelliaKeySizes", CamelliaKeyGenerator.CamelliaKeySize.values());
            model.addAttribute("rc5WordLengths", RC5WordLengthBytes.values());
            model.addAttribute("errors", ex.getMessage());
            return "createRoom";
        }

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