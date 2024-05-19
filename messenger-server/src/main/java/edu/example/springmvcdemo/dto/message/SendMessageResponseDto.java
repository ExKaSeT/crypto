package edu.example.springmvcdemo.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMessageResponseDto {
    private Long messageId;
}
