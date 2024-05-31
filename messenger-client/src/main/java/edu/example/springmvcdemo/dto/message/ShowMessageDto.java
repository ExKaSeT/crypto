package edu.example.springmvcdemo.dto.message;

import edu.example.springmvcdemo.dto.room.RoomDto;
import edu.example.springmvcdemo.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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

    private boolean isChanging;
}
