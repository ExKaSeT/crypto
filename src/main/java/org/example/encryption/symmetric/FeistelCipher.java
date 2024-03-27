package org.example.encryption.symmetric;

import lombok.RequiredArgsConstructor;
import org.example.encryption_converter.EncryptionConverter;
import org.example.round_key.RoundKeyGenerator;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public abstract class FeistelCipher implements SymmetricEncryption {

    private final EncryptionConverter converter;
    private final RoundKeyGenerator keyGenerator;

    private byte[][] roundKeys;

    @Override
    public byte[] encrypt(byte[] data) {
        return encryption(data, true);
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return encryption(data, false);
    }

    private byte[] encryption(final byte[] data, boolean isKeysDirectOrder) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Data length must be even");
        }
        if (isNull(roundKeys)) {
            throw new NullPointerException("Round keys are not initialized");
        }

        int blockLength = data.length / 2;
        var block1 = new byte[blockLength];
        var block2 = new byte[blockLength];
        System.arraycopy(data, 0, block1, 0, blockLength);
        System.arraycopy(data, blockLength, block2, 0, blockLength);

        int index = isKeysDirectOrder ? 0 : roundKeys.length - 1;
        for (int i = 0; i < roundKeys.length; i++) {
            var block2Prev = block2;
            block2 = converter.convert(block2, roundKeys[index]);
            for (int x = 0; x < block1.length; x++) {
                block2[x] = (byte) (block2[x] ^ block1[x]);
            }
            block1 = block2Prev;
            if (isKeysDirectOrder) {
                index++;
            } else {
                index--;
            }
        }

        var block1Prev = block1;
        block1 = block2;
        block2 = block1Prev;

        var result = new byte[data.length];
        System.arraycopy(block1, 0, result, 0, block1.length);
        System.arraycopy(block2, 0, result, blockLength, block2.length);

        return result;
    }

    @Override
    public void generateRoundKeys(byte[] key) {
        this.roundKeys = keyGenerator.generate(key);
    }

    @Override
    public int getKeyLenBytes() {
        return this.keyGenerator.getKeyLenBytes();
    }
}
