package edu.example.springmvcdemo.dto.rest_dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageResponseDto {
    private Long messageId;
}
