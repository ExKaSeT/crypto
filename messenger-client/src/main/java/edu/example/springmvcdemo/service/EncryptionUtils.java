package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dto.encryption.CamelliaPayload;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.encryption.RC5Payload;
import edu.example.springmvcdemo.dto.encryption.RC5WordLengthBytes;
import org.example.encryption.symmetric.CamelliaEncryption;
import org.example.encryption.symmetric.RC5_32Encryption;
import org.example.encryption.symmetric.RC5_64Encryption;
import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.Arrays;

public interface EncryptionUtils {

    static SymmetricEncryption getEncryption(EncryptionPayload payload, String key) {
        switch (payload.getEncryptionType()) {
            case CAMELLIA -> {
                var camelliaKeySize = ((CamelliaPayload) payload.getPayload()).getCamelliaKeySize();
                var encryption = new CamelliaEncryption(camelliaKeySize);
                var keyBytes = Arrays.copyOf(key.getBytes(), camelliaKeySize.getSizeBytes());
                encryption.generateRoundKeys(keyBytes);
                return encryption;
            }
            case RC5 -> {
                var rc5Info = (RC5Payload) payload.getPayload();
                var keyBytes = Arrays.copyOf(key.getBytes(), rc5Info.getKeyLength());
                var encryption = rc5Info.getWordLengthBytes().equals(RC5WordLengthBytes.WORD_32) ?
                        new RC5_32Encryption(rc5Info.getRoundCount()) : new RC5_64Encryption(rc5Info.getRoundCount());
                encryption.generateRoundKeys(keyBytes);
                return encryption;
            }
            default -> throw new IllegalStateException("Can't process encryption type: " + payload.getEncryptionType());
        }
    }
}