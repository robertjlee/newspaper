package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

class LatexInteractionTest {

    private String defaultAtPreamble =
                    "\\def\\input@path{{name/}}\n" +
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
                            "\\newcommand{\\stretchsplitbox}[3]{%\n" +
                            "  \\ifcsname sb@#2@#1\\endcsname%\n" +
                            "    \\vbox to #3{\\expandafter\\unvbox\\csname sb@#2@#1\\endcsname}%\n" +
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
                            "\\newcommand{\\continuedFrom}[1]{\\vbox{\\def\\otherpage{#1}\\makebox[\\textwidth]{\\textit{\\scriptsize\\hspace{-1em}\\dots continued from page \\otherpage}\\hfill}}\\nointerlineskip}\n";

    @Test
    void extract() {

        String haystack = "LEN:128pts";
        StringBuilder result = new StringBuilder();
        LatexInteraction.extract(haystack, "LEN", result);
        String expected = "128pts";
        assertEquals(expected, result.toString());
    }

    @Test
    void printAtPreamble() throws IOException {
        Settings settings = new Settings(new Properties(), MockPath.createMockPathWithName("name"));
        MockShellProcessFactory latexProcessFactory = new MockShellProcessFactory(new StringBuilder(), new StringBuilder(), new ByteArrayOutputStream());
        LatexInteraction li = new LatexLength(1.23, emptyList(), Stream.empty(), settings, latexProcessFactory, new CapturingLogger());
        StringWriter sw = new StringWriter();

        try (BufferedWriter writer = new BufferedWriter(sw);
             PrintWriter pw = new PrintWriter(writer)) {
            li.printAtPreamble(pw);
        }
        assertEquals("\\renewcommand{\\rmdefault}{ptm}\n" +
                "\\renewcommand{\\ttdefault}{lmtt}\n" +
                "\\DeclareFontSeriesDefault[rm]{md}{m}\n" +
                "\\DeclareFontSeriesDefault[tt]{md}{lc}\n" + defaultAtPreamble, sw.toString());
    }

    @Test
    void printAtPreambleWithoutFontFamily() throws IOException {
        Properties p = new Properties();
        p.put("defaultFontFamilyFromHeaders", "true");
        Settings settings = new Settings(p, MockPath.createMockPathWithName("name"));
        MockShellProcessFactory latexProcessFactory = new MockShellProcessFactory(new StringBuilder(), new StringBuilder(), new ByteArrayOutputStream());
        LatexInteraction li = new LatexLength(1.23, emptyList(), Stream.empty(), settings, latexProcessFactory, new CapturingLogger());
        StringWriter sw = new StringWriter();

        try (BufferedWriter writer = new BufferedWriter(sw);
             PrintWriter pw = new PrintWriter(writer)) {
            li.printAtPreamble(pw);
        }
        assertEquals(defaultAtPreamble, sw.toString());
    }

    @ParameterizedTest
    @ValueSource(ints = {2,3,4,5,6,7,8,9,10,11,12,13,14,15,16})
    void simplePixelSizes(int pixelSize) throws IOException {
        Properties p = new Properties();
        p.put("defaultFontSize", "" + pixelSize);
        Settings settings = new Settings(p, MockPath.createMockPathWithName("name"));
        MockShellProcessFactory latexProcessFactory = new MockShellProcessFactory(new StringBuilder(), new StringBuilder(), new ByteArrayOutputStream());
        LatexInteraction li = new LatexLength(1.23, emptyList(), Stream.empty(), settings, latexProcessFactory, new CapturingLogger());
        StringWriter sw = new StringWriter();

        try (BufferedWriter writer = new BufferedWriter(sw);
             PrintWriter pw = new PrintWriter(writer)) {
            li.printAtPreamble(pw);
        }
        assertTrue(sw.toString().contains(String.format("\\usepackage[fontsize=%d]{fontsize}[2021/08/04]\n", pixelSize)), sw::toString);
    }

    @ParameterizedTest
    @ValueSource(ints = {2,3,4,5,6,7,8,9,10,11,12,13,14,15,16})
    void withClo(int pixelSize) throws IOException {
        Properties p = new Properties();
        p.put("defaultFontSize", "" + pixelSize);
        p.put("defaultFontSizeClo", "myfont");
        Settings settings = new Settings(p, MockPath.createMockPathWithName("name"));
        MockShellProcessFactory latexProcessFactory = new MockShellProcessFactory(new StringBuilder(), new StringBuilder(), new ByteArrayOutputStream());
        LatexInteraction li = new LatexLength(1.23, emptyList(), Stream.empty(), settings, latexProcessFactory, new CapturingLogger());
        StringWriter sw = new StringWriter();

        try (BufferedWriter writer = new BufferedWriter(sw);
             PrintWriter pw = new PrintWriter(writer)) {
            li.printAtPreamble(pw);
        }
        assertTrue(sw.toString().contains(String.format("\\usepackage[cloname=myfont,fontsize=%d]{fontsize}[2021/08/04]\n", pixelSize)), sw::toString);
    }
}