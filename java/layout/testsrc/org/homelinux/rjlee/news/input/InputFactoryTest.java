package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class InputFactoryTest {

    private InputFactory factory;
    private Settings settings;

    @BeforeEach
    void setUp() {
        settings = new Settings(new Properties());
        factory = new InputFactory(settings, new MockLengthCalculator(), Logger.getInstance());
    }

    @Test
    void error() throws IOException {
        CapturingLogger logger = new CapturingLogger();
        InputFactory errorFactory = new InputFactory(settings, new MockLengthCalculator(), logger) {
            @Override
            protected Headers readHeaders(Path p, Settings settings, BufferedReader r) throws IOException {
                throw new IOException("Test!");
            }
        };
        try (BufferedReader reader = new BufferedReader(new StringReader(""))) {
            Input input = errorFactory.newInput(MockPath.createMockPathWithName("bad.tex"), settings, reader);
            String collectedError = logger.quietCollected();
            assertAll(
                    () -> assertNull(input),
                    () -> assertTrue(collectedError.startsWith("Error reading bad.tex:\njava.io.IOException: Test!"), collectedError)
            );
        }
    }

    @Test
    void fixed() throws IOException {
        String in = "%#Type: fixed";
        try (BufferedReader reader = new BufferedReader(new StringReader(in))) {
            Input input = factory.newInput(MockPath.createMockPathWithNameAndContent("fixed.tex", in), settings, reader);
            assertInstanceOf(Insert.class, input);
        }
    }

    @Test
    void headSpan() throws IOException {
        String in = "%#Type: headSpan\n%#Cols: 7";
        try (BufferedReader reader = new BufferedReader(new StringReader(in))) {
            Input input = factory.newInput(MockPath.createMockPathWithNameAndContent("span.tex", in), settings, reader);
            assertInstanceOf(HeadSpanArticle.class, input);
        }
    }

    @Test
    void title() throws IOException {
        String in = "%#Type: title\n%#Cols: 7";
        try (BufferedReader reader = new BufferedReader(new StringReader(in))) {
            Input input = factory.newInput(MockPath.createMockPathWithNameAndContent("00title.tex", in), settings, reader);
            assertInstanceOf(TitleInsert.class, input);
        }
    }

    @Test
    void pasteUp() throws IOException {
        String in = "%#Type: article";
        try (BufferedReader reader = new BufferedReader(new StringReader(in))) {
            Input input = factory.newInput(MockPath.createMockPathWithNameAndContent("span.tex", in), settings, reader);
            assertInstanceOf(Article.class, input);
        }
    }

    @Test
    void markdownNotUsed() {
        assertFalse(factory.isMarkdownUsed());
    }

    @Test
    void markdownUser() throws IOException {
        String in = "%#Mode=Markdown";
        try (BufferedReader reader = new BufferedReader(new StringReader(in))) {
            factory.newInput(MockPath.createMockPathWithNameAndContent("span.tex", in), settings, reader);
            assertTrue(factory.isMarkdownUsed());
        }
    }

    @Test
    void readInputFile() {
        CapturingLogger logger = new CapturingLogger();
        MockPath path = MockPath.createMockPathWithNameAndContent("in.tex", "%#Type: fixed");
        Input file = factory.readInputFile(path, settings, logger);
        assertAll(
                () -> assertInstanceOf(Insert.class, file),
                () -> assertEquals("Considering <in.tex>\n", logger.dumpAllCollected())
        );
    }
    @Test
    void readInputFile_error() {
        CapturingLogger logger = new CapturingLogger();
        MockPath path = MockPath.createMockPathForErrorOnOpenInput("in.tex");
        assertAll(
                () -> assertThrows(RuntimeException.class, () -> factory.readInputFile(path, settings, logger)),
                () -> assertEquals("Considering <in.tex>\n", logger.dumpAllCollected())
        );
    }
}