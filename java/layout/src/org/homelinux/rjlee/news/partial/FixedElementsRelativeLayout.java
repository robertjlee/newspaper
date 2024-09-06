package org.homelinux.rjlee.news.partial;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * For laying out inserts before articles
 *
 * @author Robert
 */
public class FixedElementsRelativeLayout {
    private final Settings settings;
    public class LayoutSection {
        private final List<FixedSize> inserts = new ArrayList<>();
        private double length;
        private long startCol;
        private long endCol;

        LayoutSection(FixedSize i, long startCol) {
            inserts.add(i);
            length = i.height();
            this.startCol = startCol;
            this.endCol = startCol + i.cols() - 1;
            if (startCol < 0)
                throw new IllegalStateException("Insert " + i + " attempt to set at col " + startCol + "; off the page!");
            if (endCol >= numCols)
                throw new IllegalStateException("Insert " + i + " from col " + startCol + " will overflow " + numCols + " columns by using column " + endCol);
        }

        void merge(FixedSize i, boolean append, long newStartCol) {
            if (append) inserts.add(i);
            else inserts.add(0, i);
            startCol = newStartCol;
            endCol = newStartCol + inserts.stream().mapToLong(FixedSize::cols).sum() - 1;
            length = Math.max(length, i.height());
            if (startCol < 0)
                throw new IllegalStateException("Insert " + i + " attempt to set at col " + startCol + "; off the page!");
            if (endCol >= numCols)
                throw new IllegalStateException("Insert " + i + " from col " + startCol + " will overflow " + numCols + " columns by using column " + endCol);
        }

        long cols() {
            return endCol - startCol + 1;
        }

        public String toString() {
            return String.format("VBit{%d-%d@%f:%s}", startCol, endCol, length, inserts);
        }

        public List<FixedSize> getInserts() {
            return inserts;
        }

        public double getLength() {
            return length;
        }

        public long getStartCol() {
            return startCol;
        }
    }

    /**
     * First free column
     */
    private final long minCol;
    /**
     * Cursor for stair-stepping articles
     */
    private long col;
    /**
     * Amount of vertical space filled by this chain of bits
     */
    private double vSize = 0;
    /**
     * Number of columns
     */
    private long numCols;
    /**
     * Bits filling this article
     */
    public List<LayoutSection> layoutSections = new ArrayList<>();
    final Page p;

    public String toString() {
        return String.format("PartialLayout:%fin:%s", vSize, layoutSections);
    }

    public FixedElementsRelativeLayout(long numCols, long minCol, Page p, Settings settings) {
        this.numCols = numCols;
        this.col = numCols;
        this.minCol = minCol;
        this.p = p;
        this.settings = settings;
    }

    private void layout(FixedSize i) { // add to bottom of vertical; caller checks sizing
        boolean wasEmpty = vSize == 0;
        Optional<Long> colHint = Optional.ofNullable(i.columnHint()).map(l -> l - 1);
        long col;
        if (!colHint.isPresent() || colHint.get() < 0 || colHint.get() + i.cols() > numCols) {
            // stair-step columns
            if (this.col - i.cols() <= minCol) this.col = numCols + 1;
            this.col--;
            col = this.col;
        } else {
            col = colHint.get() + i.cols();
        }
        layoutSections.add(new LayoutSection(i, col - i.cols()));
        vSize += i.height() + (wasEmpty ? 0 : settings.getAlleyHeight());
    }

    /**
     * Attempt to fit a fixed-size item onto the vertical.
     *
     * @param i to be fit
     * @return true if the item was fit, false otherwise. Note that not all possibilities that "can" fit are guaranteed to.
     */
    public boolean fit(FixedSize i, long allowPageEnlargementByCols) {
        PrintWriter algoLog = Logger.getInstance().algorithm();

        long width = i.cols();
//        System.out.printf("Width of %s (%s) is %d\n", i, i.getClass().getSimpleName(), width);
//        System.out.println("Insert or spanning article checking page width; Insert width=" + width + "; available columns " + minCol + "-" + numCols);
        if (width > numCols - minCol) {
            if (width > numCols - minCol + allowPageEnlargementByCols)
                throw new IllegalArgumentException("Insert or spanning article too wide for page! Insert width=" + width + "; available columns " + minCol + "-" + (numCols - 1));
            else {
                long numExtraCols = (width + minCol) - numCols;
                algoLog.println("Enlarged page from columns #" + numCols + "; added #" + numExtraCols + " columns");
                addExtraColumns(numExtraCols);
            }
        }
        // Enlarge the page.
        // NB: We should really roll back and re-render the whole page, because we may have already
        // set something at a narrower width.
        i.setNumColumnsOnPage(p.getColumns().size()); // as it may have changed, either for this article or a previous one.
        double length = i.height();
        algoLog.printf(" - layout of fixed size %d cols by %fin onto %d col page\n", width, length, numCols);
        algoLog.printf("   %fin %fin %fin\n", length, vSize, settings.getColumnHeight());
        if (length + vSize <= settings.getColumnHeight()) {
            layout(i);
            return true;
        }
        return fitByFirstFit(i, length, width);

    }

    private void addExtraColumns(long numExtraCols) {
        for (long c = 0; c < numExtraCols; c++, numCols++) {
            // each col needs a reference to the previous col
            p.addExtraColumn();
        }
//                System.out.println(" to columns#" + numCols);
        // expand full-width elements into the next column:
        layoutSections.forEach(layoutSection -> layoutSection.inserts.forEach(in -> in.setNumColumnsOnPage(numCols)));
    }

    boolean fitByFirstFit(FixedSize i, double iHeight, long iCols) {
        PrintWriter algoLog = Logger.getInstance().algorithm();

        // ignore stair-stepping, just try to fit the item.
        double freeSpace = settings.getColumnHeight() - vSize;
        for (LayoutSection section : layoutSections) {
            algoLog.println(" - combining with " + section + "?");

            if (!(section.length + freeSpace >= iHeight)) {
                continue;
            }


            // fit "bit" left of section, if we can
            if (section.startCol > iCols) {
                algoLog.println(" - combining left of " + section);
                section.merge(i, false, section.startCol - iCols);
                return true;
            }
            // fit "bit" right of section, if we can
            if (numCols - section.endCol - 1 >= iCols) {
                algoLog.println(" - combining right of " + section);
                section.merge(i, true, section.startCol);
                return true;
            }
            // slide "section" left to make space
            if (iCols + section.cols() <= numCols) {
                section.merge(i, false, 0);
                return true;
            }

        }
        return false;
    }
}
