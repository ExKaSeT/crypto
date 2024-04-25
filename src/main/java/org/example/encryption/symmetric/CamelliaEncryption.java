package org.example.encryption.symmetric;

import org.example.encryption_converter.CamelliaEncryptionConverter;
import org.example.round_key.CamelliaKeyGenerator;
import org.example.util.EncryptionUtil;

import java.util.Arrays;

public class CamelliaEncryption extends FeistelCipher {

    private static final int DATA_LENGTH_BYTES = 16;
    private byte[][][] roundKeys;
    private byte[][] klSubkeys;
    private byte[][] kwSubkeys;

    public CamelliaEncryption(CamelliaKeyGenerator.CamelliaKeySize keySize) {
        super(new CamelliaEncryptionConverter(), new CamelliaKeyGenerator(keySize));
    }

    @Override
    public byte[] encrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        xor16byteWithTwo8byte(data, kwSubkeys[0], kwSubkeys[1]);

        int klIndex = 0;
        for (byte[][] roundKey : this.roundKeys) {
            super.roundKeys = roundKey;
            data = super.encrypt(data);
            swap8byteBlocks(data);

            if (klIndex < klSubkeys.length) {
                var leftPart = this.FL(Arrays.copyOf(data, 8), klSubkeys[klIndex]);
                var rightPart = this.FLInv(Arrays.copyOfRange(data, 8, 16), klSubkeys[klIndex + 1]);
                xor16byteWithTwo8byte(data, leftPart, rightPart);
                klIndex += 2;
            }
        }

        xor16byteWithTwo8byte(data, kwSubkeys[2], kwSubkeys[3]);
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        if (data.length != DATA_LENGTH_BYTES) {
            throw new IllegalArgumentException("Incorrect data length");
        }

        xor16byteWithTwo8byte(data, kwSubkeys[2], kwSubkeys[3]);

        int klIndex = klSubkeys.length - 1;
        for (int roundsOf6Rounds = this.roundKeys.length - 1; roundsOf6Rounds >= 0; roundsOf6Rounds--) {
            super.roundKeys = this.roundKeys[roundsOf6Rounds];
            data = super.decrypt(data);
            swap8byteBlocks(data);

            if (klIndex > 0) {
                var leftPart = this.FL(Arrays.copyOf(data, 8), klSubkeys[klIndex]);
                var rightPart = this.FLInv(Arrays.copyOfRange(data, 8, 16), klSubkeys[klIndex - 1]);
                xor16byteWithTwo8byte(data, leftPart, rightPart);
                klIndex -= 2;
            }
        }

        xor16byteWithTwo8byte(data, kwSubkeys[0], kwSubkeys[1]);
        return data;
    }

    @Override
    public void generateRoundKeys(byte[] key) {
        var keyGenerator = (CamelliaKeyGenerator) super.keyGenerator;

        var roundKeys = keyGenerator.generate(key);

        this.roundKeys = new byte[roundKeys.length / 6][][];
        int arrayIndex = 0;
        for (int part = 0; part < roundKeys.length; part += 6) {
            this.roundKeys[arrayIndex] = Arrays.copyOfRange(roundKeys, part, part + 6);
            arrayIndex++;
        }

        this.klSubkeys = Arrays.stream(keyGenerator.getKlSubkeys()).mapToObj(EncryptionUtil::longToByteArray).toArray(byte[][]::new);
        this.kwSubkeys = Arrays.stream(keyGenerator.getKwSubkeys()).mapToObj(EncryptionUtil::longToByteArray).toArray(byte[][]::new);
    }

    @Override
    public int getBlockLenBytes() {
        return DATA_LENGTH_BYTES;
    }

    private void xor16byteWithTwo8byte(byte[] byte16, byte[] left8byte, byte[] right8byte) {
        if (byte16.length != 16 || left8byte.length != 8 || right8byte.length != 8) {
            throw new IllegalArgumentException();
        }

        for (int x = 0; x < 8; x++) {
            byte16[x] ^= left8byte[0];
        }
        for (int x = 8; x < 16; x++) {
            byte16[x] ^= right8byte[x - 8];
        }
    }

    private void swap8byteBlocks(byte[] toSwap) {
        if (toSwap.length != 16) {
            throw new IllegalArgumentException();
        }
        var tmp = Arrays.copyOf(toSwap, 8);
        System.arraycopy(toSwap, 8, toSwap, 0, 8);
        System.arraycopy(tmp, 0, toSwap, 8, 8);
    }

    private byte[] FL(byte[] data, byte[] kl) {
        if (data.length != 8 || kl.length != 8) {
            throw new IllegalArgumentException();
        }
        var result = new byte[8];
        // xor and shift
        for (int x = 0; x < 4; x++) {
            result[x + 3] = (byte) (data[x] ^ kl[x]);
        }
        result[7] = result[3];
        // xor with right part
        for (int x = 4; x < 8; x++) {
            result[x] = (byte) (result[x] ^ data[x]);
        }
        // or and xor
        for (int x = 0; x < 4; x++) {
            result[x] = (byte) ((result[x + 4] | kl[x + 4]) ^ data[x]);
        }
        return result;
    }

    private byte[] FLInv(byte[] data, byte[] kl) {
        if (data.length != 8 || kl.length != 8) {
            throw new IllegalArgumentException();
        }
        var result = new byte[8];
        // or and xor
        for (int x = 0; x < 4; x++) {
            result[x] = (byte) ((data[x + 4] | kl[x + 4]) ^ data[x]);
        }
        // xor and shift
        for (int x = 1; x < 4; x++) {
            result[x + 3] = (byte) (result[x] ^ kl[x]);
        }
        result[7] = (byte) (result[0] ^ kl[0]);
        // xor with right part
        for (int x = 4; x < 8; x++) {
            result[x] = (byte) (result[x] ^ data[x]);
        }
        return result;
    }
}
