package edu.example.springmvcdemo.dto.encryption;

import edu.example.springmvcdemo.model.EncryptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.encryption.symmetric.encryptor.Padding;
import org.example.encryption.symmetric.mode.Mode;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptionPayload implements Serializable {
    private EncryptionType encryptionType;
    private Object payload;
    private Mode mode;
    private Padding padding;
    private byte[] initialVector;
}
