package org.example.round_key;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.encryption_converter.CamelliaEncryptionConverter;
import org.example.util.EncryptionUtil;
import java.util.Arrays;
import static java.lang.Integer.min;
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

        long[] keys = null;
        long[] kl;
        long[] kw;

        if (keySize != CamelliaKeySize.KEY128) {
            L = KAL ^ KRL;
            R = KAR ^ KRR;
            result = this.feistelLoop(L, R, CONSTANTS[2]);
            long KBL = result[0];
            long KBR = result[1];

            keys = new long[24];
            keys[0] = KBL;
            keys[1] = KBR;
            var key34 = pairLeftCycleShift(KRL, KRR, 15);
            keys[2] = key34[0];
            keys[3] = key34[1];
            var key56 = pairLeftCycleShift(KAL, KAR, 15);
            keys[4] = key56[0];
            keys[5] = key56[1];
            var key78 = pairLeftCycleShift(KBL, KBR, 30);
            keys[6] = key78[0];
            keys[7] = key78[1];
            var key910 = pairLeftCycleShift(KLL, KLR, 45);
            keys[8] = key910[0];
            keys[9] = key910[1];
            var key1112 = pairLeftCycleShift(KAL, KAR, 45);
            keys[10] = key1112[0];
            keys[11] = key1112[1];
            var key1314 = pairLeftCycleShift(KRL, KRR, 60);
            keys[12] = key1314[0];
            keys[13] = key1314[1];
            var key1516 = pairLeftCycleShift(KBL, KBR, 60);
            keys[14] = key1516[0];
            keys[15] = key1516[1];
            var key1718 = pairLeftCycleShift(KLL, KLR, 77);
            keys[16] = key1718[0];
            keys[17] = key1718[1];
            var key1920 = pairLeftCycleShift(KRL, KRR, 94);
            keys[18] = key1920[0];
            keys[19] = key1920[1];
            var key2122 = pairLeftCycleShift(KAL, KAR, 94);
            keys[20] = key2122[0];
            keys[21] = key2122[1];
            var key2324 = pairLeftCycleShift(KLL, KLR, 111);
            keys[22] = key2324[0];
            keys[23] = key2324[1];

            kl = new long[6];
            var kl12 = pairLeftCycleShift(KRL, KRR, 30);
            kl[0] = kl12[0];
            kl[1] = kl12[1];
            var kl34 = pairLeftCycleShift(KLL, KLR, 60);
            kl[2] = kl34[0];
            kl[3] = kl34[1];
            var kl56 = pairLeftCycleShift(KAL, KAR, 77);
            kl[4] = kl56[0];
            kl[5] = kl56[1];

            kw = new long[4];
            kw[0] = KLL;
            kw[1] = KLR;
            var kw34 = pairLeftCycleShift(KBL, KBR, 111);
            kw[2] = kw34[0];
            kw[3] = kw34[1];
        } else {
            keys = new long[18];
            keys[0] = KAL;
            keys[1] = KAR;
            var key34 = pairLeftCycleShift(KLL, KLR, 15);
            keys[2] = key34[0];
            keys[3] = key34[1];
            var key56 = pairLeftCycleShift(KAL, KAR, 15);
            keys[4] = key56[0];
            keys[5] = key56[1];
            var key78 = pairLeftCycleShift(KLL, KLR, 45);
            keys[6] = key78[0];
            keys[7] = key78[1];
            keys[8] = pairLeftCycleShift(KAL, KAR, 45)[0];
            keys[9] = pairLeftCycleShift(KLL, KLR, 60)[1];
            var key1112 = pairLeftCycleShift(KAL, KAR, 60);
            keys[10] = key1112[0];
            keys[11] = key1112[1];
            var key1314 = pairLeftCycleShift(KLL, KLR, 94);
            keys[12] = key1314[0];
            keys[13] = key1314[1];
            var key1516 = pairLeftCycleShift(KAL, KAR, 94);
            keys[14] = key1516[0];
            keys[15] = key1516[1];
            var key1718 = pairLeftCycleShift(KLL, KLR, 111);
            keys[16] = key1718[0];
            keys[17] = key1718[1];

            kl = new long[4];
            var kl12 = pairLeftCycleShift(KAL, KAR, 30);
            kl[0] = kl12[0];
            kl[1] = kl12[1];
            var kl34 = pairLeftCycleShift(KLL, KLR, 77);
            kl[2] = kl34[0];
            kl[3] = kl34[1];

            kw = new long[4];
            kw[0] = KLL;
            kw[1] = KLR;
            var kw34 = pairLeftCycleShift(KAL, KAR, 111);
            kw[2] = kw34[0];
            kw[3] = kw34[1];
        }

        this.kl = kl;
        this.kw = kw;

        return Arrays.stream(keys).mapToObj(EncryptionUtil::longToByteArray).toArray(byte[][]::new);
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
        return new long[]{L, R};
    }

    private long[] pairLeftCycleShift(long left, long right, int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        if (count == 0) {
            return new long[]{left, right};
        }
        count = count % 128;
        int shiftCount = min(count, 64);
        long shiftedFromLeft = left >>> (64 - shiftCount);
        long leftShifted = shiftCount == 64 ? 0L : (left << shiftCount);
        left = leftShifted | (right >>> (64 - shiftCount));
        long rightShifted = shiftCount == 64 ? 0L : (right << shiftCount);
        right = rightShifted | shiftedFromLeft;

        if (shiftCount != count) {
            return pairLeftCycleShift(left, right, count - shiftCount);
        }

        return new long[]{left, right};
    }
}
