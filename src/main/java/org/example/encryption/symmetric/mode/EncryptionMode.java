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
