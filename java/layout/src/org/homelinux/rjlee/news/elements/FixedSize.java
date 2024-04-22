package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.input.Headers;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

/**
 * A fixed-size insert or multi-column article
 */
public interface FixedSize extends Part {
    long cols();

    void copyTo(PrintWriter out, Path outPath) throws IOException;

    void setNumColumnsOnPage(long colsPerPage);

    Headers getHeaders();
    /**
     * @return column hint from headers; null if not present
     */
    default Long columnHint() {
        Headers headers = getHeaders();
        long rtn = headers.getIntegerHeader("ColumnHint", 0, Long.MAX_VALUE, -1);
        return rtn >= 0 ? rtn : null;
    }
}
