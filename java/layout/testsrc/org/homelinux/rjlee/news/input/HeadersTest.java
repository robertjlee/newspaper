package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeadersTest {

    private Path path = MockPath.createMockPathWithName("input.tex");
    private Settings settings = new Settings(new Properties());

    @Test
    void readHeaders() throws IOException {
        String testHeaders = new String("" +
                "% Comment line\n" +
                "%## Comment line\n" +
                "%#! Comment line\n" +
                "%# foo = \\\\bar \\\n%# \t and \\\\baz\n" +
                "%# baz = bat\n" +
                "%# bip : bop\n" +
                "%# cip sop");
        Properties expected = new Properties();
        expected.put("foo", "\\bar and \\baz");
        expected.put("baz", "bat");
        expected.put("bip", "bop");
        expected.put("cip", "sop");
        try (BufferedReader br = new BufferedReader(new StringReader(testHeaders))) {
            Properties actual = Headers.readHeaders(br);
            assertEquals(expected, actual, actual.entrySet().stream().map(e -> "[" + e.getKey() + "]=[" + e.getValue() + "]" ).collect(Collectors.joining("  ;  ")));
        }
    }

    @Test
    void getHeader() throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%# foo=bar\n%#baz=bat"))) {
            Headers h = new Headers(path, r, settings);
            assertEquals("bar", h.getHeader("foo", "cat"));
        }
    }

    @Test
    void ifHeader_present() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new StringReader("%# foo=bar\n%#baz=bat"))) {
            Headers h = new Headers(path, r, settings);
            h.ifHeader("foo", sb::append);
        }
        assertEquals("bar", sb.toString());
    }

    @Test
    void ifHeader_absent() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new StringReader(""))) {
            Headers h = new Headers(path, r, settings);
            h.ifHeader("foo", sb::append);
        }
        assertEquals("", sb.toString());
    }

    @ParameterizedTest
    @CsvSource(textBlock = "article,ARTICLE\n" +
            ",SKIP\n" +
            "HeadSpan,HEAD_SPAN\n" +
            "inSeRT,INSERT\n")
    void getInputType(String t, Headers.InputType expecd) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Type=" + t))) {
            Headers h = new Headers(path, r, settings);
            assertEquals(expecd, h.getInputType());
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = "direct,LATEX\n" +
            ",LATEX\n" +
            "Latex,LATEX\n" +
            "Plain,PLAIN\n" +
            "maRkDoWn,MARKDOWN\n")
    void getInputMode(String t, Headers.InputMode expecd) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Mode=" + t))) {
            Headers h = new Headers(path, r, settings);
            assertEquals(expecd, h.getInputMode());
        }
    }

    @Test
    void getIntegerHeaderDefault() throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader(""))) {
            Headers h = new Headers(path, r, settings);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> h.getIntegerHeader("Col", 1, 99));
            assertEquals("input.tex: Missing header value for [Col]; must be in range 1-99", ex.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"blue", ""})
    void getIntegerHeaderInvalid(String input) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Col=" + input))) {
            Headers h = new Headers(path, r, settings);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> h.getIntegerHeader("Col", 1, 99));
            assertEquals("input.tex: Bad header value ["+input+"] for [Col]; not a valid Integer. Must be in range 1-99", ex.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "100"})
    void getIntegerHeaderOutOfRange(String input) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Col=" + input))) {
            Headers h = new Headers(path, r, settings);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> h.getIntegerHeader("Col", 1, 99));
            assertEquals("input.tex: Bad header value ["+input+"] for [Col]; not in range 1-99", ex.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1,2,99})
    void getIntegerHeaderValid(long col) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Col="+col))) {
            Headers h = new Headers(path, r, settings);
            assertEquals(col, h.getIntegerHeader("Col", 1, 99));
        }
    }

    @Test
    void getIntegerHeaderWithDefDefault() throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader(""))) {
            Headers h = new Headers(path, r, settings);
            assertEquals(50, h.getIntegerHeader("Col", 1, 99, 50));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"blue", ""})
    void getIntegerHeaderWithDefInvalid(String input) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Col=" + input))) {
            Headers h = new Headers(path, r, settings);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> h.getIntegerHeader("Col", 1, 99, 50));
            assertEquals("input.tex: Bad header value ["+input+"] for [Col]; not a valid Integer. Must be in range 1-99", ex.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "100"})
    void getIntegerHeaderWithDefOutOfRange(String input) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Col=" + input))) {
            Headers h = new Headers(path, r, settings);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> h.getIntegerHeader("Col", 1, 99, 50));
            assertEquals("input.tex: Bad header value ["+input+"] for [Col]; not in range 1-99", ex.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1,2,99})
    void getIntegerHeaderWithDefValid(long col) throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Col="+col))) {
            Headers h = new Headers(path, r, settings);
            assertEquals(col, h.getIntegerHeader("Col", 1, 99, 50));
        }
    }

    @Test
    void getLengthHeader() throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Width=1.25in"))) {
            Headers h = new Headers(path, r, settings);
            assertEquals(1.25, h.getLengthHeader("Width", 0));
        }
    }

    @Test
    void getLengthHeaderInvalid() throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader("%#Width=cols"))) {
            Headers h = new Headers(path, r, settings);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> h.getLengthHeader("Width", 0));
            assertEquals("input.tex: Bad header value [Width] for length [cols]; Not implemented to read length [cols]; try mm, inches or TeX points?", ex.getMessage());
        }
    }

    @Test
    void getLengthHeaderDefault() throws IOException {
        try (BufferedReader r = new BufferedReader(new StringReader(""))) {
            Headers h = new Headers(path, r, settings);
            assertEquals(1.25, h.getLengthHeader("Col", 1.25));
        }
    }
}