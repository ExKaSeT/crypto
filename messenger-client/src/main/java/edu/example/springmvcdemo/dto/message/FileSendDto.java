package edu.example.springmvcdemo.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileSendDto implements Serializable {
    private long firstMessageId;
    private byte[] data;
    private FileStatus status;
    private Long currentBytes;
}
