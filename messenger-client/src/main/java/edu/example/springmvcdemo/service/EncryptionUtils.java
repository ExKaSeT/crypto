package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dto.encryption.CamelliaPayload;
import edu.example.springmvcdemo.dto.encryption.EncryptionPayload;
import edu.example.springmvcdemo.dto.encryption.RC5Payload;
import edu.example.springmvcdemo.dto.encryption.RC5WordLengthBytes;
import org.example.encryption.symmetric.CamelliaEncryption;
import org.example.encryption.symmetric.RC5_32Encryption;
import org.example.encryption.symmetric.RC5_64Encryption;
import org.example.encryption.symmetric.SymmetricEncryption;
import org.example.encryption.symmetric.encryptor.SymmetricEncryptor;

import java.security.SecureRandom;
import java.util.Arrays;
import static java.util.Objects.isNull;

public interface EncryptionUtils {

    static SymmetricEncryptor getEncryption(EncryptionPayload payload, String key) {
        SymmetricEncryption symmetricEncryption;
        byte[] keyBytes;
        switch (payload.getEncryptionType()) {
            case CAMELLIA -> {
                var camelliaKeySize = ((CamelliaPayload) payload.getPayload()).getCamelliaKeySize();
                symmetricEncryption = new CamelliaEncryption(camelliaKeySize);
                keyBytes = Arrays.copyOf(key.getBytes(), camelliaKeySize.getSizeBytes());
            }
            case RC5 -> {
                var rc5Info = (RC5Payload) payload.getPayload();
                keyBytes = Arrays.copyOf(key.getBytes(), rc5Info.getKeyLength());
                symmetricEncryption = rc5Info.getWordLengthBytes().equals(RC5WordLengthBytes.WORD_32) ?
                        new RC5_32Encryption(rc5Info.getRoundCount()) : new RC5_64Encryption(rc5Info.getRoundCount());
            }
            default -> throw new IllegalStateException("Can't process encryption type: " + payload.getEncryptionType());
        }
        symmetricEncryption.generateRoundKeys(keyBytes);
        if (isNull(payload.getInitialVector())) {
            return new SymmetricEncryptor(symmetricEncryption, payload.getMode(), payload.getPadding());
        }
        return new SymmetricEncryptor(symmetricEncryption, payload.getMode(), payload.getPadding(),
                payload.getInitialVector());
    }

    static byte[] generateInitVector(EncryptionPayload payload) {
        int lengthBytes;
        switch (payload.getEncryptionType()) {
            case CAMELLIA -> {
                var camelliaKeySize = ((CamelliaPayload) payload.getPayload()).getCamelliaKeySize();
                lengthBytes = new CamelliaEncryption(camelliaKeySize).getBlockLenBytes();
            }
            case RC5 -> {
                var rc5Info = (RC5Payload) payload.getPayload();
                lengthBytes = rc5Info.getWordLengthBytes().equals(RC5WordLengthBytes.WORD_32) ?
                        new RC5_32Encryption(rc5Info.getRoundCount()).getBlockLenBytes() :
                        new RC5_64Encryption(rc5Info.getRoundCount()).getBlockLenBytes();
            }
            default -> throw new IllegalStateException("Can't process encryption type: " + payload.getEncryptionType());
        }
        SecureRandom random = new SecureRandom();
        var result = new byte[lengthBytes];
        random.nextBytes(result);
        return result;
    }
}