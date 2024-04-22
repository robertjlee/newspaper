package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

class InputTest {

    private MockPath inPath;
    private MockPath outTexPath;
    private MockInput input;
    private Settings settings;

    @BeforeEach
    void setUp() {
        inPath = MockPath.createMockPathWithNameAndContent("in.tex", "% content");
        outTexPath = MockPath.createMockPathWithName("out.tex");
        input = new MockInput();
        settings = new Settings(new Properties());
    }

    public static Stream<Arguments> copyToTexSource() {
        return Stream.of(
                Arguments.of(Headers.InputMode.LATEX, false, "% content\n"),
                Arguments.of(Headers.InputMode.PLAIN, false, "\\inputPlainStripComments{in.tex}%\n"), // doesn't support inline
                Arguments.of(Headers.InputMode.MARKDOWN, false, "\\inputMdStripComments{in.tex}%\n"),
                Arguments.of(Headers.InputMode.LATEX, true, "\\input{in.tex}%\n"),
                Arguments.of(Headers.InputMode.PLAIN, true, "\\inputPlainStripComments{in.tex}%\n"),
                Arguments.of(Headers.InputMode.MARKDOWN, true, "\\inputMdStripComments{in.tex}%\n")
        );
    }

    @ParameterizedTest
    @MethodSource("copyToTexSource")
    void copyToTex(Headers.InputMode inputMode, boolean inputWithoutCopy, String expected) throws IOException {
        Properties propertySettings = new Properties();
        propertySettings.put("inputWithoutCopy", "" + inputWithoutCopy);
        Settings settings = new Settings(propertySettings);
        try (StringWriter sw = new StringWriter();
             BufferedWriter bw = new BufferedWriter(sw);
             PrintWriter out = new PrintWriter(bw)) {
            input.copyToTex(inputMode, settings, out, outTexPath);
            out.flush();
            Assertions.assertEquals("\\setemergencystretch\\numnewscols\\hsize\n" + expected, sw.toString());
        }
    }

    @Test
    void logInput_fixed() {
        CapturingLogger logger = new CapturingLogger();
        Input input = new InputFactory(new Settings(new Properties()), new MockLengthCalculator(), logger)
                .newInput(MockPath.createMockPathWithName("in.tex"), settings, new BufferedReader(new StringReader("%#Type:fixed")));
        input.logInput(logger);
        Assertions.assertEquals("Input [Insert:in.tex([16]25.590551x26.574803)]; 425.196850 column inches (25.590551 x 26.574803)\n", logger.elementsCollected());
    }

    @Test
    void logInput() {
        CapturingLogger logger = new CapturingLogger();
        Input input = new InputFactory(new Settings(new Properties()), new MockLengthCalculator(), logger)
                .newInput(MockPath.createMockPathWithName("in.tex"), settings, new BufferedReader(new StringReader("%#Type: article")));
        input.logInput(logger);
        Assertions.assertEquals("Input in.tex; 3.141500 column inches\n", logger.elementsCollected());
    }

    private class MockInput implements Input {

        @Override
        public Path path() {
            return inPath;
        }

        @Override
        public double area() {
            return 0;
        }

        @Override
        public double columnInches() {
            return 0;
        }

        @Override
        public Headers getHeaders() {
            return null;
        }

        @Override
        public Stream<String> preambleLines() {
            return Stream.empty();
        }
    }
}