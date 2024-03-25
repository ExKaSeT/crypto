package org.example.encryption.symmetric.mode;

import org.example.encryption.symmetric.SymmetricEncryption;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class RandomDelta extends EncryptionMode {

    private BigInteger counter;
    private final BigInteger delta;

    /**
     * Requires different objects for encryption / decryption; saves state
     * */
    public RandomDelta(SymmetricEncryption encryption, boolean isEncrypt, byte[] initialVector) {
        super(encryption, isEncrypt, initialVector);
        counter = new BigInteger(initialVector);
        int deltaIndex = initialVector.length / 2;
        delta = new BigInteger(initialVector, deltaIndex, initialVector.length - deltaIndex);
    }

    @Override
    public byte[] process(byte[][] dataBlocks, ExecutorService threadPool) {
        List<Callable<byte[]>> tasks = new ArrayList<>();
        int maxBitLength = initialVector.length * 8;
        for (var block : dataBlocks) {
            var counterBytes = counter.toByteArray();
            Callable<byte[]> task = () -> blockXor(encryption.encrypt(counterBytes), block);
            tasks.add(task);
            counter = counter.add(delta);
            if (counter.bitLength() > maxBitLength) {
                counter = BigInteger.ZERO;
            }
        }

        var result = new byte[dataBlocks.length][];

        try {
            int index = 0;
            for (var future : threadPool.invokeAll(tasks)) {
                result[index] = future.get();
                index++;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return unpackBlocks(result);
    }
}
