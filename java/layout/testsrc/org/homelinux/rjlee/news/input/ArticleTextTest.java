package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArticleTextTest {

    private static final String CONTENT = "\\typeout{Test!}";
    private static final String HEADLINE = "This is a headline";
    public static final String HEAD_FONT_COMMAND = "\\null";
    public static final String ESTRETCH = "\\setemergencystretch\\numnewscols\\hsize";
    private MockPath path;
    private ArticleText article;

    @BeforeEach
    void setUp() {
        path = MockPath.createMockPathWithNameAndContent("art1.tex", CONTENT);
        Properties headerProps = new Properties();
        headerProps.put("Head", HEADLINE);
        headerProps.put("HeadCommand", HEAD_FONT_COMMAND);
        Settings settings = new Settings(new Properties());
        Headers headers = new Headers(path, headerProps, settings);
        MockLengthCalculator lengthCalculator = new MockLengthCalculator();
        article = new ArticleImpl(headers, settings, lengthCalculator);
    }

    @Test
    void copyTo() throws IOException {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(writer))) {
            article.copyTo(pw, article.getSettings().getOut());
        }
        String expected = String.format("\\headline{%s{}\n%s}\n\\par\n%s\n%s\n", HEAD_FONT_COMMAND, HEADLINE, ESTRETCH, CONTENT);
        assertEquals(expected, writer.toString());
    }

    @Test
    void name() {
        assertEquals("art1.tex", article.name());
    }

    /**
     * This is purely a coverage test, for discarding an exception that the JVM can't throw (because UTF-8 is always supported).
     */
    @Test
    void name_error() {
        ArticleImpl ai = new ArticleImpl(article.getHeaders(), article.getSettings(), new MockLengthCalculator()) {
            @Override
            String nameByEncoding(String encoding) throws UnsupportedEncodingException {
                throw new UnsupportedEncodingException("Test error");
            }
        };
        assertThrows(RuntimeException.class, ai::name);
    }

    @Test
    void nameByEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> article.nameByEncoding("illegal encoding name"));
    }
}