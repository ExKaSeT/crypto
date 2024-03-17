package org.example.encryption.symmetric.encryptor;

import java.io.File;

public class SymmetricEncryptor {
    public enum Mode {
        ECB,
        CBC,
        PCBC,
        CFB,
        OFB,
        CTR,
        RANDOM_DELTA
    }

    public enum Padding {
        ZEROES,
        ANSI_X_923,
        PKCS7,
        ISO10126
    }

    public byte[] encrypt(byte[] input) {
        return new byte[0];
    }

    public File encrypt(File input) {
        return null;
    }

    public byte[] decrypt(byte[] input) {
        return new byte[0];
    }

    public File decrypt(File input) {
        return null;
    }
}
