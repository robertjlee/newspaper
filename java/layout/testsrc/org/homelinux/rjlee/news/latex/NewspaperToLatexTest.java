package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.LaidOut;
import org.homelinux.rjlee.news.elements.ArticleFragment;
import org.homelinux.rjlee.news.input.ArticleImpl;
import org.homelinux.rjlee.news.input.Headers;
import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.rendered.Col;
import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NewspaperToLatexTest {
    private CapturingLogger logger;

    @BeforeEach
    void setUp() {
        logger = new CapturingLogger();
    }

    @Test
    void writeEmptyTexFile() {
        Settings settings = new Settings(new Properties());
        NewspaperToLatex ntl = new NewspaperToLatexImpl(settings, logger, new LatexProcessFactory());
        LaidOut laidOut = new MyLaidOut();
        ntl.writeTexFile(laidOut);
        String expected = "\\documentclass{article}\n" +
                "\\NeedsTeXFormat{LaTeX2e}[2020/02/02]\n" +
                "\\setlength{\\paperwidth}{25.590551in}\n" +
                "\\setlength{\\paperheight}{29.527559in}\n" +
                "\\usepackage[text={25.590551in,29.527559in},margin=0pt]{geometry}\n" +
                "\\ifcsname pdfpagewidth\\endcsname\\pdfpagewidth=25.590551in\\fi\n" +
                "\\ifcsname pdfpageheight\\endcsname\\pdfpageheight=29.527559in\\fi\n" +
                "\\usepackage[british]{babel}\n" +
                "\\setlength{\\textwidth}{25.590551in}\n" +
                "\\setlength{\\textheight}{29.527559in}\n" +
                "\\newlength{\\columnheight}\\setlength{\\columnwidth}{1.500000in}\n" +
                "\\setlength{\\columnheight}{26.574803in}\n" +
                "\\newcommand{\\halleyline}[1]{\\hbox{\\makebox[0.125000in]{\\rule{0.012500in}{#1}}}\\vskip 0pt}\n" +
                "\\newcommand{\\halleygap}[1]{\\vspace*{#1}}\n" +
                "\\newcommand{\\valley}[1][1.500000in]{\\vspace*{0.056250in}\\rule{#1}{0.012500in}\\vspace*{0.056250in}}\n" +
                "\\makeatletter\n" +
                "\\renewcommand{\\rmdefault}{ptm}\n" +
                "\\renewcommand{\\ttdefault}{lmtt}\n" +
                "\\DeclareFontSeriesDefault[rm]{md}{m}\n" +
                "\\DeclareFontSeriesDefault[tt]{md}{lc}\n" +
                "\\newlength{\\parindentcopy}\\setlength{\\parindentcopy}{\\parindent}\n" +
                "\\usepackage{environ}[2014/05/04]\n" +
                "\\newbox\\footnotebox\n" +
                "\\def\\news@footnotetext#1{\n" +
                "  \\global\\setbox\\footnotebox\\vbox{\n" +
                "    \\unvbox\\footnotebox\\reset@font \\footnotesize \\interlinepenalty \\interfootnotelinepenalty \\splittopskip \\footnotesep \\splitmaxdepth \\dp \\strutbox \\floatingpenalty \\@MM \\hsize \\columnwidth \\@parboxrestore \\def \\@currentcounter {footnote}\\protected@edef \\@currentlabel {\\csname p@footnote\\endcsname \\@thefnmark }\\color@begingroup \\@makefntext {\\rule \\z@ \\footnotesep \\ignorespaces #1\\@finalstrut  \\strutbox }\\par \\color@endgroup }}\n" +
                "\\newcommand{\\dumpfootnotes}{%\n" +
                "  \\ifvoid\\footnotebox\\else%\n" +
                "    \\vskip \\skip\\footins%\n" +
                "    \\color@begingroup\\normalcolor%\n" +
                "    \\footnoterule%\n" +
                "    \\unvbox\\footnotebox%\n" +
                "    \\color@endgroup%\n" +
                "  \\fi%\n" +
                "}\n" +
                "\\let\\@footnotetext\\news@footnotetext\n" +
                "\\let\\mp@footnotetext\\news@footnotetext\n" +
                "\\global\\newbox\\sb@tmp@box%\n" +
                "\\NewEnviron{newsplitbox}[1]{%\n" +
                "  \\global\\expandafter\\newbox\\csname sb@1@#1\\endcsname%\n" +
                "  \\global\\expandafter\\setbox\\csname sb@1@#1\\endcsname=\\vbox{\\BODY\\dumpfootnotes}%\n" +
                "  \\newcounter{sb@#1}%\n" +
                "  \\setcounter{sb@#1}{1}%\n" +
                "}\n" +
                "\\newcommand{\\usesplitbox}[2]{%\n" +
                "  \\ifcsname sb@#2@#1\\endcsname%\n" +
                "    \\expandafter\\copy\\csname sb@#2@#1\\endcsname%\n" +
                "  \\else%\n" +
                "    \\typeout{No such split box #1[#2]; indexes start at 1}%\n" +
                "  \\fi%\n" +
                "}\n" +
                "\\newcommand{\\htsplitbox}[2]{\\expandafter\\ht\\csname sb@#2@#1\\endcsname}\n" +
                "\\newcommand{\\dpsplitbox}[2]{\\expandafter\\dp\\csname sb@#2@#1\\endcsname}\n" +
                "\\newcommand{\\splitbox}[2]{%\n" +
                "  \\typeout{Splitting box #1 by #2}%\n" +
                "  \\edef\\sbcnt{\\arabic{sb@#1}@#1}%\n" +
                "  \\edef\\boxfrom{\\csname sb@\\sbcnt\\endcsname}%\n" +
                "  \\stepcounter{sb@#1}%\n" +
                "  \\edef\\sbcnt{\\arabic{sb@#1}@#1}%\n" +
                "  \\edef\\boxto{\\csname sb@\\sbcnt\\endcsname}%\n" +
                "  \\expandafter\\newbox\\boxto%\n" +
                "  \\global\\expandafter\\setbox\\sb@tmp@box=\\vbox{%\n" +
                "    \\expandafter\\vsplit\\boxfrom to #2%\n" +
                "  }%\n" +
                "  \\global\\expandafter\\setbox\\boxto=\\copy\\boxfrom%\n" +
                "  \\global\\expandafter\\setbox\\boxfrom=\\copy\\sb@tmp@box%\n" +
                "}\n" +
                "\\newread\\input@read\n" +
                "\\newwrite\\input@write\n" +
                "{\n" +
                "  \\catcode`\\%=12\\relax\n" +
                "  \\xdef\\commentchar{%}\n" +
                "}\n" +
                "\\newcommand*{\\eifstartswith}{\\@expandtwoargs\\ifstartswith}\n" +
                "\\newcommand*{\\ifstartswith}[2]{%\n" +
                "  \\if\\@car#1.\\@nil\\@car#2.\\@nil\n" +
                "    \\expandafter\\@firstoftwo\n" +
                "  \\else\n" +
                "    \\expandafter\\@secondoftwo\n" +
                "  \\fi}\n" +
                "\\newcommand\\inputPlainStripComments[1]{%\n" +
                "  \\find@file{#1}{\\expandafter\\plain@setcatcodes\\plain@input}{\\commentchar}%\n" +
                "}%\n" +
                "\\newcommand\\inputMdStripComments[1]{%\n" +
                "  \\find@file{#1}{\\markdownInput{\\jobname-pipe.tmp}}{\\commentchar}%\n" +
                "}%\n" +
                "\\newcommand\\inputMd[1]{%\n" +
                "  \\find@file{#1}{\\markdownInput{\\jobname-pipe.tmp}}{\\relax}%\n" +
                "}%\n" +
                "\\newcommand{\\input@processfile}[3]{%\n" +
                "  \\openin\\input@read #1\\relax%\n" +
                "  \\ifeof\\input@read%\n" +
                "    \\closein\\input@read%\n" +
                "  \\else%\n" +
                "    \\closein\\input@read%\n" +
                "    \\input@processfile@{#1}{#2}{#3}%\n" +
                "  \\fi%\n" +
                "}\n" +
                "\\newcommand{\\input@processfile@}[3]{%\n" +
                "  \\openin\\input@read #1\\relax%\n" +
                "  \\begingroup%\n" +
                "  \\endlinechar\\newlinechar%\n" +
                "  \\immediate\\openout\\input@write=\\jobname-pipe.tmp%\n" +
                "  \\@whilesw\\unless\\ifeof\\input@read\\fi{%%\n" +
                "    \\endlinechar=-1%\n" +
                "    \\readline\\input@read to\\input@line%\n" +
                "    \\ifeof\\input@read\\else%\n" +
                "      \\eifstartswith{#3}{\\input@line}{}{%\n" +
                "      \\immediate\\write\\input@write{\\input@line}%\n" +
                "      }%\n" +
                "    \\fi%\n" +
                "  }%\n" +
                "  \\closein\\input@read%\n" +
                "  \\immediate\\closeout\\input@write%\n" +
                "  \\expandafter\\endgroup%\n" +
                "  \\begingroup#2\\endgroup%\n" +
                "}\n" +
                "\\ExplSyntaxOn\n" +
                "\\newcommand{\\find@file}[3]{\n" +
                "  \\file_get_full_name:nNTF\n" +
                "       {#1}\n" +
                "       \\l_tmpa_tl\n" +
                "         {\n" +
                "           \\input@processfile{\\l_tmpa_tl}{#2}{#3}\n" +
                "         }\n" +
                "         {\n" +
                "           \\errmessage{File `#1' missing!}%\n" +
                "         }\n" +
                "}\\ExplSyntaxOff\n" +
                "\\def\\plain@input{\\input{\\jobname-pipe.tmp}}\n" +
                "\\def\\plain@setcatcodes{\n" +
                "    \\catcode`\\%=12\\relax\n" +
                "    \\catcode`$=12\\relax\n" +
                "    \\catcode`&=12\\relax\n" +
                "    \\catcode`\\#=12\\relax\n" +
                "    \\catcode`^=12\\relax\n" +
                "    \\catcode`_=12\\relax\n" +
                "    \\catcode`~=12\\relax\n" +
                "    \\catcode`{=12\\relax\n" +
                "    \\catcode`}=12\\relax\n" +
                "    \\catcode`\\\\=12\\relax\n" +
                "}\n" +
                "\\def\\@startsection#1#2#3#4#5#6{%\n" +
                "  \\if@noskipsec \\leavevmode \\fi\n" +
                "  \\par\n" +
                "  \\@tempskipa #4\\relax\n" +
                "  \\@afterindenttrue\n" +
                "  \\ifdim \\@tempskipa <\\z@\n" +
                "    \\@tempskipa -\\@tempskipa \\@afterindentfalse\n" +
                "  \\fi\n" +
                "  \\if@nobreak\n" +
                "    \\everypar{}%\n" +
                "  \\else\n" +
                "  \\addpenalty\\@secpenalty%\n" +
                "  \\@tempskipa 2pt plus 1pt minus 1pt\\relax\n" +
                "  \\addvspace\\@tempskipa\n" +
                "  \\fi\n" +
                "  \\@ifstar\n" +
                "    {\\@ssect{#3}{#4}{#5}{#6}}%\n" +
                "    {\\@ssect{#3}{#4}{#5}{#6}}%\n" +
                "}\n" +
                "\\usepackage[fontsize=10]{fontsize}[2021/08/04]\n" +
                "\\newlength\\headlineskip\\headlineskip=2pt plus 2pt\\relax\n" +
                "\\newcommand{\\headline}[1]{\\vbox{\\topsep=0pt\\parsep=0pt\\begin{center}\\fontseries{bx}\\fontsize{18}{20}\\selectfont #1\\vspace{\\headlineskip}\\end{center}}}\n" +
                "\\newcommand{\\continuedOn}[1]{\\nointerlineskip\\vfill\\def\\otherpage{#1}\\makebox[\\textwidth]{\\hfill\\textit{\\scriptsize Continued on page \\otherpage\\dots\\hspace{-1em}}}}\n" +
                "\\newcommand{\\continuedFrom}[1]{\\vbox{\\def\\otherpage{#1}\\makebox[\\textwidth]{\\textit{\\scriptsize\\hspace{-1em}\\dots continued from page \\otherpage}\\hfill}}\\nointerlineskip}\n" +
                "\\makeatother\n" +
                "\\usepackage{indentfirst}\n" +
                "\\usepackage[british]{babel}\n" +
                "\\usepackage[utf8]{inputenc}\n" +
                "\\usepackage{newtxmath,newtxtext}\n" +
                "\\usepackage{csquotes}\n" +
                "\\usepackage[TU]{fontenc}\n" +
                "\\typeout{Preamble line!}\n" +
                "\n" +
                "\\setlength{\\hfuzz}{\\maxdimen}\n" +
                "\\setlength{\\vfuzz}{\\maxdimen}\n" +
                "\\newcount\\nmulticoltolerance \\nmulticoltolerance=500\n" +
                "\\def\\setemergencystretch#1#2{\\emergencystretch=0.1\\hsize}\n" +
                "\\def\\numnewscols{15}\n" +
                "\\begin{document}\n" +
                "\\makeatletter\\vbadness\\@Mi \\hbadness5000 \\tolerance\\nmulticoltolerance\\makeatother\n" +
                "\\end{document}\n";
        String actual = logger.texOutputCollected();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void writeOutputNoAlleyLines() {
        Properties props = new Properties();
        props.put("alleyWidth", "0");
        Settings settings = new Settings(props);
        NewspaperToLatex ntl = new NewspaperToLatexImpl(settings, logger, new LatexProcessFactory());
        LaidOut laidOut = new MyLaidOut();
        ntl.writeTexFile(laidOut);
        String actual = logger.texOutputCollected();
        Assertions.assertTrue(actual.contains(String.format("\\newcommand{\\valley}[1][%fin]{\\vspace*{%fin}}\n", settings.getColumnWidth(), settings.getAlleyHeight())));
    }

    @Test
    void finalLayout_blank() throws IOException {
        Settings settings = new Settings(new Properties());
        StringBuilder stdOut = new StringBuilder("Tex response line here");
        StringBuilder stdErr = new StringBuilder();
        try (ByteArrayOutputStream stdIn = new ByteArrayOutputStream()) {
            MockShellProcessFactory processFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn);
            NewspaperToLatex ntl = new NewspaperToLatexImpl(settings, logger, processFactory);
            ntl.compileFinalPdf();
            assertAll(
                    () -> Assertions.assertEquals("", logger.quietCollected()),
                    () -> Assertions.assertEquals("Tex response line here\n", logger.dumpAllCollected())
            );
        }
    }

    @Test
    void finalLayout_twoPages() throws IOException {
        Settings settings = new Settings(new Properties());
        StringBuilder stdOut = new StringBuilder("Tex response line here");
        StringBuilder stdErr = new StringBuilder();
        try (ByteArrayOutputStream stdIn = new ByteArrayOutputStream()) {
            MockShellProcessFactory processFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn);
            NewspaperToLatex ntl = new NewspaperToLatexImpl(settings, logger, processFactory);
            LaidOut laidOut = new MyLaidOut();
            addPage(settings, laidOut);
            addPage(settings, laidOut);
            ntl.writeTexFile(laidOut);
            ntl.compileFinalPdf();
            assertAll(
                    () -> Assertions.assertEquals("", logger.quietCollected()),
                    () -> Assertions.assertEquals("Tex response line here\n", logger.dumpAllCollected())
            );
        }
    }

    private static void addPage(Settings settings, LaidOut laidOut) {
        Page p1 = new Page(0, 1, settings);
        laidOut.getPages().add(p1);
        Col col = p1.getColumns().get(0);
        MockPath article = MockPath.createMockPathWithNameAndContent("art.tex", "%art");
        ArticleImpl a = new ArticleImpl(new Headers(article, new Properties(), settings), settings, new MockLengthCalculator());
        ArticleFragment articleFragment = a.splitArticle(settings.getColumnHeight() / 2);
        Col.ColFragment firstFragment = col.new ColFragment(articleFragment, 0, settings.getColumnHeight() / 2, null);
        col.set(firstFragment);
        col.set(col.new ColFragment(a.splitRemainingArticle(settings.getColumnHeight() - firstFragment.getEnd()), firstFragment.getEnd()));
    }

    @Test
    void finalLayout_crash() throws IOException {
        Settings settings = new Settings(new Properties());
        StringBuilder stdOut = new StringBuilder("Tex response line here");
        StringBuilder stdErr = new StringBuilder();
        try (ByteArrayOutputStream stdIn = new ByteArrayOutputStream()) {
            MockShellProcessFactory processFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn, false, true);
            NewspaperToLatex ntl = new NewspaperToLatexImpl(settings, logger, processFactory);
            ntl.compileFinalPdf();
            String quietCollected = logger.quietCollected();
            System.out.println("quietCollected = " + quietCollected);
            assertAll(
                    () -> Assertions.assertTrue(quietCollected.startsWith("newspaper.tex was generated, but the call to LaTeX failed.\n" +
                            "java.io.IOException: Error!\n"), quietCollected),
                    () -> Assertions.assertEquals("", logger.dumpAllCollected())
            );
        }
    }

    @Test
    void finalLayout_errorOnStdOut() throws IOException {
        Settings settings = new Settings(new Properties());
        StringBuilder stdOut = new StringBuilder("Tex response line here");
        StringBuilder stdErr = new StringBuilder();
        try (ByteArrayOutputStream stdIn = new ByteArrayOutputStream()) {
            MockShellProcessFactory processFactory = new MockShellProcessFactory(stdOut, stdErr, stdIn).withErrorOnStdOut();
            NewspaperToLatex ntl = new NewspaperToLatexImpl(settings, logger, processFactory);
            ntl.compileFinalPdf();
            String quietCollected = logger.quietCollected();
            System.out.println("quietCollected = " + quietCollected);
            assertAll(
                    () -> Assertions.assertTrue(quietCollected.startsWith("newspaper.tex LaTeX call invoked, but there was an error reading the output.\n" +
                            "java.io.UncheckedIOException: java.io.IOException: Error!"), quietCollected),
                    () -> Assertions.assertEquals("", logger.dumpAllCollected())
            );
        }
    }

    @Test
    void write_fileIO() {
        logger.errorOnTexOutput();
        Settings settings = new Settings(new Properties());
        NewspaperToLatex ntl = new NewspaperToLatexImpl(settings, logger, new LatexProcessFactory());
        LaidOut laidOut = new MyLaidOut();
        assertThrows(RuntimeException.class, () -> ntl.writeTexFile(laidOut));
        String actual = logger.texOutputCollected();
        Assertions.assertEquals("", actual);
    }

    private static class MyLaidOut implements LaidOut {
        private List<Page> pages = new ArrayList<>();

        @Override
        public Stream<String> preambleLines() {
            return Stream.of("\\typeout{Preamble line!}");
        }

        @Override
        public List<Page> getPages() {
            return pages;
        }
    }

}