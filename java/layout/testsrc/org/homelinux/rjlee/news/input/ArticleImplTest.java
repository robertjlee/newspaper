package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.elements.ArticleFragment;
import org.homelinux.rjlee.news.elements.Overflow;
import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;

class ArticleImplTest {

    public static final String CONTENT = "\\LaTeX\\ text here\n";
    private MockPath path;
    private ArticleImpl article;
    private Headers headers;
    private Settings settings;
    private MockLengthCalculator lengthCalculator;

    @BeforeEach
    void setUp() {
        path = MockPath.createMockPathWithNameAndContent("art1.tex", CONTENT);
        Properties headerProps = new Properties();
        headerProps.put("Preamble", "\\usepackage{indentfirst}");
        headers = new Headers(path, headerProps, settings);
        settings = new Settings(new Properties());
        lengthCalculator = new MockLengthCalculator();
        article = new ArticleImpl(headers, settings, lengthCalculator);
    }

    @Test
    void path() {
        assertSame(path, article.path());
    }

    @Test
    void testToString() {
        assertEquals("art1.tex", article.toString());
    }

    @Test
    void preambleLines() {
        assertEquals(singleton("\\usepackage{indentfirst}"), article.preambleLines().collect(Collectors.toSet()));
    }

    @Test
    void name() {
        assertEquals("art1.tex", article.name());
    }

    @Test
    void getHeaders() {
        assertSame(headers, article.getHeaders());
    }

    @Test
    void getSettings() {
        assertSame(settings, article.getSettings());
    }

    @Test
    void getPath() {
        assertSame(path, article.getPath());
    }

    @Test
    void getFragments() {
        assertEquals(emptyList(), article.getFragments());
    }

    @Test
    void getFragments_split() {
        article.splitArticle(1.0); // calls registerFragment()
        assertEquals(singletonList(1.0), article.getFragments());
    }

    @Test
    void countOutput() {
        article.countOutput();
        assertEquals(1, article.getOutCtr());
    }

    @Test
    void getOutCtr() {
        assertEquals(0, article.getOutCtr());
    }

    @Test
    void setNumColumnsOnPage() {
        // a paste-up article shouldn't care about the number of columns on the page; the method exists in the superclass
        // because HeadSpanArticle does.
        assertDoesNotThrow(() -> article.setNumColumnsOnPage(10));
    }

    @Test
    void registerFragment() {
        article.registerFragment(1.0);
        assertEquals(singletonList(1.0), article.getFragments());
    }

    @Test
    void recalculateLength() {
        article.columnInches(); // initialize the length
        lengthCalculator.setLength(12.34);
        article.recalculateLength();
        assertEquals(12.34, article.columnInches(), 0.00001);
    }

    @Test
    void copyTo() throws IOException {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(writer))) {
            article.copyTo(pw, article.getSettings().getOut());
        }
        assertEquals("\\setemergencystretch\\numnewscols\\hsize\n" +
                CONTENT, writer.toString());
    }

    @Test
    void height() {
        // should be the default from the MockLengthCalculator
        assertEquals(3.1415, article.height(), 0.00001);
    }

    @Test
    void area() {
        // height should come from the MockLengthCalculator
        assertEquals(3.1415 * settings.getColumnWidth(), article.area(), 0.00001);
    }

    @Test
    void columnInches() {
        // should be the default from the MockLengthCalculator
        assertEquals(3.1415, article.columnInches(), 0.00001);
    }

    @Test
    void widthForSizing() {
        assertEquals(settings.getColumnWidth(), article.widthForSizing(), 0.00001);
    }

    @Test
    void splitRemainingArticle() {
        ArticleFragment split = article.splitRemainingArticle(20);
        assertAll(
                () -> assertSame(article, split.getArticle()),
                () -> assertSame(path, split.path()),
                () -> assertEquals(3.1415, split.height(), 0.00001),
                () -> assertEquals(settings.getColumnWidth(), split.width(), 0.0),
                () -> assertFalse(split.skipHalley())
        );
    }

    @Test
    void splitArticle() {
        ArticleFragment split = article.splitArticle(5.2);
        assertAll(
                () -> assertSame(article, split.getArticle()),
                () -> assertSame(path, split.path()),
                () -> assertEquals(5.2, split.height(), 0.00001),
                () -> assertEquals(settings.getColumnWidth(), split.width(), 0.0),
                () -> assertFalse(split.skipHalley())
        );
    }

    @Test
    void createOverflow() {
        Overflow overflow = article.createOverflow(5.4, 3);
        assertAll(
                () -> assertSame(article, overflow.getArticle()),
                () -> assertEquals(5.4, overflow.getLength(), 0.0),
                () -> assertEquals(0, overflow.getSplitCounter(), "First split should be 0")
        );
    }
}