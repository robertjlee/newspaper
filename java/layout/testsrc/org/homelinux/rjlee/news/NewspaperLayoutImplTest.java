package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.elements.Overflow;
import org.homelinux.rjlee.news.input.*;
import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.rendered.ColumnarPage;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class NewspaperLayoutImplTest {

    private Settings settings = new Settings(new Properties());
    private MockNewspaperToLatex newspaperToLatex;
    private Path[] dirs;
    private CapturingLogger logger;
    private NewspaperLayoutImpl newLayout;

    @BeforeEach
    void setUp() {
        dirs = new Path[]{MockPath.createMockDirectoryWithSettingsFile("")};
        newspaperToLatex = new MockNewspaperToLatex();
        logger = new CapturingLogger();
        newLayout = new NewspaperLayoutImpl(settings, logger, dirs);
    }

    @Test
    void testToString() {
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment empty@[0.0-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    @Test
    void hasData() {
        assertFalse(newLayout.hasData());
    }

    @Test
    void layoutPage() {
        ColumnarPage p = new ColumnarPage(1, 5, settings);
        newLayout.layoutPage(p, 0);
        assertEquals("PAGE 1\n" +
                " Column1:[Fragment empty@[0.0-26.5748031496063]]\n" +
                " Column2:[Fragment empty@[0.0-26.5748031496063]]\n" +
                " Column3:[Fragment empty@[0.0-26.5748031496063]]\n" +
                " Column4:[Fragment empty@[0.0-26.5748031496063]]\n" +
                " Column5:[Fragment empty@[0.0-26.5748031496063]]\n", p.toString());
    }

    @Test
    void trimEmptyPages() {
        newLayout.trimEmptyPages();
        assertEquals("[]", newLayout.toString());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> newLayout.validate(), "Empty layout should be valid");
    }

    @Test
    void validate_notLaidOut() {
        // create an article
        Path path1 = MockPath.createMockPathWithNameAndContent("art.tex", "\\lipsum");
        Headers headers = new Headers(path1, new Properties(), settings);
        Article article = new ArticleImpl(headers, settings, new MockLengthCalculator());
        Logger logger = new CapturingLogger();
        newLayout.processInputs(singletonList(article), logger, true);

        // as we haven't invoked the layout, the article is not laid out. So the final sanity check will fail:

        RuntimeException ex = assertThrows(RuntimeException.class, newLayout::validate);
        assertEquals("Not rendered: art.tex", ex.getMessage());
    }


    @Test
    void preambleLines() {
        assertEquals(emptyList(), newLayout.preambleLines().collect(Collectors.toList()));

    }

    @Test
    void getSettings() {
        assertSame(settings, newLayout.getSettings());
    }

    @Test
    void getPages() {
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment empty@[0.0-26.5748031496063]]\n" +
                "]", newLayout.getPages().toString());
    }

    @Test
    void layOutNewspaper() {
        newLayout.layOutNewspaper();
        assertEquals("[]", newLayout.toString());
    }

    @Test
    void layoutPage_withOverflowOnly() {
        Path path = MockPath.createMockPathWithNameAndContent("art.tex", "\\lipsum");
        Headers headers = new Headers(path, new Properties(), settings);
        Article article = new ArticleImpl(headers, settings, new MockLengthCalculator());
        newLayout.setOverflow(new Overflow(article, 3.1, 1));
        newLayout.layoutPage((ColumnarPage) newLayout.getPages().get(0), 0);
        assertAll(
                () -> assertEquals("[PAGE 1\n" +
                        " Column1:[Fragment for part [art.tex:0 => 3.059723 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                        "]", newLayout.toString()),
                () -> assertTrue(logger.algorithmCollected().contains(" - overflow set\n"), logger::algorithmCollected)
        );
    }

    @Test
    void layoutPage_withOverflowEnlargingPage() {
        Path path = MockPath.createMockPathWithNameAndContent("art.tex", "\\lipsum");
        Headers headers = new Headers(path, new Properties(), settings);
        Article article = new ArticleImpl(headers, settings, new MockLengthCalculator());
        newLayout.setOverflow(new Overflow(article, settings.getColumnHeight() + 1, 1));
        newLayout.layoutPage((ColumnarPage) newLayout.getPages().get(0), 1);
        assertAll(
                () -> assertEquals("[PAGE 1\n" +
                        " Column1:[Fragment for part [art.tex:0 => 26.493026 in]@[0.0-26.5748031496063]]\n" +
                        " Column2:[Fragment for part [art.tex:1 => 3.141500 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                        "]", newLayout.toString(), "While there is only one column defined, we should prefer to enlarge the page rather than perhaps creating another."),
                () -> assertTrue(logger.algorithmCollected().contains(" - overflow set\n"), logger::algorithmCollected)
        );
    }

    @Test
    void layoutPage_withOverflowEnlargingPage2() {
        Path path = MockPath.createMockPathWithNameAndContent("art.tex", "\\lipsum");
        Headers headers = new Headers(path, new Properties(), settings);
        Article article = new ArticleImpl(headers, settings, new MockLengthCalculator());
        newLayout.setOverflow(new Overflow(article, 2 * settings.getColumnHeight() + 1, 1));
        newLayout.layoutPage((ColumnarPage) newLayout.getPages().get(0), 2);
        assertAll(
                () -> assertEquals("[PAGE 1\n" +
                        " Column1:[Fragment for part [art.tex:0 => 26.493026 in]@[0.0-26.5748031496063]]\n" +
                        " Column2:[Fragment for part [art.tex:1 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        "]", newLayout.toString(), "While there is only one column defined, we should prefer to enlarge the page rather than perhaps creating another."),
                () -> assertTrue(logger.algorithmCollected().contains(" - overflow set\n"), logger::algorithmCollected)
        );
    }

    /**
     * This is a weird edge-case that probably can happen due to rounding errors.
     * The page is full, and we think we've got an article fragment still to lay out, but we recalculate the length
     * and get 0 - so we should place nothing on the page but also return no overflow.
     */
    @Test
    void layoutPage_with0LengthArticleOnFullPage() {
        // create a fixed-size article taking up all of the page
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Properties headProps = new Properties();
        headProps.put("Height", (settings.getColumnHeight()) + "in");
        Headers headers = new Headers(path1, headProps, settings);
        MockLengthCalculator lengthCalculator = new MockLengthCalculator();
        lengthCalculator.setLength(settings.getColumnHeight() * 16);
        Article article1 = new ArticleImpl(headers, settings, lengthCalculator);

        // create the 0-size article fragment
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Headers headers2 = new Headers(path2, new Properties(), settings);
        MockLengthCalculator lengthCalculator2 = new MockLengthCalculator();
        lengthCalculator2.setLength(0);
        Article article2 = new ArticleImpl(headers2, settings, lengthCalculator2);

        Logger logger = new CapturingLogger();
        List<Input> list = singletonList(article1);
        newLayout.processInputs(list, logger, false);

        ColumnarPage p = (ColumnarPage) newLayout.getPages().get(0);
        IntStream.range(0, 15).forEach(i -> p.addExtraColumn());
        newLayout.layOutNewspaper();

        Overflow overflow = p.setArticleByFirstFit(article2, 0.0, null, 0);

        assertNull(overflow);
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [art1.tex:0 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column2:[Fragment for part [art1.tex:1 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column3:[Fragment for part [art1.tex:2 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column4:[Fragment for part [art1.tex:3 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column5:[Fragment for part [art1.tex:4 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column6:[Fragment for part [art1.tex:5 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column7:[Fragment for part [art1.tex:6 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column8:[Fragment for part [art1.tex:7 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column9:[Fragment for part [art1.tex:8 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column10:[Fragment for part [art1.tex:9 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column11:[Fragment for part [art1.tex:10 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column12:[Fragment for part [art1.tex:11 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column13:[Fragment for part [art1.tex:12 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column14:[Fragment for part [art1.tex:13 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column15:[Fragment for part [art1.tex:14 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column16:[Fragment for part [art1.tex:15 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                "]", newLayout.toString());

    }

    /**
     * This tests the case where an article is so long that it overflows one page onto the next.
     * Probably not a real use-case, unless you're printing a very long article in A4.
     */
    @Test
    void layoutPage_withOverflowingOverflow() {
        Path path = MockPath.createMockPathWithNameAndContent("art.tex", "\\lipsum");
        Headers headers = new Headers(path, new Properties(), settings);
        MockLengthCalculator lengthCalculator = new MockLengthCalculator();
        double columnInches = settings.getColumnHeight() * 10;
        lengthCalculator.setLength(columnInches);
        Article article = new ArticleImpl(headers, settings, lengthCalculator);
        newLayout.setOverflow(new Overflow(article, columnInches, 1));
        newLayout.getPages().add(new ColumnarPage(2, 9, settings));
        newLayout.layoutPage((ColumnarPage) newLayout.getPages().get(0), 0);
        newLayout.layoutPage((ColumnarPage) newLayout.getPages().get(1), 0);
        assertAll(() -> assertEquals("[PAGE 1\n" +
                        " Column1:[Fragment for part [art.tex:0 => 26.411249 in]@[0.0-26.5748031496063]]\n" +
                        ", PAGE 2\n" +
                        " Column1:[Fragment for part [art.tex:1 => 26.493026 in]@[0.0-26.5748031496063]]\n" +
                        " Column2:[Fragment for part [art.tex:2 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        " Column3:[Fragment for part [art.tex:3 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        " Column4:[Fragment for part [art.tex:4 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        " Column5:[Fragment for part [art.tex:5 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        " Column6:[Fragment for part [art.tex:6 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        " Column7:[Fragment for part [art.tex:7 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        " Column8:[Fragment for part [art.tex:8 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                        " Column9:[Fragment for part [art.tex:9 => 26.493026 in]@[0.0-26.5748031496063]]\n" +
                        "]", newLayout.toString()),
                () -> assertEquals(0.1635538934799572, newLayout.getOverflow().getLength()));
    }

    /**
     * We attempt to fit articles entirely by worst-fit first, then fall back to first-fit.
     * So this is actually dead code at the moment: the first-fit algorithm setting the entire article into the
     * first column in one block if it will fit.
     * But it's a valid use-case for if we reuse the first-fit algorithm elsewhere.
     */
    @Test
    void layoutByFirstFit_twoArticlesOneColumn() {
        // create a short article
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Headers headers = new Headers(path1, new Properties(), settings);
        Article article1 = new ArticleImpl(headers, settings, new MockLengthCalculator());

        // create a second article
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Headers headers2 = new Headers(path2, new Properties(), settings);
        Article article2 = new ArticleImpl(headers2, settings, new MockLengthCalculator());
        Logger logger = new CapturingLogger();
        newLayout.processInputs(singletonList(article1), logger, false);

        // set article1
        ColumnarPage page = (ColumnarPage) newLayout.getPages().get(0);
        newLayout.layoutPage(page, 0);

        // now test article2 ends up in the same column
        page.setArticleByFirstFit(article2, article2.columnInches(), null, 0);
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [art1.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment for part V-mode alley{cols=1}@[3.1415-3.2665], Fragment for part [art2.tex:0 => 3.141500 in]@[3.2665-6.408], Fragment empty@[6.408-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    @Test
    void layoutPage_articlePlusOverflow() {
        // create an overflowing article from the last page
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Headers headers = new Headers(path1, new Properties(), settings);
        Article article1 = new ArticleImpl(headers, settings, new MockLengthCalculator());

        // create a new article
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Headers headers2 = new Headers(path2, new Properties(), settings);
        Article article2 = new ArticleImpl(headers2, settings, new MockLengthCalculator());
        Logger logger = new CapturingLogger();
        newLayout.processInputs(singletonList(article2), logger, false);

        newLayout.setOverflow(new Overflow(article1, 3.1, 1));
        newLayout.layoutPage((ColumnarPage) newLayout.getPages().get(0), 0);
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [art1.tex:0 => 3.059723 in]@[0.0-3.1415], Fragment for part V-mode alley{cols=1}@[3.1415-3.2665], Fragment for part [art2.tex:0 => 3.141500 in]@[3.2665-6.408], Fragment empty@[6.408-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    /**
     * This is a rare edge case, but an important one: the articles to put onto a page are chosen by their estimated
     * size, enough to just overflow the page. If the user provides exactly enough content for the one page, an entire
     * article may be pushed back to be processed on the next page.
     */
    @Test
    void layoutNewspaper_deferredDueToExactFill() {
        // create a fixed-size article taking up all of the page
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Properties headProps = new Properties();
        headProps.put("Height", (settings.getColumnHeight()) + "in");
        Headers headers = new Headers(path1, headProps, settings);
        MockLengthCalculator lengthCalculator = new MockLengthCalculator();
        lengthCalculator.setLength(settings.getColumnHeight() * 16);
        Article article1 = new ArticleImpl(headers, settings, lengthCalculator);

        // create articles that will overflow or push back onto page 2 in full
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Headers headers2 = new Headers(path2, new Properties(), settings);
        Article article2 = new ArticleImpl(headers2, settings, new MockLengthCalculator());

        Path path3 = MockPath.createMockPathWithNameAndContent("art3.tex", "\\lipsum");
        Headers headers3 = new Headers(path3, new Properties(), settings);
        Article article3 = new ArticleImpl(headers3, settings, new MockLengthCalculator());

        Logger logger = new CapturingLogger();
        List<Input> list = asList(article1, article2, article3);
        newLayout.processInputs(list, logger, false);

        ColumnarPage p = (ColumnarPage) newLayout.getPages().get(0);
        IntStream.range(0, 15).forEach(i -> p.addExtraColumn());
        newLayout.getPages().add(new ColumnarPage(1, 1, settings));
        newLayout.layOutNewspaper();

        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [art1.tex:0 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column2:[Fragment for part [art1.tex:1 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column3:[Fragment for part [art1.tex:2 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column4:[Fragment for part [art1.tex:3 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column5:[Fragment for part [art1.tex:4 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column6:[Fragment for part [art1.tex:5 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column7:[Fragment for part [art1.tex:6 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column8:[Fragment for part [art1.tex:7 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column9:[Fragment for part [art1.tex:8 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column10:[Fragment for part [art1.tex:9 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column11:[Fragment for part [art1.tex:10 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column12:[Fragment for part [art1.tex:11 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column13:[Fragment for part [art1.tex:12 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column14:[Fragment for part [art1.tex:13 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column15:[Fragment for part [art1.tex:14 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                " Column16:[Fragment for part [art1.tex:15 => 26.574803 in]@[0.0-26.5748031496063]]\n" +
                ", PAGE 1\n" +
                " Column1:[Fragment for part [art2.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment for part V-mode alley{cols=1}@[3.1415-3.2665], Fragment for part [art3.tex:0 => 3.141500 in]@[3.2665-6.408], Fragment empty@[6.408-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    @Test
    void layoutNewspaper_fixedPlusArticleWithOverflow() {
        // create a fixed-size article taking up most of the page
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Properties headProps = new Properties();
        headProps.put("Height", (settings.getColumnHeight() - settings.getAlleyHeight() * 2.01) + "in");
        Headers headers = new Headers(path1, headProps, settings);
        Insert fixed = new Insert(headers, settings);

        // create a new article that will overflow onto page 2
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Headers headers2 = new Headers(path2, new Properties(), settings);
        Article article2 = new ArticleImpl(headers2, settings, new MockLengthCalculator());
        Logger logger = new CapturingLogger();
        List<Input> list = asList(fixed, article2);
        System.out.println("list = " + list);
        newLayout.processInputs(list, logger, false);

        ColumnarPage p = (ColumnarPage) newLayout.getPages().get(0);
        IntStream.range(0, 16).forEach(i -> p.addExtraColumn());
        newLayout.getPages().add(new ColumnarPage(2, 1, settings));
        newLayout.layOutNewspaper();
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:0 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column2:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:1 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column3:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:2 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column4:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:3 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column5:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:4 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column6:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:5 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column7:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:6 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column8:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:7 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column9:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:8 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column10:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:9 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column11:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:10 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column12:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:11 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column13:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:12 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column14:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:13 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column15:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:14 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column16:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:15 => 0.126250 in]@[26.4485531496063-26.5748031496063]]\n" +
                " Column17:[Fragment for part [Insert:art1.tex([17]27.500000x26.323553)]@[0.0-26.3235531496063], Fragment for part V-mode alley{cols=17}@[26.3235531496063-26.4485531496063], Fragment for part [art2.tex:16 => 0.044473 in]@[26.4485531496063-26.5748031496063]]\n" +
                ", PAGE 2\n" +
                " Column1:[Fragment for part [art2.tex:17 => 0.995250 in]@[0.0-1.0770269467400193], Fragment empty@[1.0770269467400193-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    /**
     * Test the "Page" header works as expected, creating extra pages as needed.
     */
    @Test
    void layoutPage_keepForPage2() {
        // create an article
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Properties headProps = new Properties();
        Headers headers = new Headers(path1, headProps, settings);
        ArticleImpl article1 = new ArticleImpl(headers, settings, new MockLengthCalculator());

        // create a second article with a specified page
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Properties head2 = new Properties();
        head2.put("Page", "2");
        Headers headers2 = new Headers(path2, head2, settings);
        Article article2 = new ArticleImpl(headers2, settings, new MockLengthCalculator());
        Logger logger = new CapturingLogger();
        List<Input> list = asList(article1, article2);
        System.out.println("list = " + list);
        newLayout.processInputs(list, logger, false);
        newLayout.layOutNewspaper();
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [art1.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                ", PAGE 2\n" +
                " Column1:[Fragment for part [art2.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    /**
     * Test the "Page" header works as expected, creating extra pages as needed, even though they be blank.
     */
    @Test
    void layoutPage_keepForPage3() {
        // create an article
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Properties headProps = new Properties();
        Headers headers = new Headers(path1, headProps, settings);
        ArticleImpl article1 = new ArticleImpl(headers, settings, new MockLengthCalculator());

        // create a second article with a specified page
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Properties head2 = new Properties();
        head2.put("Page", "3");
        Headers headers2 = new Headers(path2, head2, settings);
        Article article2 = new ArticleImpl(headers2, settings, new MockLengthCalculator());
        Logger logger = new CapturingLogger();
        List<Input> list = asList(article1, article2);
        System.out.println("list = " + list);
        newLayout.processInputs(list, logger, false);
        newLayout.layOutNewspaper();
        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [art1.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                ", PAGE 2\n" +
                " Column1:[Fragment empty@[0.0-26.5748031496063]]\n" +
                ", PAGE 3\n" +
                " Column1:[Fragment for part [art2.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    @Test
    void layoutPage_keepForLastPage() {
        newLayout.getPages().add(new ColumnarPage(2, 1, settings));
        newLayout.getPages().add(new ColumnarPage(3, 1, settings));

        // create an article
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "\\lipsum");
        Properties headProps = new Properties();
        Headers headers = new Headers(path1, headProps, settings);
        ArticleImpl article1 = new ArticleImpl(headers, settings, new MockLengthCalculator());

        // create a second article with a specified page
        Path path2 = MockPath.createMockPathWithNameAndContent("art2.tex", "\\lipsum");
        Properties head2 = new Properties();
        head2.put("Page", "-1");
        Headers headers2 = new Headers(path2, head2, settings);
        Article article2 = new ArticleImpl(headers2, settings, new MockLengthCalculator());
        Logger logger = new CapturingLogger();
        List<Input> list = asList(article1, article2);
        System.out.println("list = " + list);
        newLayout.processInputs(list, logger, false);
        newLayout.layOutNewspaper();

        assertEquals("[PAGE 1\n" +
                " Column1:[Fragment for part [art1.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                ", PAGE 2\n" +
                " Column1:[Fragment empty@[0.0-26.5748031496063]]\n" +
                ", PAGE 3\n" +
                " Column1:[Fragment for part [art2.tex:0 => 3.141500 in]@[0.0-3.1415], Fragment empty@[3.1415-26.5748031496063]]\n" +
                "]", newLayout.toString());
    }

    @Test
    void handleFinalOutput() {
        newspaperToLatex.handleFinalOutput(newLayout);
        assertEquals("[writeTexFile, compileFinalPdf]", newspaperToLatex.getMethodCallOrder().toString());
    }

    @ParameterizedTest
    @CsvSource(textBlock = ".foo, false\n.tex, true")
    void isInputFile(String name, boolean isInput) {
        MockPath path = MockPath.createMockPathWithName(name);
        assertEquals(isInput, newLayout.isInputFile(path));
    }

    /**
     * Test the edge case where a user has asked for an insert too big to fit on a page
     */
    @Test
    void insertLongerThanPage() {
        for (int i=0; i < 15; i++) ((ColumnarPage)newLayout.getPages().get(0)).addExtraColumn();
        // create an article
        Path path1 = MockPath.createMockPathWithNameAndContent("art1.tex", "");
        Properties headProps = new Properties();
        headProps.put("Cols", "15");
        Headers headers = new Headers(path1, headProps, settings);
        MockLengthCalculator lengthCalculator = new MockLengthCalculator();
        lengthCalculator.setLength(settings.getPageHeight() * 30);
        HeadSpanArticle art1 = new HeadSpanArticle(headers, settings, lengthCalculator);
        newLayout.processInputs(singletonList(art1), logger, false);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, newLayout::layOutNewspaper);
        assertEquals("Input art1.tex would be longer than available page height. Increase page size, change to type article, or reduce size of insert.", ex.getMessage());
    }
}