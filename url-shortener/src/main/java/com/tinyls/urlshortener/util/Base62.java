package com.tinyls.urlshortener.util;

/**
 * Utility class for Base62 encoding and decoding.
 * Base62 is a binary-to-text encoding scheme that represents binary data in an
 * ASCII string format
 * using 62 characters: 0-9, a-z, and A-Z.
 * 
 * This implementation is used for generating short codes for URLs, providing a
 * compact
 * representation of numeric IDs while maintaining URL-safe characters.
 * 
 * Note: The character order (0-9, a-z, A-Z) must be maintained as it affects
 * the encoding
 * of existing short codes in the database.
 */
public class Base62 {
    /**
     * The character set used for Base62 encoding.
     * Contains 62 characters in the order: 0-9, a-z, A-Z.
     * This order must be maintained for compatibility with existing encoded values.
     */
    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * The base value for Base62 encoding (62).
     */
    private static final int BASE = CHARACTERS.length();

    /**
     * Maximum length of encoded string.
     * Matches the database column constraint for shortCode.
     */
    private static final int MAX_LENGTH = 8;

    /**
     * Encodes a long number to a Base62 string.
     * The encoded string will be at most 8 characters long to match the database
     * constraint.
     * 
     * @param number the number to encode
     * @return the Base62 encoded string
     * @throws IllegalArgumentException if the input number is negative or would
     *                                  produce a string longer than MAX_LENGTH
     */
    public static String encode(long number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }

        if (number == 0) {
            return String.valueOf(CHARACTERS.charAt(0));
        }

        StringBuilder result = new StringBuilder();
        while (number > 0) {
            result.insert(0, CHARACTERS.charAt((int) (number % BASE)));
            number /= BASE;
        }

        String encoded = result.reverse().toString();
        if (encoded.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Encoded value exceeds maximum length of " + MAX_LENGTH);
        }
        return encoded;
    }

    /**
     * Decodes a Base62 string back to a long number.
     * 
     * @param str the Base62 encoded string to decode
     * @return the decoded number
     * @throws IllegalArgumentException if the input string is null, empty, contains
     *                                  invalid characters,
     *                                  or exceeds MAX_LENGTH
     */
    public static long decode(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("String must not be null or empty");
        }

        if (str.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Input string exceeds maximum length of " + MAX_LENGTH);
        }

        long result = 0L;
        for (char c : str.toCharArray()) {
            int value = CHARACTERS.indexOf(c);
            if (value == -1) {
                throw new IllegalArgumentException("Invalid character in string: " + c);
            }
            result = result * BASE + value;
        }
        return result;
    }

    /**
     * Validates if a string is a valid Base62 encoded string.
     * Checks for null, empty string, invalid characters, and maximum length.
     * 
     * @param str the string to validate
     * @return true if the string is a valid Base62 encoded string, false otherwise
     */
    public static boolean isValid(String str) {
        if (str == null || str.isEmpty() || str.length() > MAX_LENGTH) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (CHARACTERS.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }
}
