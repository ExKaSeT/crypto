package edu.example.springmvcdemo.dto.rest_dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoomDto {
    private Long roomId;
    private Boolean agreed;
    private String participant;
    private Boolean participantAgreed;
}
