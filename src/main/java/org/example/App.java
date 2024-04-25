package org.example;

import org.example.encryption.symmetric.CamelliaEncryption;
import org.example.encryption.symmetric.SymmetricEncryption;
import org.example.encryption.symmetric.encryptor.Padding;
import org.example.encryption.symmetric.encryptor.SymmetricEncryptor;
import org.example.encryption.symmetric.mode.Mode;
import org.example.round_key.CamelliaKeyGenerator;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class App {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        var encryption = new CamelliaEncryption(CamelliaKeyGenerator.CamelliaKeySize.KEY256);
        var key = new byte[]{100, 65, -50, 30, 90, 1, -55, 100, 100, 65, -50, 30, 90, 1, -55, 100, 100, 65, -50, 30, 90, 1, -55, 100, 100, 65, -50, 30, 90, 1, -55, 100};

//        var encryption = new DealEncryption(KEY256);
//        var key = new byte[]{100, 65, -50, 30, 90, 1, -55, 100, 100, 65, -50, 30, 90, 1, -55, 100, 100, 65, -50, 30, 90, 1, -55, 100, 100, 65, -50, 30, 90, 1, -55, 100};

//        var key = new byte[] {100, 65, -50, 30, 90, 1, -55, 100};
//        var encryption = new DesEncryption();

        encryption.generateRoundKeys(key);
//        var arr1 = new byte[] {100, 65, -50, 1, -100, 1, 55};

        var mode = Mode.OFB;
        var padding = Padding.ISO10126;
        var file = Path.of("C:/Users/kek/Desktop/homiak.gif").toFile();

        var encryptor = new SymmetricEncryptor(encryption, mode, padding);
        var enc = encryptor.encryptAsync(file).get();
        var dec = encryptor.decryptAsync(enc).get();
        System.out.println(file.length() == dec.length());
        enc.delete();
        dec.delete();


//        var initVector = encryptor.getInitialVector();
//        System.out.println(Arrays.toString(arr1));
//        var res = encryptor.decrypt(encryptor.encrypt(arr1));
//        System.out.println(Arrays.toString(res));
//        System.out.println(Arrays.equals(res, arr1));

        encryptor.close();
    }

    public static byte[][] parseToBlocks(byte[] data, SymmetricEncryption encryption) {
        var blockLen = encryption.getBlockLenBytes();
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
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
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
