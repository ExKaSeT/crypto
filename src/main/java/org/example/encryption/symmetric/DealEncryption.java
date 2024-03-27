package org.example.encryption.symmetric;

import org.example.encryption_converter.DealEncryptionConverter;
import org.example.round_key.DealKeyGenerator;

public class DealEncryption extends FeistelCipher {

    private static final int DATA_LENGTH_BYTES = 16;

    public DealEncryption(DealKeyGenerator keyGenerator) {
        super(new DealEncryptionConverter(), keyGenerator);
    }


    @Override
    public byte[] encrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        return super.encrypt(data);
    }

    @Override
    public byte[] decrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        return super.decrypt(data);
    }

    @Override
    public int getBlockLenBytes() {
        return DATA_LENGTH_BYTES;
    }
}
