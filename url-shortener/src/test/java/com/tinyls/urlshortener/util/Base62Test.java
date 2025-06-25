package com.tinyls.urlshortener.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Base62Test {

    @ParameterizedTest
    @ValueSource(longs = {
            0L,
            1L,
            61L,
            62L,
            63L,
            12345L,
            1_000_000L,
            218340105584895L
    })
    @DisplayName("encode then decode returns original value")
    void encodeDecodeSymmetry(long value) {
        String encoded = Base62.encode(value);
        long decoded = Base62.decode(encoded);
        assertEquals(value, decoded);
    }

    @Test
    @DisplayName("encode rejects negative numbers")
    void encodeNegativeNumberThrows() {
        assertThrows(IllegalArgumentException.class, () -> Base62.encode(-1L));
    }

    @Test
    @DisplayName("encode rejects numbers that exceed maximum length")
    void encodeNumberTooLargeThrows() {
        long tooLarge = 218340105584896L; // 62^8
        assertThrows(IllegalArgumentException.class, () -> Base62.encode(tooLarge));
    }

    @Test
    @DisplayName("decode rejects invalid strings")
    void decodeInvalidStringThrows() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> Base62.decode(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> Base62.decode("")),
                () -> assertThrows(IllegalArgumentException.class, () -> Base62.decode("invalid$")),
                () -> assertThrows(IllegalArgumentException.class, () -> Base62.decode("123456789")));
    }

    @Test
    @DisplayName("isValid correctly validates strings")
    void isValidChecksStrings() {
        assertAll(
                () -> assertTrue(Base62.isValid("0")),
                () -> assertTrue(Base62.isValid("ZZZZZZZZ")),
                () -> assertFalse(Base62.isValid(null)),
                () -> assertFalse(Base62.isValid("")),
                () -> assertFalse(Base62.isValid("invalid$")),
                () -> assertFalse(Base62.isValid("123456789")));
    }
}