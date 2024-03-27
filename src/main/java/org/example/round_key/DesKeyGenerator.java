package org.example.round_key;

import org.example.util.Permutation;
import static org.example.util.Permutation.DirectionRule.LEAST_TO_MOST;
import static org.example.util.Permutation.IndexRule.FROM_1;

public class DesKeyGenerator implements RoundKeyGenerator {

    private static final int[] PC1_1 = {
            57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36
    };

    private static final int[] PC1_2 = {
            63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4
    };

    private static final int[] PC2 = {
            14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32
    };

    private static final int KEY_LENGTH_BYTES = 8;

    @Override
    public byte[][] generate(byte[] key) {
        if (key.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect key length");
        }

        var keys = new byte[16][];

        var block1 = Permutation.permute(key, PC1_1, LEAST_TO_MOST, FROM_1);
        var block2 = Permutation.permute(key, PC1_2, LEAST_TO_MOST, FROM_1);

        for (int i = 1; i <= 16; i++) {
            int shiftCount = 2;
            if (i == 1 || i == 2 || i == 9 || i == 16) {
                shiftCount = 1;
            }
            block1 = leftShift28bit(block1, shiftCount == 1);
            block2 = leftShift28bit(block2, shiftCount == 1);
            var block = merge28to56bit(block1, block2);
            block = Permutation.permute(block, PC2, LEAST_TO_MOST, FROM_1);
            keys[i - 1] = block;
        }
        return keys;
    }

    @Override
    public int getKeyLenBytes() {
        return KEY_LENGTH_BYTES;
    }

    private byte[] leftShift28bit(final byte[] data, boolean isOneShift) {
        var result = new byte[data.length];
        int handler12 = isOneShift ? 1 : 3; // ... 0/1 1
        int handler4 = 15; // ... 0 0 0 0 1 1 1 1
        int swapBits = (data[0] >> (isOneShift ? 7 : 6)) & handler12;
        int i = 0;
        for (; i < result.length - 1; i++) {
            result[i] = (byte) ((data[i] << (isOneShift ? 1 : 2)) | ((data[i + 1] >> (isOneShift ? 7 : 6) & handler12)));
        }
        // 0 0 0 0 x x x/0 0 - where 'x' is data; x/0 - depends on shift count
        byte lastByteShifted = (byte) (((data[i] << (isOneShift ? 1 : 2)) >> (4 + (isOneShift ? 1 : 2)) << (isOneShift ? 1 : 2)) & handler4);
        result[i] = (byte) ((lastByteShifted | swapBits) << 4);
        return result;
    }

    private byte[] merge28to56bit(final byte[] block1, final byte[] block2) {
        var result = new byte[7];
        System.arraycopy(block1, 0, result, 0,4);
        int handler40 = 240; // ... 1 1 1 1 0 0 0 0
        result[3] = (byte) (result[3] | (byte) ((block2[0] & handler40) >> 4));
        int handler4 = 15; // ... 0 0 0 0 1 1 1 1
        for (int i = 4; i < result.length; i++) {
            int block2Index = i - 4;
            result[i] = (byte) (((block2[block2Index] & handler4) << 4) | ((block2[block2Index + 1] & handler40) >> 4));
        }
        return result;
    }
}
