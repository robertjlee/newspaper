package org.homelinux.rjlee.news;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;

class ColumnCalculatorTest {

    @ParameterizedTest
    @CsvSource(".1,1, 1, 1\n" +
            "   1, 1, 1, 1\n" +
            "  10, 1, 1, 1\n" +
            "10.1, 2, 1, 2\n" +
            "  50, 5, 1, 5\n" +
            " 100,10,20, 5\n")
    void calculateColumnsPerPage(double columnInches, long expectedTotalColumns, long expectedNumPages, int expectedColsPerPage) {
        ColumnCalculator results = ColumnCalculator.calculateColumnsPerPage(columnInches, 10, 5);
        assertAll(
                () -> assertSame(expectedTotalColumns, results.getTotalColumns()),
                () -> assertSame(expectedNumPages, results.getNumPages()),
                () -> assertSame(expectedColsPerPage, results.getColsPerPage()));
    }

    @Test
    void calculateColumnsPerPage2() {
        ColumnCalculator results = ColumnCalculator.calculateColumnsPerPage(156, 7.44, 6);
        assertAll(
                () -> assertSame(21L, results.getTotalColumns()),
                () -> assertSame(4L, results.getNumPages()),
                () -> assertSame(5, results.getColsPerPage()));
    }
}