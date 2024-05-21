package edu.example.springmvcdemo.dto.rest_dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private Long roomId;
    private Long messageId;
    private Long epochTime;
    private byte[] data;
}
