package org.example.encryption.symmetric.encryptor;

import org.example.encryption.symmetric.SymmetricEncryption;
import org.example.encryption.symmetric.mode.Cbc;
import org.example.encryption.symmetric.mode.Ecb;
import org.example.encryption.symmetric.mode.Mode;
import java.io.File;
import java.security.SecureRandom;
import java.util.concurrent.*;

public class SymmetricEncryptor {
    private final SymmetricEncryption encryption;
    private final Mode mode;
    private final Padding padding;
    private byte[] initVector;

    public SymmetricEncryptor(SymmetricEncryption encryption, Mode mode, Padding padding, byte[] initVector) {
        this.encryption = encryption;
        this.mode = mode;
        this.padding = padding;
        this.initVector = initVector;
    }

    public SymmetricEncryptor(SymmetricEncryption encryption, Mode mode, Padding padding) {
        this(encryption, mode, padding, null);
        initVector = this.generateInitVector(encryption.getSupportedArrayLen());
    }

    public byte[] encrypt(byte[] data) {
        // TODO: padding
        var dataBlocks = parseToBlocks(data);
        switch (mode) {
            case ECB -> {
                var threadPool = this.getThreadPool();
                var result = new Ecb(encryption).process(dataBlocks, true, threadPool);
                threadPool.shutdownNow();
                return result;
            }
            case CBC -> {
                var mode = new Cbc(encryption);
                mode.setPreviousBlock(initVector);
                return mode.encrypt(dataBlocks);
            }
            default -> throw new UnsupportedOperationException();
        }
    }

    public byte[] decrypt(byte[] data) {
        var dataBlocks = parseToBlocks(data);
        switch (mode) {
            case ECB -> {
                var threadPool = this.getThreadPool();
                var result = new Ecb(encryption).process(dataBlocks, false, threadPool);
                threadPool.shutdownNow();
                return result;
            }
            case CBC -> {
                var threadPool = this.getThreadPool();
                var mode = new Cbc(encryption);
                mode.setPreviousBlock(initVector);
                var result = mode.process(dataBlocks, false, threadPool);
                threadPool.shutdownNow();
                return result;
            }
            default -> throw new UnsupportedOperationException();
        }
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
