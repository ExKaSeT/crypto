package org.example.util;

public class Permutation {
    public enum DirectionRule {
        LEAST_TO_MOST,
        MOST_TO_LEAST,
    }

    public enum IndexRule {
        FROM_0,
        FROM_1,
    }

    public static byte[] permute(final byte[] data, final int[] rule, DirectionRule direction, IndexRule indexRule) {
        int bitsLength = rule.length;
        byte[] result = new byte[bitsLength / 8 + (bitsLength % 8 == 0 ? 0 : 1)];

        for (int i = 0; i < bitsLength; i++) {
            int index = indexRule == IndexRule.FROM_0 ? rule[i] : rule[i] - 1;
            int byteIndex = index / 8;
            int shiftCount = 7 - (index % 8);
            byte bit = (byte) ((data[byteIndex] >> shiftCount) & 1);
            if (direction == DirectionRule.LEAST_TO_MOST) {
                result[i / 8] |= (byte) (bit << (7 - (i % 8)));
            } else {
                int reverseIndex = bitsLength - i - 1;
                result[reverseIndex / 8] |= (byte) (bit << (7 - (reverseIndex % 8)));
            }
        }
        return result;
    }
}
