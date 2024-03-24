package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.concurrent.ExecutorService;

public abstract class EncryptionMode {

    protected final SymmetricEncryption encryption;
    protected final boolean isEncrypt;
    protected byte[] initialVector;

    public EncryptionMode(SymmetricEncryption encryption, boolean isEncrypt, byte[] initialVector) {
        this.encryption = encryption;
        this.isEncrypt = isEncrypt;
        this.initialVector = initialVector;
    }

    abstract public byte[] process(byte[][] dataBlocks, ExecutorService threadPool);

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

    /**
     * Blocks must be same size
     * */
    protected byte[] unpackBlocks(byte[][] data) {
        int blockLen = data[0].length;
        var result = new byte[data.length * blockLen];
        int index = 0;
        for (var block : data) {
            System.arraycopy(block, 0, result, index, blockLen);
            index += blockLen;
        }
        return result;
    }
}
