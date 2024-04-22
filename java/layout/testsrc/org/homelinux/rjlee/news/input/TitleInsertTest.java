package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Properties;

class TitleInsertTest {

    private TitleInsert titleInsert;
    private MockPath path;
    private Properties headerProps;

    @BeforeEach
    void setUp() {
        path = MockPath.createMockPathWithNameAndContent("settings.properties", "Newspaper Title Test");
        headerProps = new Properties();
        headerProps.put("FontEncoding", "T1"); // Almendra isn't available in TU!
        Settings defaultSettings = new Settings(new Properties());
        titleInsert = new TitleInsert(new Headers(path, headerProps, defaultSettings), defaultSettings);
    }

    @Test
    void test() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             BufferedOutputStream os = new BufferedOutputStream(out);
             PrintWriter pw = new PrintWriter(os)) {
            titleInsert.copyTo(pw, path);
            pw.flush();
            String today = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
            Assertions.assertEquals("\\newbox\\titlebox\\setbox\\titlebox=\\hbox{\\fontencoding{T1}\\fontfamily{\\almendrafamily}\\fontseries{b}\\fontshape{n}\\fontsize{54}{56}\\selectfont\n" +
                    "\\setemergencystretch\\numnewscols\\hsize\n" +
                    "Newspaper Title Test\n" +
                    "}\n" +
                    "\\vbox{\\hbox to 25.590551in{\\raisebox{0.138370in}[0pt][0pt]{\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{8}{10}\\selectfont "+today+"}\\hfill \\raisebox{0.138370in}[0pt][0pt]{\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{8}{10}\\selectfont }}\n" +
                    "\\hbox{\n" +
                    "\\makebox[25.590551in]{\n" +
                    "    \\hfill\n" +
                    "  {\\fontencoding{T1}\\fontfamily{\\almendrafamily}\\fontseries{b}\\fontshape{n}\\fontsize{54}{56}\\selectfont\n" +
                    "\\raisebox{0.000000in}[\\ht\\titlebox][\\dp\\titlebox]{\\usebox\\titlebox}\n" +
                    "}\n" +
                    "  \\hfill\n" +
                    "  }\n" +
                    "  }\n" +
                    "  \\vspace{0.400000in}\n" +
                    "  \\hbox to 25.590551in{\n" +
                    "    \\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{8}{10}\\selectfont \\hfill{\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{8}{10}\\selectfont }\n" +
                    "}}\n", out.toString("UTF-8"));
        }
    }

    @Test
    void withOverrides() throws IOException {
        headerProps.put("Price", "Price: 10p");
        headerProps.put("LeftBox", "x");
        headerProps.put("RightBox", "y");
        headerProps.put("FontFamily", "\\rmdefault"); // why not put the title in Times?
        headerProps.put("PriceCommand", "\\small");
        headerProps.remove("FontEncoding");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             BufferedOutputStream os = new BufferedOutputStream(out);
             PrintWriter pw = new PrintWriter(os)) {
            titleInsert.copyTo(pw, path);
            pw.flush();
            String today = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
            Assertions.assertEquals("\\newbox\\titlebox\\setbox\\titlebox=\\hbox{\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{b}\\fontshape{n}\\fontsize{54}{56}\\selectfont\n" +
                    "\\setemergencystretch\\numnewscols\\hsize\n" +
                    "Newspaper Title Test\n" +
                    "}\n" +
                    "\\vbox{\\hbox to 25.590551in{\\raisebox{0.138370in}[0pt][0pt]{\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{8}{10}\\selectfont " + today + "}\\hfill \\raisebox{0.138370in}[0pt][0pt]{\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{8}{10}\\selectfont }}\n" +
                    "\\hbox{\n" +
                    "\\makebox[25.590551in]{\n" +
                    "  \\raisebox{0pt}[0pt][0pt]{\\framebox[1.000000in][c]{\\rule[-0.450000in]{0pt}{1.000000in}\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{10}{12}\\selectfont{}x}}  \\hfill\n" +
                    "  {\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{b}\\fontshape{n}\\fontsize{54}{56}\\selectfont\n" +
                    "\\raisebox{0.000000in}[\\ht\\titlebox][\\dp\\titlebox]{\\usebox\\titlebox}\n" +
                    "}\n" +
                    "  \\hfill\n" +
                    "  \\raisebox{0pt}[0pt][0pt]{\\framebox[1.000000in][c]{\\rule[-0.450000in]{0pt}{1.000000in}\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{10}{12}\\selectfont{}y}}}\n" +
                    "  }\n" +
                    "  \\vspace{0.400000in}\n" +
                    "  \\hbox to 25.590551in{\n" +
                    "    \\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{m}\\fontshape{n}\\fontsize{8}{10}\\selectfont \\hfill{\\small Price: 10p}\n" +
                    "}}\n", out.toString("UTF-8"));
        }

    }
}