package org.homelinux.rjlee.news.rendered;

import org.homelinux.rjlee.news.elements.*;
import org.homelinux.rjlee.news.input.Article;
import org.homelinux.rjlee.news.latex.LatexLength;
import org.homelinux.rjlee.news.latex.LatexProcessFactory;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * An implementation of {@code Page} that lays out pages in columns. This is the usual case.
 * See also {@link org.homelinux.rjlee.news.input.Truck}
 */
public class ColumnarPage implements Page {
    private final Settings settings;
    private long simplePageNo;
    private final List<Col> columns = new ArrayList<>();

    private double flowFromLength = -1;
    private double flowToLength = -1;
    /**
     * Number of extra columns added during layout, compared to the original estimate.
     */
    private int numExtraCols;

    public ColumnarPage(final long simplePageNo, final long numColumns, Settings settings) {
        this.settings = settings;
        if (numColumns <= 0) throw new IllegalArgumentException("A page must have columns!");
        this.simplePageNo = simplePageNo;
        Col last = null;
        for (long i = 0; i < numColumns; i++) {
            Col next = new Col(settings, last);
            columns.add(next);
            last = next;
        }
        //noinspection resource
        Logger.getInstance().elements().println("Page " + simplePageNo + " with " + numColumns + " columns");
    }

    public long numCols() {
        return columns.size();
    }

    private double calculateContinuationLength(String continuationText) {
        Logger logger = Logger.getInstance();
        LatexLength ll = new LatexLength(settings.getColumnWidth(), emptyList(), Stream.empty(), settings, new LatexProcessFactory(), logger);
        try (PrintWriter pw = ll.writer()) {
            pw.println(continuationText);
        } catch (IOException ex) {
            logger.algorithm().println("Failed to calculate length of fromFlowText; disabling 'from' continutaions.");
        }
        return ll.calculate();
    }

    private double getFlowFromLength() {
        if (flowFromLength < 0) {
            flowFromLength = calculateContinuationLength(settings.getContinuedFromPageText());
        }
        return flowFromLength;
    }

    private double getFlowToLength() {
        if (flowToLength < 0) {
            flowToLength = calculateContinuationLength(settings.getContinuedOnPageText());
        }
        return flowToLength;
    }

    /**
     * Write out the complete page to LaTeX.
     *
     * @param w       writer to write to
     * @param outPath
     */
    @Override
    public void write(PrintWriter w, Path outPath) throws IOException {
        Set<Part> fixedSet = new HashSet<>(); // which multi-column elements are already written out (identity set using default hash code)
        w.println("\\vfill\\centerline{"); // bounding box of page starts
        for (int c = 0; c < columns.size(); c++) {
            Col col = columns.get(c);
            w.printf("\\begin{minipage}[b][%fin][t]{%fin}%% column %d\n",
                    settings.getColumnHeight(), settings.getColumnWidth(), c + 1);
            long numFrags = col.getFrags().size();
            for (int f = 0; f < numFrags; ++f) {
                Col.ColFragment frag = col.getFrags().get(f);
                Part p = frag.getPart();
                if (p == null) {
                    w.printf("%%Empty fragment %s\n", frag);
                    continue;
                }
                String pathStr = Optional.ofNullable(p.path()).map(Path::toString).orElse("(no input)");
                w.printf("\\begin{minipage}[b][%fin][t]{%fin}%%Fragment %s\n", frag.height(), settings.getColumnWidth(), pathStr);
                if (frag.getPart() instanceof Valley) {
                    if (fixedSet.add(frag.getPart())) // only output on the first column
                        w.printf("\\valley[%fin]%%\n", frag.getPart().width());
                } else if (frag.getPart() instanceof FixedSize) {
                    w.printf("%% \\verb!%s!\n", frag.getPart().path());
                    FixedSize i = (FixedSize) frag.getPart();
                    if (fixedSet.add(i)) { // only output on the first column
                        // calculate the amount of space each side
                        double numCols = i.cols();
                        double fullWidth = numCols * settings.getColumnWidth() + ((numCols - 1) * settings.getAlleyWidth());
                        double spacer = (fullWidth - i.width()) / 2;
                        Logger.getInstance().dumpAll().printf("Padding = %d cols of %fin; insert width=%fin; spacer=%fin%n", i.cols(), settings.getColumnWidth(), i.width(), spacer);
                        if (spacer != 0) w.printf("\\hspace*{%fin}", spacer);
                        w.printf("\\hbox to %fin{%%\n", i.width());
                        w.printf("\\linewidth=%fin%%\n", i.width());
                        w.printf("\\columnwidth=%fin%%\n", i.width());
                        i.copyTo(w, outPath);
                        w.println("}% end hbox for fixed content");
                        if (spacer != 0) w.printf("\\hspace*{%fin}\n", spacer);
                    }
                } else if (frag.getPart() instanceof ArticleFragment) {
                    Optional.ofNullable(frag.getContinuedFrom()).ifPresent(page ->
                            w.printf("\\continuedFrom{%d}%n", page));
                    ArticleFragment a = ((ArticleFragment) frag.getPart());
                    Article article = a.getArticle();
                    w.printf("%% %s part %d of %d\n", frag.getPart().path(), article.getOutCtr() + 1, article.getFragments().size());
                    boolean lastFrag = article.getOutCtr() + 1 >= article.getFragments().size();
                    if (!lastFrag) {
                        // don't split the very  last fragment (regardless of page); there's a risk we could lose a line.
                        w.printf("\\splitbox{%s}{%fin}%%\n",
                                article.name(),
                                a.height());
                        w.printf("\\typeout{Column %d: To fill height %fin, " +
                                        "box height=\\the\\htsplitbox{%s}{%d} " +
                                        "remaining=\\the\\htsplitbox{%s}{%d}}\n",
                                c + 1, a.height(),
                                article.name(), a.getArticle().getOutCtr() + 1,
                                article.name(), a.getArticle().getOutCtr() + 2
                        );
                    } else {
                        w.printf("\\typeout{Column %d: To fill height %fin, " +
                                        "box height=\\the\\htsplitbox{%s}{%d} " +
                                        "at end of article}\n",
                                c + 1, a.height(),
                                article.name(), a.getArticle().getOutCtr() + 1
                        );
                    }
                    if (frag.isStretch()) {
                        w.printf("\\stretchsplitbox{%s}{%d}{%fin}%%\n",
                                article.name(),
                                article.countOutput(),
                                frag.height());
                    } else {
                        w.printf("\\usesplitbox{%s}{%d}%% target=%fin\n",
                                article.name(),
                                article.countOutput(),
                                frag.height());
                    }
                    Optional.ofNullable(a.getContinuedOnPage()).ifPresent(page ->
                            w.printf("\\continuedOn{%d}%n", page));
                }
                w.printf("\\end{minipage}\\par%% Fragment %s\n", pathStr);
                if (f < numFrags - 1)
                    w.println("\\nointerlineskip%");
            } // per fragment
            w.printf("\\end{minipage}%% column %d\n", c + 1); // h&vbox for column
            if (c < columns.size() - 1) {
                w.printf("\\hbox to %fin{\\vbox to %fin{%% halley\n", settings.getAlleyWidth(), settings.getColumnHeight());
                final Col right = columns.get(c + 1);
                final Iterator<Col.ColFragment> il = col.getFrags().iterator();
                final Iterator<Col.ColFragment> ir = right.getFrags().iterator();
                Col.ColFragment l = il.next();
                Col.ColFragment r = ir.next();
                for (double start = 0, end = Math.min(l.end(), r.end());
                        ;
                     l = (l.end() == end && il.hasNext()) ? il.next() : l,
                             r = (r.end() == end && ir.hasNext()) ? ir.next() : r,
                             start = end,
                             end = Math.min(l.end(), r.end())) {
                    double len = end - start;
                    if (l.getPart() == null || r.getPart() == null || l.getPart().skipHalley() || r.getPart().skipHalley())
                        w.printf("\\halleygap{%fin}%%\n", len);
                    else
                        w.printf("\\halleyline{%fin}%%\n", len);

                    if (!il.hasNext() && !ir.hasNext()) break;
                }
                w.println("}}% halley");
                //w.println("\\halley");
            }
        } // per column
        w.println("}\\vfill"); // bounding box of page
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("PAGE " + simplePageNo + "\n");
        for (int c = 0; c < columns.size(); c++)
            sb.append(String.format(" Column%d:%s\n", c + 1, columns.get(c)));
        return sb.toString();
    }

    /**
     * NB: This may not always be the same as the output page number in the PDF.
     *
     * @return an incrementing page counter, used internally for debugging.
     */
    @Override
    public long getSimplePageNo() {
        return simplePageNo;
    }

    public List<Col> getColumns() {
        return columns;
    }

    public Col addExtraColumn() {
        numExtraCols++;
        Col lastCol = getColumns().get(getColumns().size() - 1);
        Col nextCol = new Col(settings, lastCol);
        getColumns().add(nextCol);
        return nextCol;
    }

    /**
     * Set an article  in order. So find the first empty column and start from there.
     *
     * @param a                          to be set
     * @param alen                       length to be set (can be shorter than a.length())
     * @param continuedFrom              where an article is a continuation from a previous page, add "fromlength" and \\continuedFrom
     * @param allowPageEnlargementByCols how many extra columns we're allowed, compared to the original setup before laying out.
     * @return any remaining overflow to be set next time
     */
    public Overflow setArticleByFirstFit(Article a, double alen, Long continuedFrom, long allowPageEnlargementByCols) {
        allowPageEnlargementByCols -= numExtraCols; // don't count columns already added!

        ArticleFragment aa = null;
        Logger logger = Logger.getInstance();
        for (Col c : getColumns()) {
            Iterator<Col.ColFragment> spaces = c.empty().iterator();
            if (!spaces.hasNext()) continue; // this column is full!
            Col.ColFragment nextSpace = spaces.next();
            double space = nextSpace.height();
            boolean useAlley = nextSpace.start() > 0;
            if (useAlley) space -= settings.getAlleyHeight();
            if (space <= settings.getAlleyHeight()) continue;
            if (space > alen) {
                // the article fits in the space, so we're done.
                // ... or are we? We need to recalculate the remaining length, because TeX's splitting isn't exact
                alen = a.recalculateLength();
                logger.algorithm().println("Recalculated remaining length: " + alen + " with splits at " +
                        a.getFragments());
                if (alen == 0) return null; // success! We already laid it out.
            }
            if (space > alen) {
                // the article (remaining) fits into this space, so dump it & done:
                aa = a.splitArticle(alen);
                double top = nextSpace.start();
                if (useAlley) {
                    Col.ColFragment newAlleyFrag = c.new ColFragment(new Valley(settings, 1), top);
                    c.set(newAlleyFrag);
                    top += settings.getAlleyHeight();
                }
                Col.ColFragment newFrag = c.new ColFragment(aa, top, top + alen, continuedFrom);
                if (continuedFrom != null) aa.reduce(getFlowFromLength());
                continuedFrom = null; // only show "continued from" ont the first placement
                c.set(newFrag);
                return null;
            }
            // fit as much as we can in this space.
            alen -= space;
            double top = nextSpace.start();
            if (useAlley) {
                Col.ColFragment newAlleyFrag = c.new ColFragment(new Valley(settings, 1), top);
                c.set(newAlleyFrag);
                // it may be that, having set the alley, we've now filled the column. In this case, just skip to the next column.
                if (!c.empty().findFirst().isPresent()) {
                    if (settings.getAlleyThickHeight() == 0) {
                        // if there's no alley line, dumping an alley at the end of a column looks weird.
                        logger.algorithm().printf("WARN: page %d (simple count) column %d is a short column.%n", this.simplePageNo, getColumns().indexOf(c) + 1);
                    }
                    continue; // next column
                }
                top += settings.getAlleyHeight();
            }
            aa = a.splitArticle(space);
            Col.ColFragment newFrag = c.new ColFragment(aa, top, nextSpace.end(), continuedFrom);
            if (continuedFrom != null) aa.reduce(getFlowFromLength());
            continuedFrom = null; // only show "continued from" ont the first placement
            c.set(newFrag);
        }
        if (alen > 0.00001) {
            // we've reached the end of the page, but there's more article to set.
            // Perhaps we can enlarge the page to fit it?
            while (allowPageEnlargementByCols > 0 && alen > 0.00001) {
                allowPageEnlargementByCols--;
                Col extra = addExtraColumn();
                Col.ColFragment fragment = extra.longestEmpty();
                double colHeight = fragment.height();
                if (alen <= colHeight + 0.0001) {
                    aa = a.splitRemainingArticle(colHeight);
                    Col.ColFragment newFrag = extra.new ColFragment(aa, 0, aa.height(), null);
                    extra.set(newFrag);
                    alen = 0; // don't recalculate; we know the article will fit here.
                } else {
                    aa = a.splitArticle(colHeight);
                    Col.ColFragment newFrag = extra.new ColFragment(aa, 0, colHeight, null);
                    extra.set(newFrag);
                    alen = a.recalculateLength();
                }
            }
            // if setting into extra columns worked, there's nothing left to do:
            if (alen < 0.00001) return null;

            // we've no more space left on the page, but we have more article to set. That's an overflow.
            double flowToLength = getFlowToLength();
            if (aa != null) aa.reduce(flowToLength);
            logger.algorithm().println("Splitting article; " + alen + " column inches over page for " + a);
            return a.createOverflow(alen+ flowToLength, getSimplePageNo());
        }
        // we have somehow been through every column, set every column inch of the article, but without realising it.
        // In this case, there can't be an overflow, so return null is correct
        return null;
    }

    /**
     * A page is considered empty if it has no columns with content.
     *
     * @return true if either all columns contain a single fragment with no content, or if there are no columns. True if there is data.
     */
    @Override
    public boolean isEmpty() {
        return getColumns().stream().allMatch(c -> c.getFrags().size() == 1 && c.getFrags().get(0).getPart() == null);
    }

    @Override
    public void setSimplePageNo(long simplePageNo) {
        this.simplePageNo = simplePageNo;
    }
} // Page
