package org.example.encryption.symmetric;

import lombok.RequiredArgsConstructor;
import org.example.encryption_converter.EncryptionConverter;
import org.example.round_key.RoundKeyGenerator;

@RequiredArgsConstructor
public class FeistelCipher implements SymmetricEncryption {
    private final EncryptionConverter converter;
    private final RoundKeyGenerator keyGenerator;

    @Override
    public byte[] encrypt(byte[] data) {
        return new byte[0];
    }
}
