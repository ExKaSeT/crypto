package edu.example.springmvcdemo.dto.messages_kafka;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageDto {
    private Long roomId;
    private Long messageId;
    private String receiverUsername;
}
