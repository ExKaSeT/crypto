package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Cfb extends Cbc {

    /**
     * Requires different objects for encryption / decryption; saves state
     * */
    public Cfb(SymmetricEncryption encryption, boolean isEncrypt, byte[] initialVector) {
        super(encryption, isEncrypt, initialVector);
    }

    protected byte[] encrypt(byte[][] dataBlocks) {
        var result = new byte[dataBlocks.length][];
        int index = 0;
        var initVector = this.initialVector;
        for (var block : dataBlocks) {
            var initVectorEncrypted = encryption.encrypt(initVector);
            var xored = blockXor(block, initVectorEncrypted);
            result[index] = xored;
            index++;
            initVector = xored;
        }
        this.initialVector = initVector;
        return unpackBlocks(result);
    }

    protected byte[] decrypt(byte[][] dataBlocks, ExecutorService threadPool) {
        List<Callable<byte[]>> tasks = new ArrayList<>();
        for (int i = 0; i < dataBlocks.length; i++) {
            byte[] initVector;
            if (i == 0) {
                initVector = this.initialVector;
            }  else {
                initVector = dataBlocks[i - 1];
            }
            Callable<byte[]> task = () -> encryption.encrypt(initVector);
            tasks.add(task);
        }

        var encrypted = new byte[dataBlocks.length][];

        try {
            int index = 0;
            for (var future : threadPool.invokeAll(tasks)) {
                encrypted[index] = future.get();
                index++;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        var result = new byte[dataBlocks.length][];
        for (int i = 0; i < dataBlocks.length; i++) {
            var plaintext = blockXor(dataBlocks[i], encrypted[i]);
            result[i] = plaintext;
        }

        this.initialVector = dataBlocks[dataBlocks.length - 1];

        return unpackBlocks(result);
    }
}
