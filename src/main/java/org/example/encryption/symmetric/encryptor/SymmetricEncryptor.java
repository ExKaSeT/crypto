package org.example.encryption.symmetric.encryptor;

import org.example.encryption.symmetric.SymmetricEncryption;
import org.example.encryption.symmetric.mode.Mode;
import java.io.File;
import java.security.SecureRandom;
import java.util.concurrent.*;

public class SymmetricEncryptor {
    private final SymmetricEncryption encryption;
    private final Mode mode;
    private final Padding padding;
    private byte[] initialVector;

    public SymmetricEncryptor(SymmetricEncryption encryption, Mode mode, Padding padding, byte[] initialVector) {
        this.encryption = encryption;
        this.mode = mode;
        this.padding = padding;
        this.initialVector = initialVector;
    }

    public SymmetricEncryptor(SymmetricEncryption encryption, Mode mode, Padding padding) {
        this(encryption, mode, padding, null);
        initialVector = this.generateInitVector(encryption.getSupportedArrayLen());
    }

    public byte[] encrypt(byte[] data) {
        // TODO: padding
        var dataBlocks = parseToBlocks(data);
        var modeProcessor = mode.getImpl(encryption, true, initialVector);
        var threadPool = this.getThreadPool();
        var result = modeProcessor.process(dataBlocks, threadPool);
        threadPool.shutdownNow();
        return result;
    }

    public byte[] decrypt(byte[] data) {
        var dataBlocks = parseToBlocks(data);
        var modeProcessor = mode.getImpl(encryption, false, initialVector);
        var threadPool = this.getThreadPool();
        var result = modeProcessor.process(dataBlocks, threadPool);
        threadPool.shutdownNow();
        return result;
    }

    public File encrypt(File data) {
        return null;
    }

    public File decrypt(File data) {
        return null;
    }

    private ExecutorService getThreadPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private byte[] generateInitVector(int lengthBytes) {
        SecureRandom random = new SecureRandom();
        var result = new byte[lengthBytes];
        random.nextBytes(result);
        return result;
    }

    private byte[][] parseToBlocks(byte[] data) {
        // TODO: padding
        var blockLen = encryption.getSupportedArrayLen();
        if (data.length % blockLen != 0) {
            throw new RuntimeException(); // TODO: remove
        }

        var dataBlocks = new byte[data.length / blockLen][];
        for (int i = 0; i < dataBlocks.length; i++) {
            var block = new byte[blockLen];
            System.arraycopy(data, i * blockLen, block, 0, blockLen);
            dataBlocks[i] = block;
        }
        return dataBlocks;
    }

    public enum Padding {
        ZEROES,
        ANSI_X_923,
        PKCS7,
        ISO10126
    }
}
