package org.homelinux.rjlee.news.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LengthParserTest {

    @ParameterizedTest
    @CsvSource(textBlock =
            "0, 0\n" +
                    "00000000, 0\n" +
                    "1.1in, 1.1\n" +
                    "1.1inch, 1.1\n" +
                    "1.1inches, 1.1\n" +
                    "1mm, 0.039370078740157477\n" +
                    "1cm, 0.39370078740157477\n" +
                    "1dm, 3.9370078740157477\n" +
                    "1m, 39.370078740157477\n" +
                    "86.72400086724001pt, 1.2\n" +
                    "86.72400086724001pts, 1.2\n" +
	       // "fill" means pagewidth; so we use max_double
                    "fill, 1.7976931348623157E308\n" +
	       // negative numbers mean we've overflowed 16384pt:
                    "-86.72400086724001pt, 227.905408\n" +
                    "-86.72400086724001pts, 227.905408\n"
    )
    void readLength(String input, double expected) {
        assertEquals(expected, LengthParser.readLength(input), 0.000001);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"garbage", "inch by inch"})
    void readBadLength(String len) {
        assertThrows(RuntimeException.class, () -> LengthParser.readLength(len));
    }
}
