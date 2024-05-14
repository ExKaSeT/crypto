package org.example.encryption.symmetric;

public class RC5_32Encryption implements SymmetricEncryption {

    private final int roundCount;
    private int[] S;
    private static final int P32 = 0xb7e15163;
    private static final int Q32 = 0x9e3779b9;
    private static final int DATA_LENGTH_BYTES = 8;

    public RC5_32Encryption(int roundCount) {
        if (roundCount < 1 || roundCount > 255) {
            throw new IllegalArgumentException("Round count must be 1-255");
        }
        this.roundCount = roundCount;
    }

    @Override
    public byte[] encrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        int A = bytesToWord(data, 0) + S[0];
        int B = bytesToWord(data, 4) + S[1];

        for (int i = 1; i <= roundCount; i++) {
            A = rotateLeft(A ^ B, B) + S[2 * i];
            B = rotateLeft(B ^ A, A) + S[2 * i + 1];
        }

        var result = new byte[DATA_LENGTH_BYTES];

        wordToBytes(A, result, 0);
        wordToBytes(B, result, 4);

        return result;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        int A = bytesToWord(data, 0);
        int B = bytesToWord(data, 4);

        for (int i = roundCount; i >= 1; i--) {
            B = rotateRight(B - S[2 * i + 1], A) ^ A;
            A = rotateRight(A - S[2 * i], B) ^ B;
        }

        var result = new byte[DATA_LENGTH_BYTES];

        wordToBytes(A - S[0], result, 0);
        wordToBytes(B - S[1], result, 4);

        return result;
    }

    @Override
    public void generateRoundKeys(byte[] key) {
        if (key.length < 1 || key.length > 255) {
            throw new IllegalArgumentException("Key length must be 1-255 bytes");
        }

        var L = new int[(key.length + (4 - 1)) / 4];

        for (int i = 0; i != key.length; i++) {
            L[i / 4] += (key[i] & 0xff) << (8 * (i % 4));
        }

        S = new int[2 * (roundCount + 1)];

        S[0] = P32;
        for (int i = 1; i < S.length; i++) {
            S[i] = (S[i - 1] + Q32);
        }

        int iter;
        if (L.length > S.length) {
            iter = 3 * L.length;
        } else {
            iter = 3 * S.length;
        }

        int A = 0, B = 0;
        int i = 0, j = 0;

        for (int k = 0; k < iter; k++) {
            A = S[i] = rotateLeft(S[i] + A + B, 3);
            B = L[j] = rotateLeft(L[j] + A + B, A + B);
            i = (i + 1) % S.length;
            j = (j + 1) % L.length;
        }
    }

    @Override
    public int getBlockLenBytes() {
        return DATA_LENGTH_BYTES;
    }

    private int rotateLeft(int value, int bitCount) {
        return ((value << (bitCount & (32 - 1))) | (value >>> (32 - (bitCount & (32 - 1)))));
    }

    private int rotateRight(int value, int bitCount) {
        return ((value >>> (bitCount & (32 - 1))) | (value << (32 - (bitCount & (32 - 1)))));
    }

    private int bytesToWord(byte[] src, int srcOff) {
        return (src[srcOff] & 0xff) | ((src[srcOff + 1] & 0xff) << 8)
                | ((src[srcOff + 2] & 0xff) << 16) | ((src[srcOff + 3] & 0xff) << 24);
    }

    private void wordToBytes(int word, byte[] dst, int dstOff) {
        dst[dstOff] = (byte) word;
        dst[dstOff + 1] = (byte) (word >> 8);
        dst[dstOff + 2] = (byte) (word >> 16);
        dst[dstOff + 3] = (byte) (word >> 24);
    }
}
