package org.homelinux.rjlee.news.partial;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.rendered.ColumnarPage;
import org.homelinux.rjlee.news.settings.Settings;

/**
 * An algorithm that attempts to fit each insert into the first group where it will fit horizontally,
 * then creating more groups vertically only as required.
 *
 * @author Robert
 */
public class LeftToRightFirstLayout extends AbstractFixedElementsLayout {
    public LeftToRightFirstLayout(long numCols, long minCol, ColumnarPage p, Settings settings) {
        super(settings, minCol, numCols, p);
    }

    @Override
    public long fit(FixedSize i, long allowPageEnlargementByCols) {
//        if (super.layoutSections.isEmpty()) {
        long rtn = fitByFirstFit(i, i.height(), i.cols(), allowPageEnlargementByCols);
        if (rtn < 0 && (i.height() < (settings.getColumnHeight() - vSize))) {
            layoutBelow(i);
            return 0;
        }
        return rtn;
//        }
//        for (LayoutSection layoutSection : layoutSections) {
//            long endCol = layoutSection.getStartCol() + layoutSection.getInserts().stream().mapToLong(FixedSize::cols).sum() - 1L;
//            if (i.cols() > )
//        }
//        return 0;
    }
}
