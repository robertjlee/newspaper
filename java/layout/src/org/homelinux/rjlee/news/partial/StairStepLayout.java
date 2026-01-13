package org.homelinux.rjlee.news.partial;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.rendered.ColumnarPage;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.PrintWriter;

/**
 * A layout method that attempts to place each insert to the right of, and below, the previous insert, but
 * groups inserts left-to-right where needed to fit more onto the page.
 *
 * @author Robert
 */
public class StairStepLayout extends AbstractFixedElementsLayout {

    public StairStepLayout(long numCols, long minCol, ColumnarPage p, Settings settings) {
        super(settings, minCol, numCols, p);
    }

    /**
     * Attempt to fit a fixed-size item onto the vertical.
     *
     * @param i to be fit
     * @return if the item was fit returns a whole number containing the extra columns added; negative otherwise. Note that not all possibilities that "can" fit are guaranteed to.
     */
    @Override
    public long fit(FixedSize i, long allowPageEnlargementByCols) {
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
                allowPageEnlargementByCols -= numExtraCols;
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
            layoutBelow(i, allowPageEnlargementByCols);
            return 0;
        }
        return fitByFirstFit(i, length, width, allowPageEnlargementByCols);

    }

}
