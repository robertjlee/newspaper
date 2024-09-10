package org.homelinux.rjlee.news.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SemVerTest {

    @Test
    void valueOfInvalid() {
        assertThrows(NumberFormatException.class, () -> SemVer.valueOf("fish"));
    }

    @Test
    void valueOfEmpty() {
        assertThrows(NumberFormatException.class, () -> SemVer.valueOf(""));
    }

    @Test
    void valueOfNull() {
        assertThrows(NullPointerException.class, () -> SemVer.valueOf(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0.0", "0.0.1", "0.1.1", "1.0.0", "10.0.0", "0.10.0"})
    void testToString(String str) {
        SemVer semVer = new SemVer(str);
        String string = semVer.toString();
        assertEquals(str, string);
    }

    @ParameterizedTest
    @CsvSource(textBlock =
            "0.0.0, 0.0.0, 0\n" +
            "0.0.1, 0.0.1, 0\n" +
            "0.1.0, 0.1.0, 0\n" +
            "1.0.0, 1.0.0, 0\n" +
            "0.0.0, 0.0.1, -1\n" +
            "0.0.1, 0.1.0, -1\n" +
            "0.1.0, 1.0.0, -1\n" +
            "0.0.1, 0.0.0, +1\n" +
            "0.1.0, 0.0.1, +1\n" +
            "1.0.0, 0.1.0, +1\n"
    )
    void compareTo(SemVer from, SemVer to, int expected) {
        int diff = from.compareTo(to);
        if (diff < 0) diff = -1;
        if (diff > 0) diff = 1;
        assertEquals(expected, diff);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0.0", "0.0.1", "0.1.1", "1.0.0", "10.0.0", "0.10.0"})
    void testEquals_true(SemVer ver) {
        assertEquals(ver, new SemVer(ver.toString()));
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    void testEquals_same() {
        SemVer ver = SemVer.valueOf("1.2.3");
        assertEquals(ver, ver);
    }

    @ParameterizedTest
    @CsvSource(textBlock =
                    "0.0.0, 0.0.1\n" +
                    "0.0.1, 0.1.0\n" +
                    "0.1.0, 1.0.0\n" +
                    "0.0.1, 0.0.0\n" +
                    "0.1.0, 0.0.1\n" +
                    "1.0.0, 0.1.0\n"
    )
    void testEquals_false(SemVer a, SemVer b) {
        assertNotEquals(a, b);
    }

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    void testEquals_class() {
        assertNotEquals(SemVer.valueOf("1.10.20"), "fish");
    }

    @Test
    void testEquals_null() {
        assertNotEquals(SemVer.valueOf("1.10.20"), null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0.0", "0.0.1", "0.1.1", "1.0.0", "10.0.0", "0.10.0"})
    void testHashCode(SemVer ver) {
        assertEquals(ver.hashCode(), new SemVer(ver.toString()).hashCode());
    }
}