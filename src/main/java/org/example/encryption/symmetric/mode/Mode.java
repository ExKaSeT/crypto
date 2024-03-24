package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;

public enum Mode {
    ECB,
    CBC,
    PCBC,
    CFB,
    OFB,
    CTR,
    RANDOM_DELTA;

    public EncryptionMode getImpl(SymmetricEncryption encryption, boolean isEncrypt, byte[] initialVector) {
        switch (this) {
            case ECB -> {
                return new Ecb(encryption, isEncrypt);
            }
            case CBC -> {
                return new Cbc(encryption, isEncrypt, initialVector);
            }
            case PCBC -> {
                return new Pcbc(encryption, isEncrypt, initialVector);
            }
            case CFB -> {
                return new Cfb(encryption, isEncrypt, initialVector);
            }
            case OFB -> {
                return new Ofb(encryption, isEncrypt, initialVector);
            }
            default -> throw new UnsupportedOperationException();
        }
    }
}
