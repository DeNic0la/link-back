package ch.denic0la.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.security.SecureRandom;

public final class KeyGenerator {

    // Removed ambiguous characters like 0, O, 1, l to prevent user error
    private static final char[] ALPHANUMERIC_ALPHABET =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private static final char[] NUMERIC_ALPHABET =
            "0123456789".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyGenerator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a secure, URL-friendly unique ID.
     */
    public static String generateAccessKey(int length) {
        return NanoIdUtils.randomNanoId(RANDOM, ALPHANUMERIC_ALPHABET, length);
    }

    /**
     * Generates a numeric code (e.g., for 2FA or PINs).
     */
    public static String generateNumericCode(int length) {
        return NanoIdUtils.randomNanoId(RANDOM, NUMERIC_ALPHABET, length);
    }
}