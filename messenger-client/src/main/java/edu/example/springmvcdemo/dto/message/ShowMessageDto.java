package edu.example.springmvcdemo.dto.message;

import edu.example.springmvcdemo.dto.room.RoomDto;
import edu.example.springmvcdemo.model.*;
import edu.example.springmvcdemo.service.SerializationUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.Base64;

import static java.util.Objects.nonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowMessageDto {
    private Long id;

    private RoomDto room;

    private Timestamp timestamp;

    private Boolean isMine;

    private DataType dataType;

    private String text;

    private String imageBytes;

    private boolean isImage;

    private String imageExtension;

    private String filename;

    public static ShowMessageDto fromMessage(Message message) {
        var dto = new ShowMessageDto();
        dto.setId(message.getMessageId().getId());
        dto.setRoom(RoomDto.fromRoom(message.getRoom()));
        dto.setTimestamp(message.getTimestamp());
        dto.setIsMine(message.getIsMine());
        dto.setDataType(message.getDataType());
        switch (message.getDataType()) {
            case STRING -> dto.setText(new String(message.getData()));
            case FILE -> {
                // TODO:
                var fileInfo = (FileLocalDto) SerializationUtils.deserialize(message.getData());
                var extension = ImageExtension.isImage(fileInfo.getFilename());
                if (nonNull(extension)) {
                    dto.setImage(true);
                    dto.setImageExtension(extension.name().toLowerCase());
                    dto.setImageBytes(Base64.getEncoder().encodeToString(fileInfo.getData()));
                } else {
                    dto.setFilename(fileInfo.getFilename());
                }
            }
            default -> throw new IllegalStateException("Can't process data type " + message.getDataType());
        }
        return dto;
    }
}
