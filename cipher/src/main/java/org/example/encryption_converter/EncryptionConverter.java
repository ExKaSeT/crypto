package org.example.encryption_converter;

public interface EncryptionConverter {
    byte[] convert(byte[] data, byte[] roundKey);
}
