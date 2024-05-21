package edu.example.springmvcdemo.dto.encryption;

import edu.example.springmvcdemo.model.EncryptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptionPayload implements Serializable {
    private EncryptionType encryptionType;
    private Object payload;
}
