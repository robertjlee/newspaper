package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a unit test, so we don't actually test that LaTeX produces the right number, just that we output
 * the expected document and can read the number from it.
 */
class LatexLengthTest {
    private static final String EXPECTED_LATEX = "\\documentclass{article}\n" +
            "\\usepackage[british]{babel}\n" +
            "\\usepackage{indentfirst}\n" +
            "\\usepackage[british]{babel}\n" +
            "\\usepackage[utf8]{inputenc}\n" +
            "\\usepackage{newtxmath,newtxtext}\n" +
            "\\usepackage{csquotes}\n" +
            "\\usepackage[TU]{fontenc}\n" +
            "\\typeout{preamble}\n" +
            "\\usepackage{indentfirst}\n" +
            "\\makeatletter\n" +
            "\\renewcommand{\\rmdefault}{cmr}\n" +
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
            "\\newbox\\sb@junkbox\n" +
            "\\newbox\\sb@junkbox@\n" +
            "\\newcount\\nmulticoltolerance \\nmulticoltolerance=500\n" +
            "\\def\\setemergencystretch#1#2{\\emergencystretch=0.1\\hsize}\n" +
            "\\def\\numnewscols{15}\n" +
            "\\begin{document}\n" +
            "\\vbadness\\@Mi \\hbadness5000 \\tolerance\\nmulticoltolerance\n" +
            "\\hsize=34.560000in\\linewidth=34.560000in\\columnwidth=34.560000in\\textwidth=34.560000in\\vsize=\\maxdimen\\setlength{\\hfuzz}{\\maxdimen}\n" +
            "\\setlength{\\vfuzz}{\\maxdimen}\n" +
            "\\setbox\\sb@junkbox=\\vbox{\n" +
            "\\makeatother\n" +
            "\\setemergencystretch\\numnewscols\\hsize\n" +
            "My document here!\n" +
            "\\dumpfootnotes}\\makeatletter\n" +
            "\\eject\n" +
            "\\expandafter\\setbox\\sb@junkbox@=\\vbox{\\vsplit\\sb@junkbox to 3.000000in}%\n" +
            "\\expandafter\\setbox\\sb@junkbox@=\\vbox{\\vsplit\\sb@junkbox to 4.100000in}%\n" +
            "\\typeout{ART HEIGHT:\\the\\ht\\sb@junkbox}\n" +
            "\\typeout{ART DEPTH:\\the\\dp\\sb@junkbox}\n" +
            "\\end{document}";
    public static final double WIDTH = 34.56;
    private ByteArrayOutputStream sentToLaTeX;
    private StringBuilder receivedFromLaTeX;
    private Settings settings;
    private List<Double> fragments;
    private MockShellProcessFactory latexProcessFactory;
    private LatexLength latexLength;
    private CapturingLogger logger;

    @BeforeEach
    void setUp() {


        Properties properties = new Properties();
        properties.put("defaultFontFamily", "cmr");
        properties.put("tolerance", "500");
        settings = new Settings(properties);

        sentToLaTeX = new ByteArrayOutputStream();
        receivedFromLaTeX = new StringBuilder();
        latexProcessFactory = new MockShellProcessFactory(receivedFromLaTeX, new StringBuilder(), sentToLaTeX);
        fragments = Arrays.asList(3., 4.1);
        logger = new CapturingLogger();
        List<String> preambleLines = Arrays.asList("\\typeout{preamble}", "\\usepackage{indentfirst}");
        latexLength = new LatexLength(WIDTH, fragments, preambleLines.stream(), settings, latexProcessFactory, logger);
    }

    @Test
    void printHeadlineDef() throws IOException {
        try (StringWriter os = new StringWriter(); PrintWriter pw = new PrintWriter(new BufferedWriter(os))) {
            latexLength.printAtPreamble(pw);
            pw.flush();
            assertEquals("\\renewcommand{\\rmdefault}{cmr}\n" +
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
                            "\\newcommand{\\continuedFrom}[1]{\\vbox{\\def\\otherpage{#1}\\makebox[\\textwidth]{\\textit{\\scriptsize\\hspace{-1em}\\dots continued from page \\otherpage}\\hfill}}\\nointerlineskip}\n",
                    os.toString());
        }
    }

    @Test
    void ioErrorWithProcess() throws IOException {
        latexProcessFactory = new MockShellProcessFactory(new StringBuilder(), new StringBuilder(), sentToLaTeX)
                .withErrorOnStdOut();
        List<String> preambleLines = Arrays.asList("\\typeout{preamble}", "\\usepackage{indentfirst}");
        latexLength = new LatexLength(WIDTH, fragments, preambleLines.stream(), settings, latexProcessFactory, logger);
        try (PrintWriter pw = latexLength.writer()) {
            pw.println("My document here!");
        }
        RuntimeException ex = assertThrows(RuntimeException.class, latexLength::calculate);

        assertAll(
                () -> assertEquals("Failed to calculate article length: Process completed without producing length: 0", ex.getMessage()),
                () -> assertEquals("", sentToLaTeX.toString()), // TODO: can we write to the process rather than a temp file?
                () -> assertEquals(EXPECTED_LATEX + "\n  Process completed without length: 0",
                        // skip the first line; the tmpdir can change:
                        Arrays.stream(logger.dumpAllCollected().split("\n")).skip(1).collect(Collectors.joining("\n")))
        );
    }

    @Test
    void getSettings() {
        Assertions.assertSame(settings, latexLength.getSettings());
    }

    @Test
    void calculate() throws IOException {
        receivedFromLaTeX.append("ART HEIGHT: 14.56in\nART DEPTH: 0");
        try (PrintWriter pw = latexLength.writer()) {
            pw.println("My document here!");
        }
        double result = latexLength.calculate();
        assertAll(
                () -> assertEquals(14.56 + fragments.stream().mapToDouble(d -> d).sum(), result, 0.00001),
                () -> assertEquals("", sentToLaTeX.toString()) // TODO: can we write to the process rather than a temp file?
        );
    }

    @Test
    void returnedBadLength() throws IOException {
        receivedFromLaTeX.append("ART HEIGHT: invalid\nART DEPTH: bad\n");
        try (PrintWriter pw = latexLength.writer()) {
            pw.println("My document here!");
        }

        assertAll(
                () -> {
                    RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
                        latexLength.calculate();
                    });
                    assertEquals("Not implemented to read length [invalid]; try mm, inches or TeX points?", ex.getMessage());
                },
                () -> assertEquals("", sentToLaTeX.toString()));
    }


    @Test
    void processErrorOnLaunch() throws IOException {
        latexProcessFactory = new MockShellProcessFactory(receivedFromLaTeX, new StringBuilder(), sentToLaTeX, false, true);
        fragments = Arrays.asList(3., 4.1);
        logger = new CapturingLogger();
        List<String> preambleLines = Arrays.asList("\\typeout{preamble}", "\\usepackage{indentfirst}");
        latexLength = new LatexLength(WIDTH, fragments, preambleLines.stream(), settings, latexProcessFactory, logger);

        try (PrintWriter pw = latexLength.writer()) {
            pw.println("My document here!");
        }

        double result = latexLength.calculate();
        assertAll(
                () -> assertEquals(0.0, result, 0.00001),
                () -> assertEquals("", sentToLaTeX.toString()),
                () -> assertEquals("  Process not run!\n", logger.quietCollected()),
                // skip the first line; the tmpdir can change:
                () -> assertEquals(EXPECTED_LATEX, Arrays.stream(logger.dumpAllCollected().split("\n")).skip(1).collect(Collectors.joining("\n")))
        );
    }

    @Test
    void processFailed() throws IOException {
        receivedFromLaTeX.append("ART HEIGHT: 1in\nART DEPTH: 0");
        latexProcessFactory.setExitCode(-1);
        try (PrintWriter pw = latexLength.writer()) {
            pw.println("My document here!");
        }

        double calculated = latexLength.calculate();
        assertAll(
                () -> assertEquals(8.1, calculated, 0.00001),
                () -> assertEquals("", sentToLaTeX.toString()),
                () -> assertEquals("  Process completed: -1\n", logger.quietCollected()),
                // skip the first line; the tmpdir can change:
                () -> assertEquals(EXPECTED_LATEX + "\nART HEIGHT: 1in\nART DEPTH: 0\n  Calculated length:  1in=>8.1in",
                        Arrays.stream(logger.dumpAllCollected().split("\n")).skip(1).collect(Collectors.joining("\n")))
        );
    }

    @Test
    void processDied() throws IOException { // Not sure if this is a valid test case; it looks like the API can return null, but I'm not entirely sure.
        // best to test on the safe side!

        latexProcessFactory = new MockShellProcessFactory(receivedFromLaTeX, new StringBuilder(), sentToLaTeX, true, false);
        fragments = Arrays.asList(3., 4.1);
        logger = new CapturingLogger();
        List<String> preambleLines = Arrays.asList("\\typeout{preamble}", "\\usepackage{indentfirst}");
        latexLength = new LatexLength(WIDTH, fragments, preambleLines.stream(), settings, latexProcessFactory, logger);

        try (PrintWriter pw = latexLength.writer()) {
            pw.println("My document here!");
        }

        double result = latexLength.calculate();
        assertAll(
                () -> assertEquals(0.0, result, 0.00001),
                () -> assertEquals("", sentToLaTeX.toString()),
                () -> assertEquals("  Process not run!\n", logger.quietCollected()),
                // skip the first line; the tmpdir can change:
                () -> assertEquals(EXPECTED_LATEX, Arrays.stream(logger.dumpAllCollected().split("\n")).skip(1).collect(Collectors.joining("\n")))
        );
    }

    @Test
    void getTmpDirImpl() {
        Path systemTmpDir = MockPath.createMockPathWithName("/tmp", true);
        Path tmpDir = LatexLength.getTmpDirImpl(systemTmpDir, new CapturingLogger());
        // NB: This looks wrong, but it's to do with how the mock filesystem works.
        // Anything resolved from a temp directory returns the original path,
        // so we actually get the directory back again, this time as a file.
        // - which does mean that the random name is discarded, and we get a static name back, which is at least easier to test!
        assertEquals("/tmp", tmpDir.toString());
    }

    @Test
    void getTmpDirImpl_error() {
        Path systemTmpDir = MockPath.createMockPathForErrorOnCreateDir("/tmp");
        assertThrows(RuntimeException.class, () -> LatexLength.getTmpDirImpl(systemTmpDir, new CapturingLogger()));
    }
}