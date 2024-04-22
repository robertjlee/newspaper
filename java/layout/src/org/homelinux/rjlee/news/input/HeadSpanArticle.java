package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.latex.LengthCalculator;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.stream.Stream;

public class HeadSpanArticle extends ArticleText implements FixedSize {
    private long cols;
    private double ruleWidth;
    private long numColumnsOnPage;

    public HeadSpanArticle(Headers headers, Settings settings, LengthCalculator lengthCalculator) {
        super(headers, settings, lengthCalculator);
        try {
            this.cols = headers.getIntegerHeader("Cols", 0L, Long.MAX_VALUE);
            if (this.cols <= 0) throw new RuntimeException("Number of cols must be positive!");
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("HeadSpan article: 'Cols' header must exist and must be a number of columns large enough to contain the article.");
        }
        try {
            this.ruleWidth = headers.getLengthHeader("RuleWidth", settings.getAlleyThickWidth());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("HeadSpan article: 'RuleWidth' header may be set to override the thickness of a rule within the article, but must be a valid length", e);
        }
        Logger.getInstance().elements().println(cols + "-column article " + headers.getInputFilePath() + " using rule width " + ruleWidth + "in");
    }

    @Override
    public boolean skipHalley() {
        return true;
    }

    @Override
    protected double widthForSizing() {
        long cols = cols();
        return getSettings().getColumnWidth() * cols + getSettings().getAlleyWidth() * (cols - 1);
    }

    @Override
    public double width() {
        return widthForSizing();
    }

    @Override
    public Stream<String> preambleLines() {
        return Stream.concat(super.preambleLines(),
                Stream.of("\\usepackage{multicol}\\multicoltolerance" + getSettings().getTolerance()
                        + "\\multicolpretolerance100"/*,
                        "\\def\\setemergencystretch#1#2{\\emergencystretch 0pt}"*/)); // RL: multicols defines by default "\emergencystretch 4pt\multiply\emergencystretch#1}"; #1 is number of cols, #2 is \hsize
    }

    @Override
    public void copyTo(PrintWriter out, Path outPath) throws IOException {
        out.println("\\vbox{");
        out.printf("\\begingroup\\setlength{\\columnsep}{%fin}\n", getSettings().getAlleyWidth());
        out.printf("\\setlength{\\columnseprule}{%fin}\n", ruleWidth);
        double widthForSizing = widthForSizing();
        out.printf("\\textwidth=%fin\\hsize=%fin" + // in case numCols=1, we need the text width & hsize too.
        "\\linewidth=%fin\n",
                widthForSizing, widthForSizing, widthForSizing);// Multicols uses \linewidth to determine the box width.
        getHeaders().ifHeader("Head", headline ->
                out.printf("\\parbox{%fin}{\\center\\headline{%s}}\\par\n", widthForSizing, headline));
        //out.println("}\\endgroup\\par");
//        out.println("\\par");
        // NB: we must call the cols() method to recalculate the number of columns, to support full-width in case the max-cols-per-page has changed.

        long innerCols = innerCols();
        if (innerCols > 1)
            out.printf("\\begin{multicols}{%d}[][0pt]\n", innerCols); // second arg is set to 0pt to kill the possibility of throwing \eject.
        out.println("\\setlength{\\parindent}{\\parindentcopy}"); // fix up the paragraph indent, which is broken by \minipage.
        copyToTex(getHeaders().getInputMode(), getSettings(), out, outPath);
        if (innerCols > 1)
            out.printf("\\end{multicols}");
        out.print("\\endgroup}");
        // { \b \be \e }
    }

    private long innerCols() {
        return getHeaders().getIntegerHeader("InnerCols", 0, Long.MAX_VALUE, cols());
    }

    /**
     * As this is set as a single unit, we simply include the full size of the element, including the spanning header.
     *
     * @return the full height of the article, including headers.
     */
    @Override
    public double columnInches() {
        return super.height() * cols;
    }

    @Override
    public double area() {
        return super.height() * widthForSizing();
    }

    @Override
    public long cols() {
        if (numColumnsOnPage > 0)// not set until we know the preferred sizing!
            return Math.min(Math.min(cols, numColumnsOnPage), getSettings().getMaxColsPerPage());
        return cols;
    }

    @Override
    public void setNumColumnsOnPage(long numColumnsOnPage) {
        boolean changed = numColumnsOnPage != this.numColumnsOnPage && numColumnsOnPage != cols;
        // TODO: support for page-spanning articles

        this.numColumnsOnPage = numColumnsOnPage;
        if (changed)
            recalculateLength();
    }
}
