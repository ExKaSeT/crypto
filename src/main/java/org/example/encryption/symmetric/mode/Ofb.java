package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.concurrent.ExecutorService;

public class Ofb extends EncryptionMode {

    /**
     * Requires different objects for encryption / decryption; saves state
     * */
    public Ofb(SymmetricEncryption encryption, boolean isEncrypt, byte[] initialVector) {
        super(encryption, isEncrypt, initialVector);
    }

    @Override
    public byte[] process(byte[][] dataBlocks, ExecutorService threadPool) {
        var result = new byte[dataBlocks.length][];
        int index = 0;
        var initVector = this.initialVector;
        for (var block : dataBlocks) {
            var encrypted = encryption.encrypt(initVector);
            var xored = blockXor(encrypted, block);
            result[index] = xored;
            index++;
            initVector = encrypted;
        }
        this.initialVector = initVector;
        return unpackBlocks(result);
    }
}
