package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.settings.Settings;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class for things that interact with LaTeX
 *
 * @author Robert
 */
public abstract class LatexInteraction {
    private final Settings settings;

    public LatexInteraction(Settings settings) {
        this.settings = settings;
    }

    /**
     * Define the low-level LaTeX headers in the output file.
     * <p>
     * This defines the input search directories, the default typeface, and the font-size commands (which aren't provided my minimal).
     * <p>
     * Precondition: the output file should be in {@code \makeatletter} mode.
     *
     * @param out
     */
    // \huge is defined as    \@setfontsize\huge\@xxpt{25}}
    protected void printAtPreamble(PrintWriter out) {
        if (!settings.isDefaultFontFamilyFromHeaders()) {
            out.printf("\\renewcommand{\\rmdefault}{%s}\n", settings.getDefaultFontFamily());

            // Switch typewriter default font to Latin Modern Roman Light Condensed.
            // This doesn't look particularly beautiful, but it's one of the few typewriter fonts suitable for narrow columns.
            out.printf("\\renewcommand{\\ttdefault}{%s}%n", settings.getDefaultTeletypeFamily());
            out.printf("\\DeclareFontSeriesDefault[rm]{md}{%s}%n", settings.getDefaultFontSeries());
            out.printf("\\DeclareFontSeriesDefault[tt]{md}{%s}%n", settings.getDefaultTeletypeSeries());

            // consider the following for maths:
//                "\\SetMathAlphabet{\\mathtt}{normal}{OT1}{lmtt}{m}{n}\n" +
//                "\\SetMathAlphabet{\\mathtt}{bold}{OT1}{lmtt}{m}{n}\n");
        }


        Path[] srcDirs = settings.getSrcDirs();
        if (srcDirs.length > 0) {
            // One can also use the $TEXINPUTS env var, but setting the search parameters in the path gives a more easily
            // usable TeX file:
            String inputPaths = Arrays.stream(srcDirs)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .map(s -> s.replace('\\', '/')) // I expect "/" to work on all systems
                    .map(s -> s.endsWith("/") ? s : s + "/") // TeX search paths for directories must include a trailing slash
                    .map(s -> "{" + s + "}")
                    .collect(Collectors.joining("", "\\def\\input@path{", "}"));
            out.println(inputPaths);
        }

        out.println("\\newlength{\\parindentcopy}\\setlength{\\parindentcopy}{\\parindent}");

        // The environ package, used to create environments, and in the definition of newsplitbox:
        out.println("\\usepackage{environ}[2014/05/04]");

        // Redefine \@footnotetext and \@mp@footnotetext to simply collect footnotes to the end of each article,
        // and define the command \dumpfootnotes to dump them in-place.
        // This means each footnote is a part of the article's length as far as the Java programme is concerned,
        // and we don't have to worry about allocating space for inserts.
        out.println("\\newbox\\footnotebox\n" +
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
                "\\let\\mp@footnotetext\\news@footnotetext");

        // Our box-handling macros (only used directly in final output, but included elsewhere so users can use these macros if they want to)
        // I had a whole splitbox.dtx worked out, but it's overkill.
        out.println("\\global\\newbox\\sb@tmp@box%\n" +
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
                "\\newcommand{\\stretchsplitbox}[3]{%\n" + // Sometimesusing \\vbox to X{\\unvboxY} leaves the box LONGER than X, even if \\copyY (dp+ht) is shorter than X. I don't yet understand why, except that unvbox unfreezes the glue while copy does not.
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
                "}");

        // Definitions for handling inputs of plain-text, while stripping comments:

        out.print("\\newread\\input@read\n" + // used to read from the input file
                "\\newwrite\\input@write\n" + // Temporary file used for the actual input file, with comments stripped.
                // (it would be nice to not have to use a pipe; we can easily collect the catcode-12 tokens, but for some reason they don't display properly unless we go via \input.

                // Sorry to any LaTeX macro lovers, but defining a macro that expands to the CATCODE OTHER "%" is easier in plain TeX:
                "{\n" +
                "  \\catcode`\\%=12\\relax\n" +
                "  \\xdef\\commentchar{%}\n" +
                "}\n" +


                //  Test if #2 starts with char #1; if true, do #3, else do #4
                //  This one's a neat solution picked up from StackExchange (https://tex.stackexchange.com/questions/132248/test-if-the-first-character-of-a-string-is-a)
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
                // guard: call input@processfile@ but only if the file is readable:
                "\\newcommand{\\input@processfile}[3]{%\n" +
                "  \\openin\\input@read #1\\relax%\n" +
                "  \\ifeof\\input@read%\n" +
                "    \\closein\\input@read%\n" +
                "  \\else%\n" +
                "    \\closein\\input@read%\n" +
                "    \\input@processfile@{#1}{#2}{#3}%\n" +
                "  \\fi%\n" +
                "}\n" +
                //  Partial input loosely from https://tex.stackexchange.com/questions/4889/input-only-part-of-a-file
                "\\newcommand{\\input@processfile@}[3]{%\n" +
                "  \\openin\\input@read #1\\relax%\n" +
                "  \\begingroup%\n" +
                "  \\endlinechar\\newlinechar%\n" +
 //               "  \\def\\input@lines{}%\n" +
                // \immediate - because we want to write out now, not on page-out:
                "  \\immediate\\openout\\input@write=\\jobname-pipe.tmp%\n" +
                "  \\@whilesw\\unless\\ifeof\\input@read\\fi{%%\n" +
                //    % e-TeX's \\readline makes everything catcode 12 (except spaces).
                "    \\endlinechar=-1%\n" + // don't add a newline back onto each input line (results in extra space at the end)
                "    \\readline\\input@read to\\input@line%\n" +
                "    \\ifeof\\input@read\\else%\n" + // avoid \n at end of file
                "      \\eifstartswith{#3}{\\input@line}{}{%\n" +
 //               "        \\edef\\input@lines{\\input@lines\\input@line}%%\n" +
                "      \\immediate\\write\\input@write{\\input@line}%\n" +
                "      }%\n" +
                "    \\fi%\n" +
                "  }%\n" +
                "  \\closein\\input@read%\n" +
                //  Originally used a single \\write, because \\write adds \\newlinechar, but each line already has a \\newlinechar in it - but now removed in input by setting endlinechar.
                // This produces an extra blank line at the end of the file, but that'll be converted to removable space anyway:
                "  \\immediate\\closeout\\input@write%\n" +
                "  \\expandafter\\endgroup%\n" +
                "  \\begingroup#2\\endgroup%\n" +
                "}\n" +
                "\\ExplSyntaxOn\n" +
                // Searches \input@path for the file. Kudos to Witiko of the Markdown package for the suggestion
                // https://github.com/Witiko/markdown/issues/429#issuecomment-2028894964
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
                "}\n");

        // patch the section commands, so that they still work in markdown input, but produce no numbers (even if the
        // non-star form is used), and with reduced vertical spacing:
        out.println(
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
                        "}"
        );


        // fontsize takes second argument for baselineskip
        // normalsize definition from size10.clo
        Optional<String> fontClo = Optional.ofNullable(settings.getDefaultFontSizeClo()).map(String::trim).filter(s -> !s.isEmpty());
        if (fontClo.isPresent()) {
            out.println("\\input{size10.clo}");
            out.printf("\\usepackage[cloname=%s,fontsize=%d]{fontsize}[2021/08/04]%n", fontClo.get(), settings.getDefaultFontSize());
        } else {
            // RL: tried inputting the .clo file directly here, for 10-12pt, but this produced spacing errors.
            out.printf("\\usepackage[fontsize=%d]{fontsize}[2021/08/04]%n", settings.getDefaultFontSize());
        }

        // TODO: make spaces configurable!
        out.println("\\newlength\\headlineskip\\headlineskip=2pt plus 2pt\\relax");
        // RL: the \vbox here ensures that we don't break the headline, and also forces us into vertical mode.
        out.println("\\newcommand{\\headline}[1]{\\vbox{\\topsep=0pt\\parsep=0pt\\begin{center}\\fontseries{bx}\\fontsize{18}{20}\\selectfont #1\\vspace{\\headlineskip}\\end{center}}}");

        out.printf("\\newcommand{\\continuedOn}[1]{\\nointerlineskip\\vfill\\def\\otherpage{#1}%s}\n", settings.getContinuedOnPageText());
        out.printf("\\newcommand{\\continuedFrom}[1]{\\vbox{\\def\\otherpage{#1}%s}\\nointerlineskip}\n", settings.getContinuedFromPageText());
    }

    public static void extract(String line, String prefix, StringBuilder result) {
        if (line.startsWith(prefix + ":"))
            result.append(line.substring(prefix.length() + 1));
    }

    protected Settings getSettings() {
        return settings;
    }
}
