package edu.example.springmvcdemo.dto.rest_responses.room;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRoomDto {
    private Long roomId;
    private Boolean agreed;
    private String participant;
    private Boolean participantAgreed;
}