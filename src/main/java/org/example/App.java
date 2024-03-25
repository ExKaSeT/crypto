package org.example;

import org.example.encryption.symmetric.DesEncryption;
import org.example.encryption.symmetric.SymmetricEncryption;
import org.example.encryption.symmetric.encryptor.SymmetricEncryptor;
import org.example.encryption.symmetric.mode.Mode;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

public class App  {
    public static void main( String[] args ) {
//        byte[] arr = intToByteArray(923456789);
//        printByteArr(arr);
//        System.out.println(Arrays.toString(arr));
//        byte[] res = Permutation.permute(arr, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29}, Permutation.DirectionRule.LEAST_TO_MOST, Permutation.IndexRule.FROM_0);
//        System.out.println(Arrays.toString(res));
//        printByteArr(res);

        var encryption = new DesEncryption();
        var key = new byte[] {100, 65, -50, 30, 90, 1, -55, 100};
        encryption.generateRoundKeys(key);
        var arr1 = new byte[] {100, 65, -50, 1, -100, 1, 55, 100, 87, 88, 89, 90, 91, 92, 93, 94};

        var mode = Mode.RANDOM_DELTA;

        var encryptor = new SymmetricEncryptor(encryption, mode, SymmetricEncryptor.Padding.ZEROES);
        var initVector = encryptor.getInitialVector();
        System.out.println(Arrays.toString(arr1));
        var res = encryptor.decrypt(encryptor.encrypt(arr1));
        System.out.println(Arrays.toString(res));
        System.out.println(Arrays.equals(res, arr1));

        var modeImpl = mode.getImpl(encryption, true, initVector);
        var modeImplDecrypt = mode.getImpl(encryption, false, initVector);
        var dataBlocks = parseToBlocks(arr1, encryption);
        var block1 = modeImpl.process(new byte[][] {dataBlocks[0]}, ForkJoinPool.commonPool());
        var block2 = modeImpl.process(new byte[][] {dataBlocks[1]}, ForkJoinPool.commonPool());
        block1 = modeImplDecrypt.process(new byte[][] {block1}, ForkJoinPool.commonPool());
        block2 = modeImplDecrypt.process(new byte[][] {block2}, ForkJoinPool.commonPool());
        var res1 = new byte[arr1.length];
        System.arraycopy(block1, 0, res1, 0, block1.length);
        System.arraycopy(block2, 0, res1, block1.length, block2.length);
        System.out.println(Arrays.equals(res1, arr1));

        encryptor.close();
    }

    public static byte[][] parseToBlocks(byte[] data, SymmetricEncryption encryption) {
        var blockLen = encryption.getSupportedArrayLen();
        if (data.length % blockLen != 0) {
            throw new RuntimeException();
        }
        var dataBlocks = new byte[data.length / blockLen][];
        for (int i = 0; i < dataBlocks.length; i++) {
            var block = new byte[blockLen];
            System.arraycopy(data, i * blockLen, block, 0, blockLen);
            dataBlocks[i] = block;
        }
        return dataBlocks;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
    public static void printByteArr(byte[] arr) {
        for (byte b : arr) {
            for (int i = 7; i >= 0; i--) {
                System.out.print((b >> i) & 1);
                System.out.print(" ");
            }
        }
        System.out.println();
    }
}
