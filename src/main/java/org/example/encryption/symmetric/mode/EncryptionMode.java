package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.concurrent.ExecutorService;

public abstract class EncryptionMode {

    protected final SymmetricEncryption encryption;

    public EncryptionMode(SymmetricEncryption encryption) {
        this.encryption = encryption;
    }

    abstract public byte[] process(byte[][] dataBlocks, boolean isEncrypt, ExecutorService threadPool);

    protected byte[] blockXor(byte[] block1, byte[] block2) {
        if (block1.length != block2.length) {
            throw new IllegalArgumentException();
        }
        var result = new byte[block1.length];
        for (int i = 0; i < block1.length; i++) {
            result[i] = (byte) (block1[i] ^ block2[i]);
        }
        return result;
    }
}
