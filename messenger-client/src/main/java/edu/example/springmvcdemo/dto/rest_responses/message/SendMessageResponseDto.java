package edu.example.springmvcdemo.dto.rest_responses.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMessageResponseDto {
    private Long messageId;
}
