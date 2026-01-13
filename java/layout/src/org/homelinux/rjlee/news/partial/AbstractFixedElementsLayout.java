package org.homelinux.rjlee.news.partial;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.rendered.ColumnarPage;
import org.homelinux.rjlee.news.settings.SemVer;
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
public abstract class AbstractFixedElementsLayout {
    protected final Settings settings;
    /**
     * First free column
     */
    protected final long minCol;
    final ColumnarPage p;
    /**
     * Bits filling this article
     */
    public List<LayoutSection> layoutSections = new ArrayList<>();
    /**
     * Cursor for stair-stepping articles
     */
    protected long col;
    /**
     * Number of columns
     */
    protected long numCols;
    /**
     * Amount of vertical space filled by this chain of bits
     */
    protected double vSize = 0;

    public AbstractFixedElementsLayout(Settings settings, long minCol, long numCols, ColumnarPage p) {
        this.settings = settings;
        this.minCol = minCol;
        this.col = numCols;
        this.numCols = numCols;
        this.p = p;
    }

    public String toString() {
        return String.format("PartialLayout:%fin:%s", vSize, layoutSections);
    }

    /**
     * Lay out a fixed-size element at the bottom of the vertical; caller checks sizing.
     *
     * @param i                          to lay out.
     * @param allowPageEnlargementByCols
     */
    protected void layoutBelow(FixedSize i, long allowPageEnlargementByCols) {
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
        // allow enlargement at this point if needed:
        long startCol = col - i.cols();
        if (startCol < 0 && numCols < (-startCol)) {
            throw new IllegalStateException("Can't set a fixed-size insert " + i + " into " + (numCols + allowPageEnlargementByCols) + " columns");
        }
        if (startCol < 0) {
            numCols -= startCol;
            while (startCol < 0) {
                startCol++;
                p.addExtraColumn();
            }
            startCol = 0;
        }
        layoutSections.add(new LayoutSection(i, startCol));
        vSize += i.height() + (wasEmpty ? 0 : settings.getAlleyHeight());
    }

    public abstract long fit(FixedSize i, long allowPageEnlargementByCols);

    protected void addExtraColumns(long numExtraCols) {
        for (long c = 0; c < numExtraCols; c++, numCols++) {
            // each col needs a reference to the previous col
            p.addExtraColumn();
        }
//                System.out.println(" to columns#" + numCols);
        // expand full-width elements into the next column:
        layoutSections.forEach(layoutSection -> layoutSection.inserts.forEach(in -> in.setNumColumnsOnPage(numCols)));
    }

    long fitByFirstFit(FixedSize i, double iHeight, long iCols, long allowPageEnlargementByCols) {
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
                vSize += section.merge(i, false, section.startCol - iCols);
                return 0;
            }
            // fit "bit" right of section, if we can
            if (numCols - section.endCol - 1 >= iCols) {
                algoLog.println(" - combining right of " + section);
                vSize += section.merge(i, true, section.startCol);
                return 0;
            }
            // slide "section" left to make space
            if (iCols + section.cols() <= numCols) {
                vSize += section.merge(i, false, 0);
                return 0;
            }

            // enlarge the page if it will fit "bit" right of section
            // (but skip if we're in 1.0.0-1.0.2, which did not do this)
            if (settings.getVersion().compareTo(SemVer.valueOf("0.0.3")) >= 0
                    && numCols - section.endCol + allowPageEnlargementByCols - 1 >= iCols) {
                long numExtraCols = iCols - (numCols - section.endCol - 1);
                addExtraColumns(numExtraCols);
                algoLog.printf(" - enlarging by %d to combine right of %s%n", iCols, section);
                vSize += section.merge(i, true, section.startCol);
                return iCols;
            }
        }
        return -1;
    }

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

        /**
         *
         * @param i to me merged with this section
         * @param append true to add to the right, false to the left
         * @param newStartCol new start column for merged block
         * @return any additional height used by this section to accommodate the {@code i}, inches
         */
        double merge(FixedSize i, boolean append, long newStartCol) {
            double oldLength = length;
            if (append) inserts.add(i);
            else inserts.add(0, i);
            startCol = newStartCol;
            endCol = newStartCol + inserts.stream().mapToLong(FixedSize::cols).sum() - 1;
            length = Math.max(length, i.height());
            if (startCol < 0)
                throw new IllegalStateException("Insert " + i + " attempt to set at col " + startCol + "; off the page!");
            if (endCol >= numCols)
                throw new IllegalStateException("Insert " + i + " from col " + startCol + " will overflow " + numCols + " columns by using column " + endCol);
            return length - oldLength;
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
}
