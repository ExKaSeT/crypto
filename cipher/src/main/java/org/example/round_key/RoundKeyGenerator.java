package org.example.round_key;

public interface RoundKeyGenerator {
    byte[][] generate(byte[] key);
}
