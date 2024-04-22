package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HeadSpanArticleTest {

    private static final long NUM_COLS = 6;
    private static final String CONTENT = "content";
    private Path path;
    private MockLengthCalculator lengthCalculator;
    private Headers headers;
    private Settings settings;
    private HeadSpanArticle headSpanArticle;

    @BeforeEach
    void setUp() {
        path = MockPath.createMockPathWithNameAndContent("span.tex", CONTENT);
        Properties headProperties = new Properties();
        headProperties.put("Preamble1", "% Preamble1");
        headProperties.put("Preamble2", "% Preamble2");
        headProperties.put("Cols", "" + NUM_COLS);
        headProperties.put("Head", "Newsflash!");
        headers = new Headers(path, headProperties, settings);
        settings = new Settings(new Properties());
        lengthCalculator = new MockLengthCalculator();
        headSpanArticle = new HeadSpanArticle(headers, settings, lengthCalculator);
    }

    @Test
    void illegalCols() {
        Properties headProps = new Properties();
        headProps.put("Cols", "illegal");
        Headers headers = new Headers(path, headProps, settings);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new HeadSpanArticle(headers, settings, lengthCalculator));
        assertEquals("HeadSpan article: 'Cols' header must exist and must be a number of columns large enough to contain the article.", ex.getMessage());
    }

    @Test
    void missingCols() {
        Properties headProps = new Properties();
        Headers headers = new Headers(path, headProps, settings);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new HeadSpanArticle(headers, settings, lengthCalculator));
        assertEquals("HeadSpan article: 'Cols' header must exist and must be a number of columns large enough to contain the article.", ex.getMessage());
    }

    @Test
    void illegalRuleWidth() {
        Properties headProps = new Properties();
        headProps.put("Cols", "" + NUM_COLS);
        headProps.put("RuleWidth", "illegal length");
        Headers headers = new Headers(path, headProps, settings);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new HeadSpanArticle(headers, settings, lengthCalculator));
        assertEquals("HeadSpan article: 'RuleWidth' header may be set to override the thickness of a rule within the article, but must be a valid length", ex.getMessage());
    }

    @Test
    void path() {
        assertSame(path, headSpanArticle.path());
    }

    @Test
    void recalculateLength() {
        headSpanArticle.height(); // initialize length
        lengthCalculator.setLength(14.5);
        headSpanArticle.recalculateLength();
        assertEquals(14.5, headSpanArticle.height(), 0.0001);
    }

    @Test
    void copyTo() throws IOException {

        try (StringWriter sw = new StringWriter();
             BufferedWriter bw = new BufferedWriter(sw);
             PrintWriter pw = new PrintWriter(bw)) {
            headSpanArticle.copyTo(pw, headSpanArticle.getSettings().getOut());
            pw.flush();

            double expectedWidth = NUM_COLS * settings.getColumnWidth() + (NUM_COLS - 1) * settings.getAlleyWidth();
            String expected = String.format(
                    "\\vbox{\n" +
                            "\\begingroup\\setlength{\\columnsep}{%fin}\n" +
                            "\\setlength{\\columnseprule}{%fin}\n" +
                            "\\textwidth=%fin\\hsize=%fin\\linewidth=%fin\n" +
                            "\\parbox{9.625000in}{\\center\\headline{Newsflash!}}\\par\n" +
                            "\\begin{multicols}{%d}[][0pt]\n" +
                            "\\setlength{\\parindent}{\\parindentcopy}\n" +
                            "\\setemergencystretch\\numnewscols\\hsize\n" +
                            "%s\n" +
                            "\\end{multicols}\\endgroup}",
                    settings.getAlleyWidth(),
                    settings.getAlleyThickWidth(),
                    expectedWidth,expectedWidth,expectedWidth,
                    NUM_COLS,
                    CONTENT);

            assertEquals(expected, sw.toString());
        }
    }


    @Test
    void preambleLines() {
        List<String> expected = new ArrayList<>();
        expected.add("% Preamble1");
        expected.add("% Preamble2");
        expected.add("\\usepackage{multicol}\\multicoltolerance" + settings.getTolerance() + "\\multicolpretolerance100");
        assertEquals(expected, headSpanArticle.preambleLines().collect(Collectors.toList()));
    }

    @Test
    void height() {
        assertEquals(3.1415, headSpanArticle.height(), 0.0001); // default from MockLengthCalculator
    }

    @Test
    void name() {
        assertEquals("span.tex", headSpanArticle.name());
    }

    @Test
    void getHeaders() {
        assertSame(headers, headSpanArticle.getHeaders());
    }

    @Test
    void getSettings() {
        assertSame(settings, headSpanArticle.getSettings());
    }

    @Test
    void getPath() {
        assertSame(path, headSpanArticle.getPath());
    }

    @Test
    void getFragments() {
        assertTrue(headSpanArticle.getFragments().isEmpty(), "We don't fragment head-span articles");
    }

    @Test
    void countOutput() {
        headSpanArticle.countOutput();
        assertEquals(1, headSpanArticle.getOutCtr());
    }

    @Test
    void getOutCtr() {
        assertEquals(0, headSpanArticle.getOutCtr());
    }

    @Test
    void setNumColumnsOnPage() {
        assertDoesNotThrow(() -> headSpanArticle.setNumColumnsOnPage(12));
    }

    @Test
    void skipHalley() {
        assertTrue(headSpanArticle.skipHalley());
    }

    @Test
    void widthForSizing() {
        assertEquals(settings.getColumnWidth() * NUM_COLS + settings.getAlleyWidth() * (NUM_COLS - 1), headSpanArticle.widthForSizing(), 0.0001);
    }

    @Test
    void width() {
        assertEquals(settings.getColumnWidth() * NUM_COLS + settings.getAlleyWidth() * (NUM_COLS - 1), headSpanArticle.width(), 0.0001);
    }

    @Test
    void columnInches() {
        assertEquals(3.1415 * NUM_COLS, headSpanArticle.columnInches(), 0.0001); // default from MockLengthCalculator
    }

    @Test
    void area() {
        double expectedWidth = settings.getColumnWidth() * NUM_COLS + settings.getAlleyWidth() * (NUM_COLS - 1);
        assertEquals(expectedWidth * 3.1415, headSpanArticle.area(), 0.0001);
    }

    @Test
    void cols() {
        assertEquals(NUM_COLS, headSpanArticle.cols());
    }

    /**
     * The ability to limit the number of columns is to allow for full-width elements (eg titles):
     * if the page has to be enlarged to accommodate another fixed-sized element, then we must also enlarge the
     * full-width elements. The simplest way is to set the full-width element width to Double.MAX_VALUE, and use
     * the number of columns actually on the page as a limit. This works for both fixed N-column articles (where N is
     * the number of columns on the page before enlargement), and full-width articles.
     */
    @Test
    void cols_limitedPage() {
        headSpanArticle.setNumColumnsOnPage(NUM_COLS-1);
        assertEquals(NUM_COLS-1, headSpanArticle.cols());
    }

    @Test
    void testSetNumColumnsOnPage() {
        headSpanArticle.setNumColumnsOnPage(7);
        assertEquals(NUM_COLS, headSpanArticle.cols());
    }

    @Test
    void testToString() {
        assertEquals("span.tex", headSpanArticle.toString());
    }

}