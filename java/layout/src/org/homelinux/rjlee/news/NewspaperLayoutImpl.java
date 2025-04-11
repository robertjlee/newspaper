package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.elements.*;
import org.homelinux.rjlee.news.input.*;
import org.homelinux.rjlee.news.latex.FileCachingLengthCalculator;
import org.homelinux.rjlee.news.latex.LaTeXLengthCalculator;
import org.homelinux.rjlee.news.latex.LengthCalculator;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.partial.FixedElementsRelativeLayout;
import org.homelinux.rjlee.news.rendered.Col;
import org.homelinux.rjlee.news.rendered.ColumnarPage;
import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A passable Newspaper layout algorithm in Java.
 * <p>
 * All measurements stored as "double" are in inches throughout.
 *
 * @author Robert Lee
 */
public class NewspaperLayoutImpl implements NewspaperLayout {
    /**
     * The number of columns on a typical page
     */
    private final int colsPerPage;
    private final Logger logger;
    /**
     * The settings as determined by settings.properties files.
     */
    private final Settings settings;
    /**
     * Holds any partial article held over from the last page
     */
    private Overflow overflow = null;
    /**
     * All inputs (for sanity checking)
     */
    private final List<Input> allInputs = new ArrayList<>();
    /**
     * Input documents still to be processed
     */
    private final List<Input> inputs = new ArrayList<>();
    /**
     * Combined preamble
     */
    private final List<String> allPreambleLines = new ArrayList<>();
    /**
     * Output pages
     */
    private final List<Page> pages = new ArrayList<>();


    public String toString() {
        return pages.toString();
    }


    /**
     * Read in what to lay out, from the filesystem.
     */
    public NewspaperLayoutImpl(final Settings settings, Logger logger, Path[] dirs) {
        this.logger = logger;
        this.settings = settings;
        readInputs(dirs);
        // calculate the number of columns per page
        double columnInches = inputs.stream().mapToDouble(Input::columnInches).sum();
        ColumnCalculator columnPageResult = ColumnCalculator.calculateColumnsPerPage(settings, columnInches, settings.getColumnHeight(), settings.getMaxColsPerPage());
        colsPerPage = columnPageResult.getColsPerPage(); // c

        PrintWriter algorithm = logger.algorithm();
        algorithm.printf("Page size (%s x %s)\n", settings.getPageWidth(), settings.getPageHeight());
        algorithm.printf("Column size (%s x %s)\n", settings.getColumnWidth(), settings.getColumnHeight());
        algorithm.printf("%s column inches over %d columns @ %d cols/page => %d pages mostly %d cols\n",
                columnInches, columnPageResult.getTotalColumns(), settings.getMaxColsPerPage(), columnPageResult.getNumPages(), colsPerPage);

        algorithm.printf("Estimate %f column-inches before current pages are full",
                (columnPageResult.getNumPages() * settings.getMaxColsPerPage() * settings.getColumnHeight()) - columnInches);

        inputs.forEach(i -> i.setNumColumnsOnPage(colsPerPage));

        int pageNo;
        for (pageNo = 1; pageNo < columnPageResult.getNumPages(); pageNo++) {
            Page p = new ColumnarPage(pageNo, colsPerPage, settings);
            pages.add(p);
        }
        Page lastPage = new ColumnarPage(pageNo, columnPageResult.getTotalColumns() - ((long) colsPerPage * pages.size()), settings);
        pages.add(lastPage);
    }

    private void readInputs(final Path[] dirs) {
        LengthCalculator lengthCalculator = new FileCachingLengthCalculator(new LaTeXLengthCalculator());
        Logger logger = Logger.getInstance();
        InputFactory inf = new InputFactory(settings, lengthCalculator, logger);
        List<Input> inputs = Arrays.stream(dirs)
                .peek(dir -> logger.dumpAll().println("Directory <" + dir + ">"))
                .flatMap(path -> {
                    try {
                        return Files.list(path);
                    } catch (IOException | NullPointerException e) { // NPE can probably only happen in tests
                        e.printStackTrace(System.err);
                        logger.algorithm().println("Skipping " + path);
                        return Stream.empty();
                    }
                })
                .filter(this::isInputFile)
                .sorted()
                .map(f -> inf.readInputFile(f, settings, logger))
                .filter(Objects::nonNull) // some input files don't declare a Type header, and are assumed to be imported by other inputs, so just skip them.
                .collect(Collectors.toList());
        processInputs(inputs, logger, inf.isMarkdownUsed());
    }

    /**
     * Does the given filename match the configured input filters?
     *
     * @param d path to potential input file
     * @return true if we should check this file for preamble lines; false if it is definitely not an input we're interested in.
     */
    boolean isInputFile(Path d) {
        String filename = d.getFileName().toString();
        return Arrays.stream(settings.getInputFilters()).anyMatch(filename::endsWith);
    }

    void processInputs(List<Input> inputs, Logger logger, boolean isMarkdownUsed) {
        this.inputs.addAll(inputs);
        this.allInputs.addAll(inputs);
        if (isMarkdownUsed) allPreambleLines.add(settings.getMarkdown());

        // RL: possibly inefficient, but it tidies the debugging if we precalculate the lengths
        this.inputs.parallelStream().forEach(Input::columnInches);
        this.inputs.forEach(input -> input.logInput(logger));

        this.inputs.stream()
                .flatMap(Input::preambleLines)
                .forEach(allPreambleLines::add);
    }

    boolean hasData() {
        return !inputs.isEmpty() || overflow != null;
    }

    /**
     * Lay out the page.
     *
     * @param p                          page to lay out
     * @param allowPageEnlargementByCols on the last page, there may be fewer columns, but there may be overflow.
     *                                   This parameter says how many columns we can add to the page to accommodate items larger than expected.
     */
    void layoutPage(ColumnarPage p, long allowPageEnlargementByCols) {
        logger.algorithm().println("Laying out page [" + p.getSimplePageNo() + "]; overflow=" + overflow);
        logger.algorithm().println("Inputs remaining: #" + inputs.size());
        final List<Article> ip = new ArrayList<>();
        final List<FixedSize> is = new ArrayList<>();
        // calculate usable page area
        double pageArea = settings.getPageWidth() * settings.getColumnHeight();
        // move inputs from this.inputs to ip until page area is exceeded
        double overflowLength = overflow == null ? 0.0 : overflow.getLength();
        int pos = 0;
        for (double areaMoved = overflowLength; areaMoved < pageArea && inputs.size() > pos; ) {
            Input next = inputs.remove(pos);
            Headers headers = next.getHeaders();
            boolean[] skipInput = {false};
            if (next instanceof Truck) skipInput[0] = true;
            else headers.ifHeader("Page", str -> {
                long minPage = headers.getIntegerHeader("Page", Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (minPage > 0 && minPage > p.getSimplePageNo()) skipInput[0] = true;
                if (minPage < 0 && minPage > (p.getSimplePageNo() - pages.size() - 1)) skipInput[0] = true;
            });
            if (skipInput[0]) {
                inputs.add(pos++, next);
                continue;
            }
            double size = next.area();
            areaMoved += size;
            logger.algorithm().println(" - Expecting on this page: " + next + " of type " + next.getClass().getSimpleName());
            if (next instanceof ArticleImpl) ip.add((Article) next);
            else is.add((FixedSize) next);
        }

        if (!p.isEmpty()) {
            // issue 19: only do FixedElementsRelativeLayout if the page is empty. FERL doesn't support partial pages.
            Collections.reverse(is);
            is.forEach(unprocessed -> inputs.add(0, (Input) unprocessed));
        } else {
            // now lay out the fixed inserts
            FixedElementsRelativeLayout v = new FixedElementsRelativeLayout(p.numCols(), 0, p, settings);
            while (!is.isEmpty()) {
                FixedSize fs = is.get(0);
                logger.algorithm().println("Fitting fixed-size " + fs);
                long colsAdded;
                if ((colsAdded = v.fit(fs, allowPageEnlargementByCols)) >= 0) {
                    is.remove(0);
                    allowPageEnlargementByCols -= colsAdded;
                } else {
                    if (fs.height() > settings.getColumnHeight()) {
                        throw new IllegalArgumentException(String.format("Input " + fs.path().getFileName() + " would be longer than available page height. Increase page size, change to type article, or reduce size of insert."));
                    }
                    logger.algorithm().printf("Failed to fit insert %s; deferring %d fixed articles to next page\n", fs, is.size());
                    Collections.reverse(is);
                    is.forEach(unprocessed -> inputs.add(0, (Input) unprocessed));
                    break;
                }
            }
            logger.algorithm().println("Placing " + v);
            place(p, v);
            logger.algorithm().println(" - placing fixed elements done");
        }
        // basic layout: worst-fit without splitting, falling back to inserting from the first column
        // if we have an overflow, set it first, by first-fit.
        // that way, overflows always start in column 1.
        if (overflow != null) {
            logger.algorithm().println("Setting overflow: " + overflow);
            Overflow overflown = overflow;
            long continuedFrom = p.getSimplePageNo()-1;
            overflow = p.setArticleByFirstFit(overflow.getArticle(), overflow.getLength(), continuedFrom, allowPageEnlargementByCols);
            if (overflow == overflown || overflow == null) {// don't clear a *new* overflow!
                logger.algorithm().println(" - overflow set");
                setOverflow(null);
            } else {
                logger.algorithm().println(" - overflow set, but created a new overflow");
            }
        }
        while (!ip.isEmpty()) {
            Col.ColFragment longestEmpty = null;
            Col longestEmptyCol = null;
            for (Col c : p.getColumns()) {
                Col.ColFragment f = c.longestEmpty();
                if (f == null) continue;
                if (longestEmpty == null || longestEmpty.height() > f.height()) {
                    longestEmpty = f;
                    longestEmptyCol = c;
                }
            }
            if (longestEmpty == null) {
                logger.algorithm().println("Exactly filled page!");
                while (!ip.isEmpty()) {
                    logger.algorithm().println(" - Deferring to next page: " + ip);
                    inputs.add(0, ip.remove(ip.size() - 1));
                }
                break;
            }

            Article a = ip.remove(0);
            logger.algorithm().println("Setting fragment-able article " + a);
            double alen = a.columnInches();

            double reqAlleyHeight = longestEmpty.start() == 0 ? 0 : settings.getAlleyHeight();
            if (longestEmpty.height() >= alen + reqAlleyHeight) {
                System.out.println("longestEmpty.height() = " + longestEmpty.height());
                System.out.println("alen = " + alen);
                System.out.println("settings.getAlleyHeight() = " + settings.getAlleyHeight());
                // the entire article will fit here exactly, so put it here!
                ArticleFragment aa = a.splitRemainingArticle(longestEmpty.height() - reqAlleyHeight);
                double begin = longestEmpty.start();
                if (begin > 0) {
                    Col.ColFragment newAlley = longestEmptyCol.new ColFragment(new Valley(settings, 1), begin);
                    longestEmptyCol.set(newAlley);
                    begin += settings.getAlleyHeight();
                }
                Col.ColFragment newFrag = longestEmptyCol.new ColFragment(aa, begin, begin + aa.height(), null);
                longestEmptyCol.set(newFrag);
            } else {
                // need to split the article. Frankly, in this case it's probably best
                // to just place it in order for now.
                // (There are more clever things we can do later, such as arranging columns side-by-side)
                overflow = p.setArticleByFirstFit(a, alen, null, allowPageEnlargementByCols);
            }
            if (overflow != null) {
                // the page is filled. There may be more spaces, but we can't fit an article in them.
                ip.forEach(unprocessed -> inputs.add(0, unprocessed));
                ip.clear();
            }
        }

        logger.algorithm().println("Laying out page [" + p.getSimplePageNo() + "] done; overflow=" + overflow);
        logger.algorithm().println(p);
    }

    /**
     * Place inserts onto the page
     */
    private void place(ColumnarPage p, FixedElementsRelativeLayout v) {
        double cursor = 0;
        PrintWriter algorithm = Logger.getInstance().algorithm();
        algorithm.println("Moving fixed elements onto page " + p.getSimplePageNo());
        for (FixedElementsRelativeLayout.LayoutSection b : v.layoutSections) {
            algorithm.println(": Bit " + b);
            int c = (int) b.getStartCol();
            for (FixedSize i : b.getInserts()) {
                if (i.height() == 0) {
                    System.out.println("0-height insert " + i + "; will now crash.");
                }
                double end = cursor + i.height();
                for (int col = c; col < c + i.cols(); ++col) {
                    //System.out.println(": : Insert " + i + " on col " + col);
                    Col cc = p.getColumns().get(col);
                    cc.set(cc.new ColFragment(i, cursor, end, null));
                }
                c += (int) i.cols();
            }
            cursor += b.getLength();
        }
    }


    /**
     * We can end up allocating a column for overflow, without needing it. So trim any empty
     * pages, so we don't output a blank. But only remove pages from the end, so that page numbers for "Page" headers match.
     */
    void trimEmptyPages() {
        pages.removeIf(Page::isEmpty);
        /*
        for (int i = pages.size() - 1; i >= 0; i--) {
            Page page = pages.get(i);
            if (page.isEmpty()) {
                pages.remove(i);
            } else {
                break; // we expect empty pages to be at the end.
            }
        }
         */
    }

    /**
     * A final sanity check to make sure that all inputs actually appear in the output.
     */
    public void validate() {
        Set<Path> pathsRendered =
                Stream.concat(pages.stream()
                                        .filter(p -> p instanceof ColumnarPage)
                                        .map(ColumnarPage.class::cast)
                                        .flatMap(p -> p.getColumns().stream())
                                        .flatMap(c -> c.getFrags().stream())
                                        .map(Col.ColFragment::getPart)
                                        .filter(Objects::nonNull)
                                        .map(Part::path),
                                pages.stream()
                                        .filter(p -> p instanceof Truck)
                                        .map(Truck.class::cast)
                                        .map(Truck::path)
                                        .filter(Objects::nonNull)
                        )
                        .collect(Collectors.toSet());

        String missing = allInputs.stream()
                .filter(i -> !pathsRendered.contains(i.path()))
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        if (!missing.isEmpty())
            throw new RuntimeException("Not rendered: " + missing);
    }


    @Override
    public Stream<String> preambleLines() {
        return allPreambleLines.stream();
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public List<Page> getPages() {
        return pages;
    }

    /**
     * Entry point to lay out the newspaper.
     */
    @Override
    public void layOutNewspaper() {
        while (hasData()) {
            Page p = null;
            for (int simplePageNo = 0; simplePageNo < getPages().size();) {
                // NB: the last page may well only contain overflow,
                // but we still need to set it out.
                p = getPages().get(simplePageNo);
                p.setSimplePageNo(++simplePageNo); // track the page numbers with extra page inserts
                if (p instanceof ColumnarPage) {
                    ColumnarPage cp = ((ColumnarPage) p);
                    long numExtraColsAllowed = settings.getMaxColsPerPage() - cp.getColumns().size();
                    layoutPage(cp, numExtraColsAllowed);
                }
                // have we accumulated any full-trucks to output?
                while (!inputs.isEmpty() && inputs.get(0) instanceof Truck) {
                    Truck truck = (Truck) inputs.remove(0);
                    getPages().add(simplePageNo++, truck);
                    truck.setSimplePageNo(simplePageNo);
                }
            }
            // if we still have data, e.g. by using "Page" to create blank space, then we need to add extra pages:
            if (hasData()) {
                Objects.requireNonNull(p);
                getPages().add(new ColumnarPage(p.getSimplePageNo() + 1, colsPerPage, settings));
            }
        }
        trimEmptyPages();


        Logger logger = Logger.getInstance();
        logger.algorithm().println("Layout done");
        logger.dumpAll().println("Layout is:");
        logger.dumpAll().println(this);

    }

    void setOverflow(Overflow overflow) {
        this.overflow = overflow;
    }

    Overflow getOverflow() {
        return overflow;
    }
}
