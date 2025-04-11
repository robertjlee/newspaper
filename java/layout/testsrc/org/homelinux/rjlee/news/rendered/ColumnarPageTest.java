package org.homelinux.rjlee.news.rendered;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.elements.Valley;
import org.homelinux.rjlee.news.input.Article;
import org.homelinux.rjlee.news.input.InputFactory;
import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;

class ColumnarPageTest {

    private ColumnarPage page;
    private Settings settings = new Settings(new Properties());

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        properties.put("pageHeight", "5in");
        Settings settings = new Settings(properties);
        this.page = new ColumnarPage(1, 3, settings);
    }

    @Test
    void numCols() {
        Assertions.assertEquals(3, page.numCols());
    }

    @Test
    void getSimplePageNo() {
        Assertions.assertEquals(1, page.getSimplePageNo());
    }

    @Test
    void getColumns() {
        Assertions.assertEquals(3, page.getColumns().size());
    }

    @Test
    void write_empty() throws IOException {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(new BufferedWriter(sw))) {
            page.write(pw, settings.getOut());
            pw.flush();
            Assertions.assertEquals("\\vfill\\centerline{\n" +
                    "\\begin{minipage}[b][4.500000in][t]{1.500000in}% column 1\n" +
                    "%Empty fragment Fragment empty@[0.0-4.5]\n" +
                    "\\end{minipage}% column 1\n" +
                    "\\hbox to 0.125000in{\\vbox to 4.500000in{% halley\n" +
                    "\\halleygap{4.500000in}%\n" +
                    "}}% halley\n" +
                    "\\begin{minipage}[b][4.500000in][t]{1.500000in}% column 2\n" +
                    "%Empty fragment Fragment empty@[0.0-4.5]\n" +
                    "\\end{minipage}% column 2\n" +
                    "\\hbox to 0.125000in{\\vbox to 4.500000in{% halley\n" +
                    "\\halleygap{4.500000in}%\n" +
                    "}}% halley\n" +
                    "\\begin{minipage}[b][4.500000in][t]{1.500000in}% column 3\n" +
                    "%Empty fragment Fragment empty@[0.0-4.5]\n" +
                    "\\end{minipage}% column 3\n" +
                    "}\\vfill\n", sw.toString());
        }
    }

    /**
     * Write various fragments to the document, and verify the output.
     *
     * @throws IOException unexpected, as we should not touch any external systems.
     */
    @Test
    void write_fragments() throws IOException {
        Col col1 = page.getColumns().get(0);
        Col col2 = page.getColumns().get(1);
        col1.set(col1.new ColFragment(new Valley(settings, 1), 0));
        String fixedContent = "%#Type: fixed\n%#Width: 2in\n%#Height: 2in";
        FixedSize fixed;
        try (BufferedReader br = new BufferedReader(new StringReader(fixedContent))) {
            fixed = (FixedSize) new InputFactory(settings, new MockLengthCalculator(), new CapturingLogger()).newInput(MockPath.createMockPathWithNameAndContent("fixed.tex", fixedContent), settings, br);
        }
        String artContent = "%#Type: article";
        Article art;
        try (BufferedReader br = new BufferedReader(new StringReader(artContent))) {
            art = (Article) new InputFactory(settings, new MockLengthCalculator(), new CapturingLogger()).newInput(MockPath.createMockPathWithNameAndContent("art.tex", artContent), settings, br);
        }
        Col.ColFragment frag2 = col1.new ColFragment(fixed, 0.125);
        col1.set(frag2);
        Col.ColFragment frag3 = col1.new ColFragment(art.splitArticle(1.5), frag2.end());
        col1.set(frag3);

        col2.set(col1.new ColFragment(art.splitRemainingArticle(settings.getColumnHeight()), 0));
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(new BufferedWriter(sw))) {
            page.write(pw, settings.getOut());
            pw.flush();
            Assertions.assertAll(
                    () -> Assertions.assertAll(
                            "Column 1",
                            () -> Assertions.assertTrue(sw.toString().contains("\\begin{minipage}[b][4.500000in][t]{1.500000in}% column 1\n" +
                                    "\\begin{minipage}[b][0.125000in][t]{1.500000in}%Fragment (no input)\n" +
                                    "\\valley[1.500000in]%\n" +
                                    "\\end{minipage}\\par% Fragment (no input)\n" +
                                    "\\nointerlineskip%\n" +
                                    "\\begin{minipage}[b][2.000000in][t]{1.500000in}%Fragment fixed.tex\n" +
                                    "% \\verb!fixed.tex!\n" +
                                    "\\hspace*{0.562500in}\\hbox to 2.000000in{%\n" +
                                    "\\linewidth=2.000000in%\n" +
                                    "\\columnwidth=2.000000in%\n" +
                                    "\\setemergencystretch\\numnewscols\\hsize\n" +
                                    "%#Type: fixed\n" +
                                    "%#Width: 2in\n" +
                                    "%#Height: 2in\n" +
                                    "}% end hbox for fixed content\n" +
                                    "\\hspace*{0.562500in}\n" +
                                    "\\end{minipage}\\par% Fragment fixed.tex\n" +
                                    "\\nointerlineskip%\n" +
                                    "\\begin{minipage}[b][1.500000in][t]{1.500000in}%Fragment art.tex\n" +
                                    "% art.tex part 1 of 2\n" +
                                    "\\splitbox{art.tex}{1.500000in}%\n" +
                                    "\\typeout{Column 1: To fill height 1.500000in, box height=\\the\\htsplitbox{art.tex}{1} remaining=\\the\\htsplitbox{art.tex}{2}}\n" +
                                    "\\usesplitbox{art.tex}{1}% target=1.500000in\n" +
                                    "\\end{minipage}\\par% Fragment art.tex\n" +
                                    "\\nointerlineskip%\n" +
                                    "%Empty fragment Fragment empty@[3.625-4.5]\n" +
                                    "\\end{minipage}% column 1\n"), sw::toString)),
                    () -> Assertions.assertAll("Column 2",
                            () ->
                            Assertions.assertTrue(sw.toString().contains("\\begin{minipage}[b][4.500000in][t]{1.500000in}% column 2\n" +
                                    "\\begin{minipage}[b][3.141500in][t]{1.500000in}%Fragment art.tex\n" +
                                    "% art.tex part 2 of 2\n" +
                                    "\\typeout{Column 2: To fill height 3.141500in, box height=\\the\\htsplitbox{art.tex}{2} at end of article}\n" +
                                    "\\usesplitbox{art.tex}{2}% target=3.141500in\n" +
                                    "\\end{minipage}\\par% Fragment art.tex\n" +
                                    "\\nointerlineskip%\n" +
                                    "%Empty fragment Fragment empty@[3.1415-4.5]\n" +
                                    "\\end{minipage}% column 2\n"), sw::toString))
            );
        }

    }


    @Test
    void testToString() {
        Assertions.assertEquals("PAGE 1\n" +
                " Column1:[Fragment empty@[0.0-4.5]]\n" +
                " Column2:[Fragment empty@[0.0-4.5]]\n" +
                " Column3:[Fragment empty@[0.0-4.5]]\n", page.toString());
    }
}