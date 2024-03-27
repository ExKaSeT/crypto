package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import org.example.util.EncryptionUtil;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Pcbc extends Cbc {

    /**
     * Requires different objects for encryption / decryption; saves state
     * */
    public Pcbc(SymmetricEncryption encryption, boolean isEncrypt, byte[] initialVector) {
        super(encryption, isEncrypt, initialVector);
    }

    @Override
    protected byte[] encrypt(byte[][] dataBlocks) {
        var result = new byte[dataBlocks.length][];
        int index = 0;
        var plainXoredCipherText = this.initialVector;
        for (var block : dataBlocks) {
            var xored = EncryptionUtil.blockXor(block, plainXoredCipherText);
            var encrypted = encryption.encrypt(xored);
            result[index] = encrypted;
            index++;
            plainXoredCipherText = EncryptionUtil.blockXor(block, encrypted);
        }
        this.initialVector = plainXoredCipherText;
        return unpackBlocks(result);
    }

    @Override
    protected byte[] decrypt(byte[][] dataBlocks, ExecutorService threadPool) {
        List<Callable<byte[]>> tasks =  Arrays.stream(dataBlocks)
                .map(block -> (Callable<byte[]>) () -> encryption.decrypt(block))
                .toList();

        var decrypted = new byte[dataBlocks.length][];

        try {
            int index = 0;
            for (var future : threadPool.invokeAll(tasks)) {
                decrypted[index] = future.get();
                index++;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        var result = new byte[dataBlocks.length][];
        var plainXoredCipherText = this.initialVector;
        for (int i = 0; i < decrypted.length; i++) {
            var plaintext = EncryptionUtil.blockXor(decrypted[i], plainXoredCipherText);
            result[i] = plaintext;
            plainXoredCipherText = EncryptionUtil.blockXor(plaintext, dataBlocks[i]);
        }

        this.initialVector = plainXoredCipherText;

        return unpackBlocks(result);
    }
}
