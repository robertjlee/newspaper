package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.input.ArticleImpl;
import org.homelinux.rjlee.news.input.ArticleText;
import org.homelinux.rjlee.news.input.Headers;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coverage tests for the LaTeXLengthCalculator, to test the mechanism while avoiding shelling out
 *
 * @author Robert
 */
class LaTeXLengthCalculatorTest {

    @Test
    void calculateMethod() {
        Object[] args = new Object[2];
        LengthCalculator calc = new LaTeXLengthCalculator() {
            @Override
            double calculateLength(ArticleText articleText, LatexLength ll) {
                args[0] = articleText;
                args[1] = ll;
                return 13.45;
            }
        };

        Path path = MockPath.createMockPathWithName("tmp.tex");
        Settings settings = new Settings(new Properties());
        Headers headers = new Headers(path, new Properties(), settings);
        ArticleImpl article = new ArticleImpl(headers, settings, calc);
        List<Double> fragments = Arrays.asList(6.5, 7.8);
        double returnedLength = calc.calculateLength(1.23, fragments, Stream.empty(), new Settings(new Properties()), article);

        assertAll(
                () -> assertEquals(13.45, returnedLength, 0.0),
                () -> assertSame(article, args[0]),
                () -> assertEquals(1.23, ((LatexLength) args[1]).getWidth(), 0.0001),
                () -> assertEquals(fragments, ((LatexLength) args[1]).getFragments()),
                () -> assertInstanceOf(LatexProcessFactory.class, ((LatexLength) args[1]).getLatexProcessFactory())
        );
    }

    @Test
    void calculateLengthByCalculator_badLength() {
        LaTeXLengthCalculator calc = new LaTeXLengthCalculator();
        Path path = MockPath.createMockPathWithName("tmp.tex");
        Settings settings = new Settings(new Properties());
        Headers headers = new Headers(path, new Properties(), settings);
        List<Double> fragments = Arrays.asList(6.5, 7.8);
        ArticleImpl article = new ArticleImpl(headers, settings, calc);

        StringBuilder stdOut = new StringBuilder();
        StringBuilder stdErr = new StringBuilder();
        ByteArrayOutputStream stdIn = new ByteArrayOutputStream();

        ShellProcessFactory shellProcessFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn);
        LatexLength length = new LatexLength(1.23, fragments, Stream.of("\\usepackage{foo}"), settings, shellProcessFactory, Logger.getInstance());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> calc.calculateLength(article, length), "No length was returned!");
        assertEquals("Failed to calculate article length: Process completed without producing length: 0", ex.getMessage());
    }

    @Test
    void calculateLengthByCalculator_process_io_error() {
        LaTeXLengthCalculator calc = new LaTeXLengthCalculator();
        Path path = MockPath.createMockPathWithName("tmp.tex");
        Settings settings = new Settings(new Properties());
        Headers headers = new Headers(path, new Properties(), settings);
        List<Double> fragments = Arrays.asList(6.5, 7.8);
        ArticleImpl article = new ArticleImpl(headers, settings, calc);

        StringBuilder stdOut = new StringBuilder();
        StringBuilder stdErr = new StringBuilder();
        ByteArrayOutputStream stdIn = new ByteArrayOutputStream();

        ShellProcessFactory shellProcessFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn);
        LatexLength length = new LatexLength(1.23, fragments, Stream.of("\\usepackage{foo}"), settings, shellProcessFactory, Logger.getInstance()) {
            @Override
            public PrintWriter writer() throws IOException {
                throw new IOException("Error!");
            }
        };
        assertThrows(RuntimeException.class, () -> calc.calculateLength(article, length), "No length was returned!");
    }

    @Test
    void calculateLengthByCalculator() {
        LaTeXLengthCalculator calc = new LaTeXLengthCalculator();
        Path path = MockPath.createMockPathWithName("tmp.tex");
        Settings settings = new Settings(new Properties());
        Headers headers = new Headers(path, new Properties(), settings);
        List<Double> fragments = Arrays.asList(6.5, 7.8);
        ArticleImpl article = new ArticleImpl(headers, settings, calc);

        StringBuilder stdOut = new StringBuilder("ART HEIGHT:99.9in\nART DEPTH:0");
        StringBuilder stdErr = new StringBuilder();
        ByteArrayOutputStream stdIn = new ByteArrayOutputStream();

        ShellProcessFactory shellProcessFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn);
        LatexLength length = new LatexLength(1.23, fragments, Stream.of("\\usepackage{foo}"), settings, shellProcessFactory, Logger.getInstance());
        double result = calc.calculateLength(article, length);

        assertAll(
                () -> assertEquals("", new String(stdIn.toByteArray(), StandardCharsets.UTF_8)), // TODO: this writes to a tmp file; should it?
                () -> assertEquals(6.5+7.8+99.9, result, 0.0001)
        );
    }

    @Test
    void calculateLengthWithAssets() {
        LaTeXLengthCalculator calc = new LaTeXLengthCalculator();
        Path path = MockPath.createMockPathWithName("tmp.tex");
        Settings settings = new Settings(new Properties());
        Headers headers = new Headers(path, new Properties(), settings) {
            @Override
            public Stream<Path> assets() {
                return Stream.of(MockPath.createMockPathWithName("file.txt"));
            }
        };
        List<Double> fragments = Arrays.asList(6.5, 7.8);
        ArticleImpl article = new ArticleImpl(headers, settings, calc);

        StringBuilder stdOut = new StringBuilder("ART HEIGHT:99.9in\nART DEPTH:0");
        StringBuilder stdErr = new StringBuilder();
        ByteArrayOutputStream stdIn = new ByteArrayOutputStream();

        ShellProcessFactory shellProcessFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn);
        LatexLength length = new LatexLength(1.23, fragments, Stream.of("\\usepackage{foo}"), settings, shellProcessFactory, Logger.getInstance()) {
            @Override
            public Path outPath() {
                return MockPath.createMockPathWithName("tmpdir", true);
            }
        };
        double result = calc.calculateLength(article, length);

        assertAll(
                () -> assertEquals("", new String(stdIn.toByteArray(), StandardCharsets.UTF_8)), // TODO: this writes to a tmp file; should it?
                () -> assertEquals(6.5+7.8+99.9, result, 0.0001)
        );
    }
}