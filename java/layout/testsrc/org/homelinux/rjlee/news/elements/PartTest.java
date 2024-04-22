package org.homelinux.rjlee.news.elements;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PartTest {


    public static Stream<Arguments> widths() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(0.1, 1),
                Arguments.of(0.8, 1),
                Arguments.of(0.89, 1),
                Arguments.of(0.9, 1),
                Arguments.of(0.91, 2),
                Arguments.of(1, 2),
                Arguments.of(1.1, 2),
                Arguments.of(1.2, 2),
                Arguments.of(1.8, 2),
                Arguments.of(1.89, 2),
                Arguments.of(1.9, 2),
                Arguments.of(1.91, 3),
                Arguments.of(2, 3)
        );
    }

    @ParameterizedTest
    @MethodSource("widths")
    void widthToNumberOfColumns(double width, long expected) {
        long actual = Part.widthToNumberOfColumns(width, 0.1, 0.9);
        assertEquals(expected, actual);
    }
}