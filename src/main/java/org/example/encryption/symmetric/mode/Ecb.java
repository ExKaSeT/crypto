package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Ecb extends EncryptionMode {

    public Ecb(SymmetricEncryption encryption, boolean isEncrypt) {
        super(encryption, isEncrypt, null);
    }

    @Override
    public byte[] process(byte[][] dataBlocks,ExecutorService threadPool) {
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
