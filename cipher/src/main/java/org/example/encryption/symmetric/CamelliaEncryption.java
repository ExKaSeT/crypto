package org.example.encryption.symmetric;

import org.example.encryption_converter.CamelliaEncryptionConverter;
import org.example.round_key.CamelliaKeyGenerator;

import java.util.Arrays;

import static org.example.util.EncryptionUtil.byteArrayToLong;
import static org.example.util.EncryptionUtil.longToByteArray;

public class CamelliaEncryption implements SymmetricEncryption {

    private static final int DATA_LENGTH_BYTES = 16;
    private final CamelliaEncryptionConverter converter;
    private final CamelliaKeyGenerator keyGenerator;
    private byte[][] roundKeys;
    private long[] klSubkeys;
    private long[] kwSubkeys;

    public CamelliaEncryption(CamelliaKeyGenerator.CamelliaKeySize keySize) {
        this.converter = new CamelliaEncryptionConverter();
        this.keyGenerator = new CamelliaKeyGenerator(keySize);
    }

    @Override
    public byte[] encrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        long D1 = byteArrayToLong(Arrays.copyOf(data, 8));
        long D2 = byteArrayToLong(Arrays.copyOfRange(data, 8, 16));

        D1 = D1 ^ kwSubkeys[0];
        D2 = D2 ^ kwSubkeys[1];
        int klIndex = 0;
        for (int round = 0; round < roundKeys.length; round += 2) {
            if (round == 6 || round == 12 || round == 18) {
                D1 = FL(D1, klSubkeys[klIndex]);
                D2 = FLInv(D2, klSubkeys[klIndex + 1]);
                klIndex += 2;
            }
            D2 = D2 ^ byteArrayToLong(converter.convert(longToByteArray(D1), roundKeys[round]));
            D1 = D1 ^ byteArrayToLong(converter.convert(longToByteArray(D2), roundKeys[round + 1]));
        }
        D2 = D2 ^ kwSubkeys[2];
        D1 = D1 ^ kwSubkeys[3];

        var result = Arrays.copyOf(longToByteArray(D2), 16);
        System.arraycopy(longToByteArray(D1), 0, result, 8, 8);

        return result;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        long D1 = byteArrayToLong(Arrays.copyOf(data, 8));
        long D2 = byteArrayToLong(Arrays.copyOfRange(data, 8, 16));

        D1 = D1 ^ kwSubkeys[2];
        D2 = D2 ^ kwSubkeys[3];
        int klIndex = klSubkeys.length - 1;
        for (int round = 0; round < roundKeys.length; round += 2) {
            if (round == 6 || round == 12 || round == 18) {
                D1 = FL(D1, klSubkeys[klIndex]);
                D2 = FLInv(D2, klSubkeys[klIndex - 1]);
                klIndex -= 2;
            }
            int reverseRound = roundKeys.length - round - 1;
            D2 = D2 ^ byteArrayToLong(converter.convert(longToByteArray(D1), roundKeys[reverseRound]));
            D1 = D1 ^ byteArrayToLong(converter.convert(longToByteArray(D2), roundKeys[reverseRound - 1]));
        }
        D2 = D2 ^ kwSubkeys[0];
        D1 = D1 ^ kwSubkeys[1];

        var result = Arrays.copyOf(longToByteArray(D2), 16);
        System.arraycopy(longToByteArray(D1), 0, result, 8, 8);

        return result;
    }

    @Override
    public void generateRoundKeys(byte[] key) {
        this.roundKeys = keyGenerator.generate(key);
        this.klSubkeys = keyGenerator.getKlSubkeys();
        this.kwSubkeys = keyGenerator.getKwSubkeys();
    }

    @Override
    public int getBlockLenBytes() {
        return DATA_LENGTH_BYTES;
    }

    private long FL(long data, long subkey) {
        int x1 = (int) (data >>> 32);
        int x2 = (int) (data & 0xFFFFFFFFL);
        int k1 = (int) (subkey >>> 32);
        int k2 = (int) (subkey & 0xFFFFFFFFL);
        x2 = x2 ^ (oneCycleShift((x1 & k1)));
        x1 = x1 ^ (x2 | k2);
        return ((long) x1 << 32) | (long) x2 & 0xFFFFFFFFL;
    }

    private long FLInv(long data, long subkey) {
        int y1 = (int) (data >>> 32);
        int y2 = (int) (data & 0xFFFFFFFFL);
        int k1 = (int) (subkey >>> 32);
        int k2 = (int) (subkey & 0xFFFFFFFFL);
        y1 = y1 ^ (y2 | k2);
        y2 = y2 ^ (oneCycleShift((y1 & k1)));
        return ((long) y1 << 32) | (long) y2 & 0xFFFFFFFFL;
    }

    private int oneCycleShift(int value) {
        return ((value << 1) | (value >>> 31));
    }
}
