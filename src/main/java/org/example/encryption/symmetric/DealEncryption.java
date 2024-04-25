package org.example.encryption.symmetric;

import org.example.encryption_converter.DealEncryptionConverter;
import org.example.round_key.DealKeyGenerator;

public class DealEncryption extends FeistelCipher {

    private final DealKeyGenerator.DealKeySize keySize;

    public DealEncryption(DealKeyGenerator.DealKeySize keySize) {
        super(new DealEncryptionConverter(), new DealKeyGenerator(keySize));
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
