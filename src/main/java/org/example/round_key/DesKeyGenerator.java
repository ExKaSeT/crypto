package org.example.round_key;

import org.example.util.Permutation;
import static org.example.util.Permutation.DirectionRule.LEAST_TO_MOST;
import static org.example.util.Permutation.IndexRule.FROM_1;

public class DesKeyGenerator implements RoundKeyGenerator {

    private final static int[] PC1_1 = {
            57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36
    };

    private final static int[] PC1_2 = {
            63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4
    };

    private final static int[] PC2 = {
            14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32
    };

    @Override
    public byte[][] generate(byte[] key) {
        if (key.length != 8) {
            throw new IllegalArgumentException("Incorrect key length");
        }

        var block1 = Permutation.permute(key, PC1_1, LEAST_TO_MOST, FROM_1);
        var block2 = Permutation.permute(key, PC1_2, LEAST_TO_MOST, FROM_1);

        for (int i = 1; i <= 16; i++) {
            int shiftCount = 2;
            if (i == 1 || i == 2 || i == 9 || i == 16) {
                shiftCount = 1;
            }


        }

        return new byte[0][];
    }

    public static byte[] leftShift28bit(final byte[] data, boolean isOneShift) {
        var result = new byte[data.length];
        int handler = isOneShift ? 1 : 3;
        int swapBits = (data[0] >> (isOneShift ? 7 : 6)) & handler;
        int i = 0;
        for (; i < result.length - 1; i++) {
            result[i] = (byte) ((data[i] << (isOneShift ? 1 : 2)) | ((data[i + 1] >> (isOneShift ? 7 : 6) & handler)));
        }
        // 0 0 0 0 x x x/0 0 - where 'x' is data; x/0 - depends on shift count
        byte lastByteShifted = (byte) ((data[i] << (isOneShift ? 1 : 2)) >> (4 + (isOneShift ? 1 : 2)) << (isOneShift ? 1 : 2));
        result[i] = (byte) ((lastByteShifted | swapBits) << 4);
        return result;
    }
}
