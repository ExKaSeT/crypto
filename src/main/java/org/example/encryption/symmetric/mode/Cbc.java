package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.Objects.isNull;

public class Cbc extends EncryptionMode {

    /**
     * Requires different objects for encryption / decryption; saves state
     * */
    public Cbc(SymmetricEncryption encryption, boolean isEncrypt, byte[] initialVector) {
        super(encryption, isEncrypt, initialVector);
        if (isNull(this.initialVector)) {
            throw new IllegalStateException("Previous block not initialized");
        }
    }

    @Override
    public byte[] process(byte[][] dataBlocks, ExecutorService threadPool) {
        if (this.isEncrypt) {
            return encrypt(dataBlocks);
        } else {
            if (isNull(threadPool)) {
                throw new IllegalArgumentException("Thread pool not initialized");
            }
            return decrypt(dataBlocks, threadPool);
        }
    }

    protected byte[] encrypt(byte[][] dataBlocks) {
        var result = new byte[dataBlocks.length][];
        int index = 0;
        var prevBlock = this.initialVector;
        for (var block : dataBlocks) {
            var xored = blockXor(block, prevBlock);
            var encrypted = encryption.encrypt(xored);
            result[index] = encrypted;
            index++;
            prevBlock = encrypted;
        }
        this.initialVector = prevBlock;
        return unpackBlocks(result);
    }

    protected byte[] decrypt(byte[][] dataBlocks, ExecutorService threadPool) {
        List<Callable<byte[]>> tasks = new ArrayList<>();
        for (int i = 0; i < dataBlocks.length; i++) {
            byte[] prevBlock;
            if (i == 0) {
                prevBlock = this.initialVector;
            }  else {
                prevBlock = dataBlocks[i - 1];
            }
            int finalI = i;
            Callable<byte[]> task = () -> blockXor(encryption.decrypt(dataBlocks[finalI]), prevBlock);
            tasks.add(task);
        }

        this.initialVector = dataBlocks[dataBlocks.length - 1];

        try {
            var result = new byte[dataBlocks.length][];
            int index = 0;
            for (var future : threadPool.invokeAll(tasks)) {
                result[index] = future.get();
                index++;
            }
            return unpackBlocks(result);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
