package org.example.util;

public interface EncryptionUtil {
    static byte[] blockXor(byte[] block1, byte[] block2) {
        if (block1.length != block2.length) {
            throw new IllegalArgumentException();
        }
        var result = new byte[block1.length];
        for (int i = 0; i < block1.length; i++) {
            result[i] = (byte) (block1[i] ^ block2[i]);
        }
        return result;
    }

    static byte[] addMinorBytes(byte[] toExpand, int resultArrayLen) {
        if (toExpand.length > resultArrayLen) {
            throw new IllegalArgumentException();
        }
        var result = new byte[resultArrayLen];
        int needAddBytes = result.length - toExpand.length;
        System.arraycopy(toExpand, 0, result, needAddBytes, toExpand.length);
        return result;
    }
}
