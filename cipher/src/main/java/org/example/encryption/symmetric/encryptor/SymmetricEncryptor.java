package org.example.encryption.symmetric.encryptor;

import org.example.encryption.symmetric.SymmetricEncryption;
import org.example.encryption.symmetric.mode.Mode;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.concurrent.*;

import static java.util.Objects.nonNull;

public class SymmetricEncryptor implements AutoCloseable {
    private static final String ENCRYPTED_SUB_PATH_NAME = "encrypted";
    private static final String DECRYPTED_SUB_PATH_NAME = "decrypted";
    private static final int MAX_BYTE_BUFFER_LENGTH = 10_000;
    private final int byteBufferLength;
    private final SymmetricEncryption encryption;
    private final Mode mode;
    private final Padding padding;
    private final byte[] initialVector;
    private final ExecutorService threadPool;

    public SymmetricEncryptor(SymmetricEncryption encryption, Mode mode, Padding padding, byte[] initialVector) {
        this.encryption = encryption;
        this.byteBufferLength = MAX_BYTE_BUFFER_LENGTH - MAX_BYTE_BUFFER_LENGTH % encryption.getBlockLenBytes();
        if (encryption.getBlockLenBytes() >= byteBufferLength) {
            throw new IllegalArgumentException();
        }
        this.mode = mode;
        this.padding = padding;
        this.initialVector = initialVector.clone();
        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public SymmetricEncryptor(SymmetricEncryption encryption, Mode mode, Padding padding) {
        this(encryption, mode, padding, generateInitVector(encryption.getBlockLenBytes()));
    }

    public byte[] encrypt(byte[] data) {
        var withPadding = padding.add(data, encryption.getBlockLenBytes());
        var dataBlocks = parseToBlocks(withPadding);
        var modeProcessor = mode.getImpl(encryption, true, initialVector);
        return modeProcessor.process(dataBlocks, this.threadPool);
    }

    public byte[] decrypt(byte[] data) {
        var dataBlocks = parseToBlocks(data);
        var modeProcessor = mode.getImpl(encryption, false, initialVector);
        var decrypted = modeProcessor.process(dataBlocks, this.threadPool);
        return padding.remove(decrypted);
    }

    public byte[] getInitialVector() {
        return this.initialVector.clone();
    }

    public File encrypt(File data) throws IOException {
        File output = createFileInSubPath(data, ENCRYPTED_SUB_PATH_NAME);
        if (byteBufferLength % encryption.getBlockLenBytes() != 0) {
            throw new IllegalStateException();
        }

        try (FileInputStream inputStream = new FileInputStream(data);
             FileOutputStream outputStream = new FileOutputStream(output)) {

            writeInitialVector(this.initialVector, outputStream);

            var modeProcessor = mode.getImpl(encryption, true, initialVector);
            byte[] buffer = new byte[byteBufferLength];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead != byteBufferLength) {
                    var dataBytes = new byte[bytesRead];
                    System.arraycopy(buffer, 0, dataBytes, 0, bytesRead);
                    var withPadding = padding.add(dataBytes, encryption.getBlockLenBytes());
                    var encrypted = modeProcessor.process(parseToBlocks(withPadding), this.threadPool);
                    outputStream.write(encrypted);
                    break;
                }
                var encrypted = modeProcessor.process(parseToBlocks(buffer), this.threadPool);
                outputStream.write(encrypted);
            }
            if (bytesRead == -1) {
                var withPadding = padding.add(new byte[] {}, encryption.getBlockLenBytes());
                var encrypted = modeProcessor.process(parseToBlocks(withPadding), this.threadPool);
                outputStream.write(encrypted);
            }
        }

        return output;
    }

    public File decrypt(File data) throws IOException {
        File output = createFileInSubPath(data, DECRYPTED_SUB_PATH_NAME);
        if (byteBufferLength % encryption.getBlockLenBytes() != 0) {
            throw new IllegalStateException();
        }

        try (FileInputStream inputStream = new FileInputStream(data);
             FileOutputStream outputStream = new FileOutputStream(output)) {

            var initialVector = readInitialVector(inputStream);

            var modeProcessor = mode.getImpl(encryption, false, initialVector);
            byte[] buffer = new byte[byteBufferLength];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead != byteBufferLength) {
                    var dataBytes = new byte[bytesRead];
                    System.arraycopy(buffer, 0, dataBytes, 0, bytesRead);
                    var decrypted = modeProcessor.process(parseToBlocks(dataBytes), this.threadPool);
                    var withoutPadding = padding.remove(decrypted);
                    outputStream.write(withoutPadding);
                    break;
                }
                var decrypted = modeProcessor.process(parseToBlocks(buffer), this.threadPool);
                outputStream.write(decrypted);
            }
            if (bytesRead == -1) {
                throw new IllegalStateException();
            }
        }

        return output;
    }

    public Future<File> encryptAsync(File file) {
        return threadPool.submit(() -> this.encrypt(file));
    }

    public Future<File> decryptAsync(File file) {
        return threadPool.submit(() -> this.decrypt(file));
    }

    private void writeInitialVector(byte[] vector, FileOutputStream outputStream) throws IOException {
        var dataBlocks = parseToBlocks(vector);
        var modeProcessor = mode.getImpl(encryption, true, new byte[encryption.getBlockLenBytes()]);
        outputStream.write(modeProcessor.process(dataBlocks, this.threadPool));
    }

    private byte[] readInitialVector(FileInputStream inputStream) throws IOException {
        byte[] vector = new byte[encryption.getBlockLenBytes()];
        int bytesRead = inputStream.read(vector);
        if (bytesRead != vector.length) {
            throw new IOException("Incorrect file");
        }
        var modeProcessor = mode.getImpl(encryption, false, new byte[encryption.getBlockLenBytes()]);
        return modeProcessor.process(parseToBlocks(vector), this.threadPool);
    }

    private File createFileInSubPath(File original, String subPath) throws IOException {
        var directoryPath = Files.createDirectories(Path.of(original.getParent(), subPath));
        var file = Path.of(directoryPath.toString(), original.getName()).toFile();
        if (!file.createNewFile()) {
            throw new FileAlreadyExistsException("File '" + file.getAbsolutePath() + "' already exists");
        }
        return file;
    }

    private static byte[] generateInitVector(int lengthBytes) {
        SecureRandom random = new SecureRandom();
        var result = new byte[lengthBytes];
        random.nextBytes(result);
        return result;
    }

    private byte[][] parseToBlocks(byte[] data) {
        int blockLen = encryption.getBlockLenBytes();
        if (data.length % blockLen != 0) {
            throw new IllegalArgumentException();
        }

        var dataBlocks = new byte[data.length / blockLen][];
        for (int i = 0; i < dataBlocks.length; i++) {
            var block = new byte[blockLen];
            System.arraycopy(data, i * blockLen, block, 0, blockLen);
            dataBlocks[i] = block;
        }
        return dataBlocks;
    }

    @Override
    public void close() {
        if (nonNull(this.threadPool)) {
            this.threadPool.shutdownNow();
        }
    }
}
