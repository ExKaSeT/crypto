package org.example.encryption.assymetric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static java.util.Objects.isNull;

public class DiffieHellmanEncryption {

    // RFC 3526: 2048-bit MODP Group
    private static final BigInteger PRIME = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);
    private static final BigInteger GENERATOR = BigInteger.TWO;
    private static final int PRIME_BIT_LENGTH = 2048;

    private static final BigInteger MIN_SECRET_VALUE = BigInteger.valueOf(666L);
    private static final Random random = new SecureRandom();

    private BigInteger secret;

    public BigInteger generateSecretAndOpenKey() {
        secret = generateSecret();
        return GENERATOR.modPow(secret, PRIME);
    }

    public BigInteger generateSharedSecret(BigInteger openKey) {
        if (isNull(this.secret)) {
            throw new IllegalStateException("Secret key not initialized");
        }
        return openKey.modPow(secret, PRIME);
    }

    private static BigInteger generateSecret() {
        BigInteger secret;
        do {
            secret = new BigInteger(random.nextInt( PRIME_BIT_LENGTH), random);
        } while (secret.compareTo(MIN_SECRET_VALUE) == -1);
        return secret;
    }
}
