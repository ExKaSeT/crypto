package edu.example.springmvcdemo.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDto implements Serializable {
    private String filename;
    private byte[] data;
}
