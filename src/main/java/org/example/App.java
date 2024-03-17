package org.example;

import org.example.round_key.DesKeyGenerator;

public class App  {
    public static void main( String[] args ) {
//        byte[] arr = intToByteArray(923456789);
//        printByteArr(arr);
//        System.out.println(Arrays.toString(arr));
//        byte[] res = Permutation.permute(arr, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29}, Permutation.DirectionRule.LEAST_TO_MOST, Permutation.IndexRule.FROM_0);
//        System.out.println(Arrays.toString(res));
//        printByteArr(res);

        var arr = new byte[] {100, 65, -50, 30, 90, 1, 55, -100};
        new DesKeyGenerator().generate(arr);

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
