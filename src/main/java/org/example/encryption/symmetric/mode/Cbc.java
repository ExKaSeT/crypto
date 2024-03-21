package org.example.encryption.symmetric.mode;

import lombok.Setter;
import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.Objects.isNull;

public class Cbc extends EncryptionMode {

    @Setter
    private byte[] previousBlock;

    /**
     * Requires different objects for encryption / decryption; saves state
     * */
    public Cbc(SymmetricEncryption encryption) {
        super(encryption);
    }

    @Override
    public byte[] process(byte[][] dataBlocks, boolean isEncrypt, ExecutorService threadPool) {
        if (isNull(previousBlock)) {
            throw new IllegalStateException("Previous block not initialized");
        }
        if (isEncrypt) {
            return encryptBlocks(dataBlocks);
        } else {
            if (isNull(threadPool)) {
                throw new IllegalArgumentException("Thread pool not initialized");
            }
            return decrypt(dataBlocks, threadPool);
        }
    }

    public byte[] encrypt(byte[][] dataBlocks) {
        return this.process(dataBlocks, true, null);
    }

    private byte[] encryptBlocks(byte[][] dataBlocks) {
        var result = new byte[dataBlocks.length * dataBlocks[0].length];
        int blockLen = dataBlocks[0].length;
        int index = 0;
        var prevBlock = previousBlock;
        for (var block : dataBlocks) {
            var xored = blockXor(block, prevBlock);
            var encrypted = encryption.encrypt(xored);
            System.arraycopy(encrypted, 0, result, index, blockLen);
            index += blockLen;
            prevBlock = encrypted;
        }
        this.previousBlock = prevBlock;
        return result;
    }

    private byte[] decrypt(byte[][] dataBlocks, ExecutorService threadPool) {
        List<Callable<byte[]>> tasks = new ArrayList<>();
        for (int i = 0; i < dataBlocks.length; i++) {
            byte[] prevBlock;
            if (i == 0) {
                prevBlock = this.previousBlock;
            }  else {
                prevBlock = dataBlocks[i - 1];
            }
            int finalI = i;
            Callable<byte[]> task = () -> blockXor(encryption.decrypt(dataBlocks[finalI]), prevBlock);
            tasks.add(task);
        }
        try {
            int blockLen = dataBlocks[0].length;
            var result = new byte[dataBlocks.length * blockLen];
            int index = 0;
            for (var future : threadPool.invokeAll(tasks)) {
                System.arraycopy(future.get(), 0, result, index, blockLen);
                index += blockLen;
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
