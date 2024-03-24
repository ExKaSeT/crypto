package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.Objects.isNull;

public class Ecb extends EncryptionMode {

    public Ecb(SymmetricEncryption encryption, boolean isEncrypt) {
        super(encryption, isEncrypt, null);
    }

    @Override
    public byte[] process(byte[][] dataBlocks,ExecutorService threadPool) {
        if (isNull(threadPool)) {
            throw new IllegalArgumentException("Thread pool not initialized");
        }

        List<Callable<byte[]>> tasks =  Arrays.stream(dataBlocks)
                .map(block -> (Callable<byte[]>) () -> {
                    if (this.isEncrypt) {
                        return encryption.encrypt(block);
                    } else {
                        return encryption.decrypt(block);
                    }
                })
                .toList();
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
