package edu.example.springmvcdemo.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileLocalDto implements Serializable {
    private String filename; // original
    private String localFilename;
    private FileStatus status;
    private Long sizeBytes;
}
