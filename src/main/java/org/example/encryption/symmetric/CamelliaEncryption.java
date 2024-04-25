package org.example.encryption.symmetric;

import org.example.encryption_converter.CamelliaEncryptionConverter;
import org.example.round_key.CamelliaKeyGenerator;

public class CamelliaEncryption extends FeistelCipher {

    private final CamelliaKeyGenerator.CamelliaKeySize keySize;

    public CamelliaEncryption(CamelliaKeyGenerator.CamelliaKeySize keySize) {
        super(new CamelliaEncryptionConverter(), new CamelliaKeyGenerator(keySize));
        this.keySize = keySize;
    }


    @Override
    public byte[] encrypt(byte[] data) {
        if (data.length != keySize.getSizeBytes()) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        return super.encrypt(data);
    }

    @Override
    public byte[] decrypt(byte[] data) {
        if (data.length != keySize.getSizeBytes()) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        return super.decrypt(data);
    }

    @Override
    public int getBlockLenBytes() {
        return keySize.getSizeBytes();
    }
}
