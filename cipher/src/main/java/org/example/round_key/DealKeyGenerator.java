package org.example.round_key;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.encryption.symmetric.DesEncryption;
import org.example.encryption.symmetric.mode.EncryptionMode;
import org.example.encryption.symmetric.mode.Mode;
import java.util.concurrent.ForkJoinPool;
import static org.example.util.EncryptionUtil.blockXor;

public class DealKeyGenerator implements RoundKeyGenerator {

    private static final byte[] K = {1, 35, 69, 103, -119, -85, -51, -17};

    private final DealKeySize keySize;

    public DealKeyGenerator(DealKeySize keySize) {
        this.keySize = keySize;
    }

    @Override
    public byte[][] generate(byte[] key) {
        if (key.length != keySize.getSizeBytes()) {
            throw new IllegalArgumentException("Incorrect key length");
        }

        int keyCount = keySize.getSizeBytes() / 8;
        var keys = new byte[keyCount][];
        for (int i = 0; i < keyCount; i++) {
            var parseKey = new byte[8];
            System.arraycopy(key, i * 8, parseKey, 0, 8);
            keys[i] = parseKey;
        }

        var encryption = new DesEncryption();
        encryption.generateRoundKeys(K);
        var encryptor = Mode.CBC.getImpl(encryption, true, new byte[8]);

        byte[][] resultKeys;
        switch (keySize) {
            case KEY128 -> {
                resultKeys = new byte[6][];
                var toEncrypt = keys[0];
                resultKeys[0] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(keys[1], resultKeys[0]);
                resultKeys[1] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[0], getIforXor(1)), resultKeys[1]);
                resultKeys[2] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[1], getIforXor(2)), resultKeys[2]);
                resultKeys[3] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[0], getIforXor(4)), resultKeys[3]);
                resultKeys[4] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[1], getIforXor(8)), resultKeys[4]);
                resultKeys[5] = this.encrypt(encryptor, toEncrypt);
            }
            case KEY192 -> {
                resultKeys = new byte[6][];
                var toEncrypt = keys[0];
                resultKeys[0] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(keys[1], resultKeys[0]);
                resultKeys[1] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(keys[2], resultKeys[1]);
                resultKeys[2] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[0], getIforXor(1)), resultKeys[2]);
                resultKeys[3] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[1], getIforXor(2)), resultKeys[3]);
                resultKeys[4] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[2], getIforXor(4)), resultKeys[4]);
                resultKeys[5] = this.encrypt(encryptor, toEncrypt);
            }
            case KEY256 -> {
                resultKeys = new byte[8][];
                var toEncrypt = keys[0];
                resultKeys[0] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(keys[1], resultKeys[0]);
                resultKeys[1] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(keys[2], resultKeys[1]);
                resultKeys[2] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(keys[3], resultKeys[2]);
                resultKeys[3] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[0], getIforXor(1)), resultKeys[3]);
                resultKeys[4] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[1], getIforXor(2)), resultKeys[4]);
                resultKeys[5] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[2], getIforXor(4)), resultKeys[5]);
                resultKeys[6] = this.encrypt(encryptor, toEncrypt);
                toEncrypt = blockXor(blockXor(keys[3], getIforXor(8)), resultKeys[6]);
                resultKeys[7] = this.encrypt(encryptor, toEncrypt);
            }
            default -> throw new IllegalStateException();
        }

        return resultKeys;
    }

    @Getter
    @RequiredArgsConstructor
    public enum DealKeySize {
        KEY128(16),
        KEY192(24),
        KEY256(32);

        private final int sizeBytes;
    }

    private byte[] encrypt(EncryptionMode encryptor, byte[] data) {
        return encryptor.process(new byte[][] {data}, ForkJoinPool.commonPool());
    }
    
    private byte[] getIforXor(int i) {
        if (i < 1 || i > 8) {
            throw new IllegalArgumentException();
        }
        byte first = (byte) (1 << (8 - i));
        var result = new byte[8];
        result[0] = first;
        return result;
    }
}
