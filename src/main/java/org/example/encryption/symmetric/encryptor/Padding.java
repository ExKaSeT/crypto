package org.example.encryption.symmetric.encryptor;

import java.security.SecureRandom;

public enum Padding {
    ZEROES,
    ANSI_X_923,
    PKCS7,
    ISO10126;

    public byte[] add(byte[] data, int blockLenBytes) {
        switch (this) {
            case ZEROES -> {
                int addToLength = data.length % blockLenBytes == 0 ? 0 : blockLenBytes - data.length % blockLenBytes;
                var result = new byte[data.length + addToLength];
                System.arraycopy(data, 0, result, 0, data.length);
                return result;
            }
            case ANSI_X_923 -> {
                return addAnsiX923Padding(data, blockLenBytes);
            }
            case PKCS7 -> {
                var result = addAnsiX923Padding(data, blockLenBytes);
                int paddingLength = result.length - data.length;
                for (int i = result.length - paddingLength; i < result.length; i++) {
                    result[i] = (byte) paddingLength;
                }
                return result;
            }
            case ISO10126 -> {
                var result = addAnsiX923Padding(data, blockLenBytes);
                int paddingLength = result.length - data.length;
                SecureRandom random = new SecureRandom();
                var randomBytes = new byte[paddingLength - 1];
                random.nextBytes(randomBytes);
                System.arraycopy(randomBytes, 0, result, result.length - paddingLength, randomBytes.length);
                return result;
            }
        }
        throw new UnsupportedOperationException();
    }

    public byte[] remove(byte[] data) {
        switch (this) {
            case ZEROES -> {
                int paddingIndex = data.length - 1;
                for (; paddingIndex > 0; paddingIndex--) {
                    if (data[paddingIndex] != 0) {
                        paddingIndex++;
                        break;
                    }
                }
                var result = new byte[paddingIndex];
                System.arraycopy(data, 0, result, 0, paddingIndex);
                return result;
            }
            case ANSI_X_923, PKCS7, ISO10126 -> {
                int paddingLength = this.byteToUnsigned(data[data.length - 1]);
                int paddingIndex = data.length - paddingLength;
                var result = new byte[paddingIndex];
                System.arraycopy(data, 0, result, 0, paddingIndex);
                return result;
            }
        }
        throw new UnsupportedOperationException();
    }

    private byte[] addAnsiX923Padding(byte[] data, int blockLenBytes) {
        if (blockLenBytes > 255) {
            throw new UnsupportedOperationException("Max block len - 255");
        }
        int needMinLength = data.length + 1;
        int addToLength = needMinLength % blockLenBytes == 0 ? 0 : blockLenBytes - needMinLength % blockLenBytes;
        var result = new byte[needMinLength + addToLength];
        System.arraycopy(data, 0, result, 0, data.length);
        int paddingLength = result.length - data.length;
        result[result.length - 1] = (byte) paddingLength;
        return result;
    }

    private int byteToUnsigned(byte b) {
        return b & 255;
    }


}
