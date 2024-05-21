package edu.example.springmvcdemo.dto.message;

import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenKeyExchangeDto implements Serializable {
    private String openKey;
    private EncryptionPayload encryptionPayload;
}
