package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.encryption.RC5WordLengthBytes;
import edu.example.springmvcdemo.dto.room.RoomDto;
import edu.example.springmvcdemo.dto.room.RoomForm;
import edu.example.springmvcdemo.model.EncryptionType;
import edu.example.springmvcdemo.model.RoomStatus;
import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.RoomService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.round_key.CamelliaKeyGenerator;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Controller
@Slf4j
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final Validator validator;

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
            roomService.createRoom(roomForm.getParticipantUsername(), new EncryptionPayload(roomForm.getEncryptionType(), encryptionPayload));
        } catch (Exception ex) {
            model.addAttribute("encryptionTypes", EncryptionType.values());
            model.addAttribute("camelliaKeySizes", CamelliaKeyGenerator.CamelliaKeySize.values());
            model.addAttribute("rc5WordLengths", RC5WordLengthBytes.values());
            model.addAttribute("errors", ex.getMessage());
            return "createRoom";
        }

        return "redirect:/rooms"; // Перенаправление на список комнат после успешного создания
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