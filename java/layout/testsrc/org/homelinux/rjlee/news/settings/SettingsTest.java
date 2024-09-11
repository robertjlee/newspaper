package org.homelinux.rjlee.news.settings;

import org.homelinux.rjlee.news.CmdLineOptions;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.homelinux.rjlee.news.parsing.LengthParser.readLength;
import static org.junit.jupiter.api.Assertions.*;

class SettingsTest {

    public static final String DEFAULT_SETTINGS = "Settings{version=0.0.1, pageWidth=25.590551181102363, pageHeight=29.52755905511811, columnWidth=1.5, " +
            "columnHeight=26.5748031496063, alleyWidth=0.125, alleyHeight=0.125, alleyThickWidth=0.0125, " +
            "alleyThickHeight=0.0125, maxSquashVSpace=0.0, columnStrategy=BALANCE, minSideMargins=0.125, defaultFontEncoding=TU, defaultFontSize=10, defaultFontSizeClo=null, defaultFontFamily=ptm, defaultFontSeries=m, defaultTeletypeFamily=lmtt, defaultTeletypeSeries=lc, tolerance=500, emergencyStretch=\\emergencystretch=0.1\\hsize, " +
            "inputFilters=[.tex, .md, .txt, .text], out=out, " +
            "jobName=newspaper, lengthsCache=lengths.cache, texinputs=:, latex='pdflatex', latexCmdline=[--interaction=nonstopmode], " +
            "extraPreambleLines=[\\usepackage{indentfirst}, \\usepackage[british]{babel}, \\usepackage[utf8]{inputenc}, \\usepackage{newtxmath,newtxtext}, \\usepackage{csquotes}, \\usepackage[TU]{fontenc}], " +
            "markdown=\\usepackage[smartEllipses,fancyLists]{markdown}, continuedOnPageText=\\makebox[\\textwidth]{\\hfill\\textit{\\scriptsize Continued on page \\otherpage\\dots\\hspace{-1em}}}, continuedFromPageText=\\makebox[\\textwidth]{\\textit{\\scriptsize\\hspace{-1em}\\dots continued from page \\otherpage}\\hfill}, " +
            "logFile=layout.log, stdOutLevel=ELEMENTS, stdErrLevel=SILENT, logFileLevel=ALGORITHM, " +
            "headerFont=\\fontencoding{TU}\\fontfamily{\\rmdefault}\\fontseries{bc}\\fontshape{n}\\fontsize{18}{20}\\selectfont, " +
            "allowTexFileOverwrite=false, inputWithoutCopy=false, defaultFontFamilyFromHeaders=false, enableLaTeXHooks=false}";

    private int returnValue = Integer.MIN_VALUE; // not called

    @Test
    void defaults() {
        Settings s = new Settings(new Properties());

        assertEquals(DEFAULT_SETTINGS, s.toString());
    }

    @Test
    void parse() {
        Properties p = new Properties();
        p.put("version", "0.0.2");
        p.put("pageWidth", "1.1in");
        p.put("pageHeight", "2.2in");
        p.put("columnWidth", "3.3in");
        p.put("columnHeightRatioOfPage", "4.4");
        p.put("columnHeight", "5.5in");
        p.put("alleyWidth", "6.6in");
        p.put("alleyHeight", "7.7in");
        p.put("alleyThickWidth", "8.8in");
        p.put("alleyThickHeight", "9.9in");
        p.put("minSideMargins", "10.10in");
        p.put("defaultFontEncoding", "T1");
        p.put("defaultFontFamily", "cmr");
        p.put("defaultFontSeries", "it");
        p.put("defaultFontSize", "14");
        p.put("defaultFontSizeClo", "sizes");
        p.put("defaultFontFamilyFromHeaders", "true");
        p.put("defaultTeletypeFamily", "cmr");
        p.put("defaultTeletypeSeries", "it");
        p.put("columnStrategy", "fillFirst");
        p.put("out", "path2");
        p.put("jobName", "news");
        p.put("lengthsCache", "len.cache");
        p.put("latex", "/path/to/lualatex");
        p.put("latexCmdLine", "--interaction=nonstopmode --jobname=newspaper");
        p.put("preamble!01head", "\\usepackage{babel}");// us english hyphenation
        p.put("preamble1", "\\newlength{mylen}"); // plus an extra length
        p.put("markdown", "\\usepackage[smartEllipsis=true]{markdown}");
        p.put("tolerance", "1000");
        p.put("allowTexFileOverwrite", "TrUe");
        p.put("inputWithoutCopy", "tRuE");
        p.put("logFile", "log.txt");
        p.put("stdOutLevel", "QUIET");
        p.put("stdErrLevel", "DUMP_ALL");
        p.put("logFileLevel", "ELEMENTS");
        p.put("headCommand", "\\null");
        p.put("inputFilter", ", .mDown, .mUp,,");
        p.put("emergencyStretch", "{}");
        p.put("continuedOnPageText", "(Ctd. page \\otherpage)\\hfill");
        p.put("continuedFromPageText", "\\hfill(From page \\otherpage)");
        p.put("enableLaTeXHooks", "truE");
        p.put("maxSquashVSpace", "20in");

        Settings s = new Settings(p);
        assertEquals("Settings{version=0.0.2, pageWidth=1.1, pageHeight=2.2, columnWidth=3.3, columnHeight=5.5, " +
                "alleyWidth=6.6, alleyHeight=7.7, alleyThickWidth=8.8, alleyThickHeight=9.9, maxSquashVSpace=20.0, columnStrategy=FILLFIRST, minSideMargins=10.1, " +
                "defaultFontEncoding=T1, defaultFontSize=14, defaultFontSizeClo=sizes, defaultFontFamily=cmr, defaultFontSeries=it, defaultTeletypeFamily=cmr, defaultTeletypeSeries=it, " +
                "tolerance=1000, emergencyStretch={}, " +
                "inputFilters=[.mDown, .mUp], out=path2, jobName=news, lengthsCache=len.cache, texinputs=:, latex='/path/to/lualatex', " +
                "latexCmdline=[--interaction=nonstopmode, --jobname=newspaper], extraPreambleLines=[\\usepackage{indentfirst}, \\usepackage{babel}, \\usepackage[utf8]{inputenc}, \\usepackage{newtxmath,newtxtext}, \\usepackage{csquotes}, \\newlength{mylen}, \\usepackage[T1]{fontenc}], " +
                "markdown=\\usepackage[smartEllipsis=true]{markdown}, continuedOnPageText=(Ctd. page \\otherpage)\\hfill, continuedFromPageText=\\hfill(From page \\otherpage), " +
                "logFile=log.txt, stdOutLevel=QUIET, stdErrLevel=DUMP_ALL, logFileLevel=ELEMENTS, " +
                "headerFont=\\null, " +
                "allowTexFileOverwrite=true, inputWithoutCopy=true, defaultFontFamilyFromHeaders=true, enableLaTeXHooks=true}", s.toString());
    }

    @ParameterizedTest
    @CsvSource(textBlock = "0.0.1, 0.0\n0.0.2, 0.4\n")
    void maxSquishVSpaceDefault(SemVer version, double maxSquashVSpaceDefault) {
        Properties p = new Properties();
        p.put("version", version.toString());
        Settings s = new Settings(p);
        double actual = s.getMaxSquashVSpace();
        assertEquals(maxSquashVSpaceDefault, actual, 0.00001);
    }

    @Test
    void heightRatio() {
        Properties p = new Properties();
        p.put("pageHeight", "10in");
        p.put("columnHeightRatioOfPage", ".75");

        Settings s = new Settings(p);
        assertEquals(7.5, s.getColumnHeight(), 0.00001);
    }

    @Test
    void overriding() {

        Path srcDir1 = MockPath.createMockDirectoryWithSettingsFile("pageHeight = 4.5in\npageWidth = 10");
        Path srcDir2 = MockPath.createMockDirectoryWithSettingsFile("pageWidth = 5.4in");

        Settings s = Settings.build(new CmdLineOptions(System.out, srcDir1, srcDir2), x -> returnValue = x);
        assertAll(
                () -> assertEquals(4.5, s.getPageHeight(), 0.0),
                () -> assertEquals(5.4, s.getPageWidth(), 0.0),
                () -> assertEquals(Integer.MIN_VALUE, returnValue, "System.exit should not be called!")
        );
    }

    @Test
    void missingSettingsFile() {

        Path srcDir1 = MockPath.createMockDirectoryWithMissingSettingsFile("pageHeight = 4.5in\npageWidth = 10");

        Settings settings = Settings.build(new CmdLineOptions(System.out, srcDir1), x -> returnValue = x);
        assertAll(
                () -> assertEquals(DEFAULT_SETTINGS, settings.toString()),
                () -> assertEquals(Integer.MIN_VALUE, returnValue, "System.exit should not be called!")
        );
    }

    @Test
    void ioErrorOnSettingsFile() {

        Path srcDir1 = MockPath.createMockDirectoryWithSettingsFileIOError();

        assertAll(
                () -> assertDoesNotThrow(() -> Settings.build(new CmdLineOptions(System.out, srcDir1), x -> returnValue = x), "Should be no exception because System.exit should be invoked instead."),
                () -> assertEquals(-1, returnValue, "System.exit should be called before we try and use a broken filesystem.")
        );
    }

    @Test
    void readEnum_Error() {
        Properties properties = new Properties();
        properties.put("foo", "invalid");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Settings.readEnum(properties, "foo", DebugLevel.class, DebugLevel.DUMP_ALL));
        assertEquals("Bad setting value [invalid] for [foo]; expected one of [SILENT];[QUIET];[ELEMENTS];[ALGORITHM];[DUMP_ALL]", ex.getMessage());
    }


    @Test
    void toleranceInvalid() {
        Properties p = new Properties();
        p.put("tolerance", "in");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Settings(p));
        assertEquals("Bad setting value [in] for [tolerance]; not a valid Integer. Must be in range 0-10000", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 10001})
    void toleranceOutOfRange(int value) {
        Properties p = new Properties();
        p.put("tolerance", "" + value);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Settings(p));
        assertEquals("Bad setting value [" + value + "] for [tolerance]; not in range 0-10000", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource(textBlock =
            "        1,1,0  ,0,1\n" +
                    "2,1,0  ,0,2\n" +
                    "5,1,0.5,0,3\n" +
                    "4,1,0.5,0.1,2")
    void getMaxColsPerPage(double pageWidth, double columnWidth, double alleyWidth, double minSideMargins, long expected) {
        Properties p = new Properties();
        p.put("pageWidth", pageWidth + "in");
        p.put("columnWidth", columnWidth + "in");
        p.put("alleyWidth", alleyWidth + "in");
        p.put("minSideMargins", minSideMargins + "in");
        assertEquals(expected, new Settings(p).getMaxColsPerPage());

    }

    @ParameterizedTest
    @EnumSource(PageSizeCode.class)
    void pageSize(PageSizeCode code) {
        Properties p = new Properties();
        p.put("pageSize", code.name().toLowerCase());
        Settings settings = new Settings(p);
        assertAll(
                () -> assertEquals(readLength(code.getWidth()), settings.getPageWidth()),
                () -> assertEquals(readLength(code.getHeight()), settings.getPageHeight())
        );
    }

    @Test
    void pageWidthOverridesPageSize() {
        Properties p = new Properties();
        p.put("pageWidth", "5in");
        p.put("pageSize", PageSizeCode.A2);
        assertEquals(5, new Settings(p).getPageWidth());
    }

    @Test
    void pageWidthHeightOverridesPageSize() {
        Properties p = new Properties();
        p.put("pageHeight", "5in");
        p.put("pageSize", PageSizeCode.A2);
        assertEquals(5, new Settings(p).getPageHeight());
    }

    @Test
    void getPageWidth() {
        assertEquals(25.590551181102363, new Settings(new Properties()).getPageWidth()); // 650mm in in
    }

    @Test
    void getPageHeight() {
        assertEquals(29.52755905511811, new Settings(new Properties()).getPageHeight()); // 750mm in in
    }

    @Test
    void getColumnWidth() {
        assertEquals(1.5, new Settings(new Properties()).getColumnWidth());
    }

    @Test
    void getColumnHeight() {
        assertEquals(26.5748031496063, new Settings(new Properties()).getColumnHeight()); // 90% of 750mm in in
    }

    @Test
    void getAlleyWidth() {
        assertEquals(0.125, new Settings(new Properties()).getAlleyWidth());
    }

    @Test
    void getAlleyHeight() {
        assertEquals(0.125, new Settings(new Properties()).getAlleyHeight());
    }

    @Test
    void getAlleyThickWidth() {
        assertEquals(0.0125, new Settings(new Properties()).getAlleyThickWidth());
    }

    @Test
    void getAlleyThickHeight() {
        assertEquals(0.0125, new Settings(new Properties()).getAlleyThickHeight());
    }

    @Test
    void getDefaultFontFamily() {
        assertEquals("ptm", new Settings(new Properties()).getDefaultFontFamily());
    }

    @Test
    void getTolerance() {
        assertEquals(500, new Settings(new Properties()).getTolerance());
    }

    @Test
    void getOut() {
        assertEquals("out", new Settings(new Properties()).getOut().toString());
    }

    @Test
    void getLogFile() {
        assertEquals("layout.log", new Settings(new Properties()).getLogFile().toString());
    }

    @Test
    void getJobName() {
        assertEquals("newspaper", new Settings(new Properties()).getJobName());
    }

    @Test
    void getLatex() {
        assertEquals("pdflatex", new Settings(new Properties()).getLatex());
    }

    @Test
    void getLatexCmdLine() {
        assertArrayEquals(new String[]{"--interaction=nonstopmode"}, new Settings(new Properties()).getLatexCmdLine());
    }

    @Test
    void getPreambleLines() {
        assertEquals(Arrays.asList(
                "\\usepackage{indentfirst}",
                "\\usepackage[british]{babel}",
                "\\usepackage[utf8]{inputenc}",
                "\\usepackage{newtxmath,newtxtext}",
                "\\usepackage{csquotes}",
                "\\usepackage[TU]{fontenc}"
        ), new Settings(new Properties()).preambleLines().collect(Collectors.toList()));
    }

    @Test
    void markdown() {
        Properties p = new Properties();
        p.put("markdown", "%");
        assertEquals("%", new Settings(p).getMarkdown());
    }

    @Test
    void getMinSideMargins() {
        assertEquals(0.125, new Settings(new Properties()).getMinSideMargins());
    }

    @Test
    void allowTexFileOverwrite_default() {
        assertFalse(new Settings(new Properties()).isAllowTexFileOverwrite());
    }

    @Test
    void allowTexFileOverwrite_set() {
        Properties p = new Properties();
        p.put("allowTexFileOverwrite", "true");
        assertTrue(new Settings(p).isAllowTexFileOverwrite());
    }

    @Test
    void inputWithoutCopy_default() {
        assertFalse(new Settings(new Properties()).isAllowTexFileOverwrite());
    }

    @Test
    void inputWithoutCopy_set() {
        Properties p = new Properties();
        p.put("inputWithoutCopy", "true");
        assertTrue(new Settings(p).isInputWithoutCopy());
    }

    @Test
    void getLogFileLevel() {
        assertSame(DebugLevel.ALGORITHM, new Settings(new Properties()).getLogFileLevel());
    }

    @Test
    void getStdOutLevel() {
        assertSame(DebugLevel.ELEMENTS, new Settings(new Properties()).getStdOutLevel());
    }

    @Test
    void getStdErrLevel() {
        assertSame(DebugLevel.SILENT, new Settings(new Properties()).getStdErrLevel());
    }

}