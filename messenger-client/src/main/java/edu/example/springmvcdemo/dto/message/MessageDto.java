package edu.example.springmvcdemo.dto.message;

import edu.example.springmvcdemo.model.DataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto implements Serializable {
    private DataType dataType;
    private byte[] data;
}
