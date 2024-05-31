package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dto.message.FileLocalDto;
import edu.example.springmvcdemo.dto.message.ShowMessageDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.DataType;
import edu.example.springmvcdemo.model.MessageId;
import edu.example.springmvcdemo.model.Room;
import edu.example.springmvcdemo.service.MessageService;
import edu.example.springmvcdemo.service.RoomService;
import edu.example.springmvcdemo.service.SerializationUtils;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class MessageController {

    private final RoomService roomService;
    private final MessageService messageService;
    private final MessageRepository messageRepository;

    @GetMapping("/{roomId}/messages/{pullFromIdExcluded}")
    public List<ShowMessageDto> pullMessages(@PathVariable Long roomId, @PathVariable Long pullFromIdExcluded) {
        Room room = roomService.getById(roomId);

        return messageRepository
                .getMessagesByRoomAndMessageId_IdGreaterThan(room, pullFromIdExcluded)
                .stream().map(ShowMessageDto::fromMessage).toList();
    }

    @GetMapping("/{roomId}/message/{messageId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long roomId, @PathVariable Long messageId) {
        var message = messageRepository.findById(new MessageId(messageId, roomId))
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        if (!message.getDataType().equals(DataType.FILE)) {
            throw new AccessDeniedException("Object is not FILE");
        }
        // TODO:
        var fileDto = (FileLocalDto) SerializationUtils.deserialize(message.getData());

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileDto.getData()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        URLEncoder.encode(fileDto.getFilename(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/{roomId}/send")
    public String sendMessage(@PathVariable Long roomId, HttpServletRequest request) throws IOException {
        Room room = roomService.getById(roomId);
        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            throw new UnsupportedOperationException("Supports only multipart request");
        }

        JakartaServletFileUpload upload = new JakartaServletFileUpload();
        FileItemInputIterator iterStream = upload.getItemIterator(request);
        while (iterStream.hasNext()) {
            FileItemInput item = iterStream.next();
            var formName = SendMessageFormName.getIgnoreCase(item.getFieldName());
            if (isNull(formName)) {
                throw new UnsupportedOperationException("Can't parse field: " + item.getFieldName());
            }

            InputStream stream = item.getInputStream();
            switch (formName) {
                case TEXT -> {
                    String text = IOUtils.toString(stream, StandardCharsets.UTF_8);
                    if (nonNull(text) && !text.isBlank()) {
                        messageService.sendTextEncrypted(room, text);
                    }
                }
                case FILE -> {
                    var filename = item.getName();
                    if (nonNull(filename) && !filename.isBlank()) {

                    }
                }
            }
        }


        if (nonNull(file) && nonNull(file.getOriginalFilename()) && !file.getOriginalFilename().isBlank()) {
            messageService.sendFileEncrypted(room, file);
        }
        return "redirect:/rooms/" + roomId;
    }

    enum SendMessageFormName {
        TEXT,
        FILE;

        @Nullable
        public static SendMessageFormName getIgnoreCase(String name) {
            for (var value : SendMessageFormName.values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }
    }
}
