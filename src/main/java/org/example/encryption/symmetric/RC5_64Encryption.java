package org.example.encryption.symmetric;

public class RC5_64Encryption implements SymmetricEncryption {

    private final int roundCount;
    private long[] S;
    private static final long P64 = 0xb7e151628aed2a6bL;
    private static final long Q64 = 0x9e3779b97f4a7c15L;
    private static final int DATA_LENGTH_BYTES = 16;

    public RC5_64Encryption(int roundCount) {
        this.roundCount = roundCount;
    }

    @Override
    public byte[] encrypt(byte[] data) {
        long A = bytesToWord(data, 0) + S[0];
        long B = bytesToWord(data, 8) + S[1];

        for (int i = 1; i <= roundCount; i++)
        {
            A = rotateLeft(A ^ B, B) + S[2*i];
            B = rotateLeft(B ^ A, A) + S[2*i+1];
        }

        var result = new byte[DATA_LENGTH_BYTES];
        
        wordToBytes(A, result, 0);
        wordToBytes(B, result, 8);
        return result;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        long A = bytesToWord(data, 0);
        long B = bytesToWord(data, 8);

        for (int i = roundCount; i >= 1; i--)
        {
            B = rotateRight(B - S[2*i+1], A) ^ A;
            A = rotateRight(A - S[2*i],   B) ^ B;
        }

        var result = new byte[DATA_LENGTH_BYTES];
        
        wordToBytes(A - S[0], result, 0);
        wordToBytes(B - S[1], result, 8);

        return result;
    }

    @Override
    public void generateRoundKeys(byte[] key) {
        long[] L = new long[(key.length + (8 - 1)) / 8];

        for (int i = 0; i != key.length; i++) {
            L[i / 8] += (long) (key[i] & 0xFF) << (8 * (i % 8));
        }

        S = new long[2 * (roundCount + 1)];
        S[0] = P64;
        for (int i = 1; i < S.length; i++) {
            S[i] = (S[i - 1] + Q64);
        }

        int iter;
        if (L.length > S.length) {
            iter = 3 * L.length;
        } else {
            iter = 3 * S.length;
        }

        long A = 0, B = 0;
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

    private long rotateLeft(long value, long bitCount) {
        return ((value << (bitCount & (64 - 1))) | (value >>> (64 - (bitCount & (64 - 1)))));
    }

    private long rotateRight(long value, long bitCount) {
        return ((value >>> (bitCount & (64 - 1))) | (value << (64 - (bitCount & (64 - 1)))));
    }

    private long bytesToWord(byte[] src, int srcOff) {
        long word = 0;

        for (int i = 8 - 1; i >= 0; i--) {
            word = (word << 8) + (src[i + srcOff] & 0xFF);
        }

        return word;
    }

    private void wordToBytes(long word, byte[] dst, int dstOff) {
        for (int i = 0; i < 8; i++) {
            dst[i + dstOff] = (byte) word;
            word >>>= 8;
        }
    }
}
