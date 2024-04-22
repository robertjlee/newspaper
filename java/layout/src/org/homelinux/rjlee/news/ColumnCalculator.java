package org.homelinux.rjlee.news;

/**
 * Calculates the likely number of columns and pages, given the total column inches and page metrics
 *
 * @author robert
 */
class ColumnCalculator {
    private final long totalColumns;
    private final long numPages;
    private final int colsPerPage;

    private ColumnCalculator(long totalColumns, long numPages, int colsPerPage) {
        this.totalColumns = totalColumns;
        this.numPages = numPages;
        this.colsPerPage = colsPerPage;
    }

    /**
     * @param columnInches   total length (or equivalent page-area, for column-spanning objects)
     * @param columnHeight   printable height of a column
     * @param maxColsPerPage maximum number of columns that can be accommodated on a page
     * @return calculation results
     */
    static ColumnCalculator calculateColumnsPerPage(double columnInches, double columnHeight, long maxColsPerPage) {
        long totalColumns = Math.round((columnInches / columnHeight) + 0.5); // round up // C
        long numPages = Math.round(((double) totalColumns / maxColsPerPage) + 0.5); // round up // P
        int cpp = Math.round((float) totalColumns / numPages);
        return new ColumnCalculator(totalColumns, numPages, cpp);
    }


    /**
     * @return total number of columns across all pages
     */
    long getTotalColumns() {
        return totalColumns;
    }

    /**
     * @return number of pages
     */
    long getNumPages() {
        return numPages;
    }

    /**
     * @return Columns per page, on all but the last page
     */
    int getColsPerPage() {
        return colsPerPage;
    }
}
