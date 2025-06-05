package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.input.Headers;
import org.homelinux.rjlee.news.layout.Magnet;
import org.homelinux.rjlee.news.parsing.LengthParser;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

/**
 * A fixed-size insert, full truck page, or Head-span article
 */
public interface FixedSize extends Part {
    long cols();

    default double margin() {
        double frameLineWidth = frameLineWidth();
        if (frameLineWidth > 0) return  (2 * frameLineWidth + 2 * frameLineSep());
        return 0;
    }

    default void copyTo(PrintWriter out, Path outPath) throws IOException {
        if (getSettings().isEnableLateXHooks()) getHeaders().ifHeader("BeforeBox", out::println);
        double boxWidth = frameLineWidth();
        if (boxWidth > 0) {
            double boxSep = frameLineSep();
            out.printf("{\\fboxrule=%fin\\fboxsep=%fin%n\\fbox{", boxWidth, boxSep);
        }
        copyToImpl(out, outPath);
        if (boxWidth > 0) {
            out.println("}}");
        }
        if (getSettings().isEnableLateXHooks()) getHeaders().ifHeader("AfterBox", out::println);
    }

    Settings getSettings();

    default double frameLineSep() {
        Headers headers = getHeaders();
        return headers.getLengthHeader("BoxSep", 3 * LengthParser.PT_PER_IN);
    }

    default double frameLineWidth() {
        Headers headers = getHeaders();
        return headers.getLengthHeader("BoxRule", 0.0);
    }

    void copyToImpl(PrintWriter out, Path outPath) throws IOException;


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

    default Magnet getMagnet() {
        return getHeaders().getMagnet();
    }
}
