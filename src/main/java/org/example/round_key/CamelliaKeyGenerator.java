package org.example.round_key;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.encryption_converter.CamelliaEncryptionConverter;
import java.util.Arrays;
import static org.example.util.EncryptionUtil.byteArrayToLong;
import static org.example.util.EncryptionUtil.longToByteArray;

public class CamelliaKeyGenerator implements RoundKeyGenerator {

    private static final long[][] CONSTANTS = {
            {0xA09E667F3BCC908BL, 0xB67AE8584CAA73B2L},
            {0xC6EF372FE94F82BEL, 0x54FF53A5F1D36F1CL},
            {0x10E527FADE682D1DL, 0xB05688C2B3E6C1FDL}
    };

    private final CamelliaKeySize keySize;
    private long[] kl = null;
    private long[] kw = null;

    public CamelliaKeyGenerator(CamelliaKeySize keySize) {
        this.keySize = keySize;
    }

    public long[] getKlSubkeys() {
        return kl.clone();
    }

    public long[] getKwSubkeys() {
        return kw.clone();
    }

    @Override
    public byte[][] generate(byte[] key) {
        if (key.length != keySize.getSizeBytes()) {
            throw new IllegalArgumentException("Incorrect key length");
        }

        long KLL = byteArrayToLong(Arrays.copyOf(key, 8));
        long KLR = byteArrayToLong(Arrays.copyOfRange(key, 8, 16));
        long KRL = 0;
        long KRR = 0;
        if (keySize != CamelliaKeySize.KEY128) {
            KRL = byteArrayToLong(Arrays.copyOfRange(key, 16, 24));
            if (keySize == CamelliaKeySize.KEY256) {
                KRR = byteArrayToLong(Arrays.copyOfRange(key, 24, 32));
            } else {
                KRR = ~KRL;
            }
        }

        long L = KLL ^ KRL;
        long R = KLR ^ KRR;
        var result = this.feistelLoop(L, R, CONSTANTS[0]);
        L = result[0] ^ KLL;
        R = result[1] ^ KLR;
        result = this.feistelLoop(L, R, CONSTANTS[1]);
        long KAL = result[0];
        long KAR = result[1];

        if (keySize != CamelliaKeySize.KEY128) {
            L = KAL ^ KRL;
            R = KAR ^ KRR;
            result = this.feistelLoop(L, R, CONSTANTS[2]);
            long KBL = result[0];
            long KBR = result[1];
        }


        return new byte[][]{};
    }

    @Getter
    @RequiredArgsConstructor
    public enum CamelliaKeySize {
        KEY128(16),
        KEY192(24),
        KEY256(32);

        private final int sizeBytes;
    }

    private long[] feistelLoop(long L, long R, long[] keys) {
        var F = new CamelliaEncryptionConverter();
        for (long key : keys) {
            long LF = byteArrayToLong(F.convert(longToByteArray(L), longToByteArray(key)));
            long LXored = LF ^ R;
            R = L;
            L = LXored;
        }
        return new long[] {L, R};
    }

    private long leftCycleShift(long value, int count) {
        count = count % 64;
        long leftShifted = value << count;
        return leftShifted | (value >>> (64 - count));
    }
}
