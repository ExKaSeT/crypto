package org.example.encryption_converter;

import org.example.encryption.symmetric.DesEncryption;

public class DealEncryptionConverter implements EncryptionConverter {

    @Override
    public byte[] convert(byte[] data, byte[] roundKey) {
        DesEncryption desEncryption = new DesEncryption();
        desEncryption.generateRoundKeys(roundKey);
        return desEncryption.encrypt(data);
    }
}
