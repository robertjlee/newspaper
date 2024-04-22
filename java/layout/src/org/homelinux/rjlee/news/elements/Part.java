package org.homelinux.rjlee.news.elements;

import java.nio.file.Path;

/**
 * A part of a page to be laid out.
 */
public interface Part {
    double width();

    double height();

    Path path();

    /**
     * True for types that skip the horizontal-mode alley line, ie things that span columns
     */
    default boolean skipHalley() {
        return false;
    }

    static long widthToNumberOfColumns(double width, double alleyWidth, double columnWidth) {
        long wholeColumns = Math.max(0, (long) (width / (alleyWidth + columnWidth))-1);
        double remainder = width - wholeColumns * (alleyWidth + columnWidth);
        return wholeColumns +
                (remainder == 0 ? 0 :
                        remainder <= columnWidth ? 1 :
                                remainder <= (2 * columnWidth + alleyWidth) ? 2 : 3);
    }

}
