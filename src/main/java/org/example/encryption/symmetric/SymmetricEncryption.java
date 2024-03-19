package org.example.encryption.symmetric;

public interface SymmetricEncryption {
    byte[] encrypt(byte[] data);

    byte[] decrypt(byte[] data);

    void generateRoundKeys(byte[] key);
}
