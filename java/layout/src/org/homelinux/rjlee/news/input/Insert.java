package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.elements.Part;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A fixed-size insert, such as image, including caption.
 */
public class Insert implements Part, Input, FixedSize {
    private final Headers headers;
    private final Settings settings;
    private final double widthHeader; // either a fixed width, or pageWidth (for "fill")
    private final double height;
    private String cap; // some sort of caption, for later use.
    private long numColumnsOnPage;

    public Insert(Headers headers, Settings settings) {
        this.headers = headers;
        this.settings = settings;
        double maxWidth = settings.getPageWidth();
        this.widthHeader = Math.min(headers.getLengthHeader("Width", maxWidth), maxWidth);
        Logger.getInstance().elements().println(" + simple width() of " + headers.getInputFilePath() + "=" + widthHeader + "in");
        this.height = headers.getLengthHeader("Height", settings.getColumnHeight());
        this.cap = headers.getHeader("Cap", "");
    }

    @Override
    public boolean skipHalley() {
        return true;
    }

    public double height() {
        return this.height;
    }

    public double width() {
        if (widthHeader == settings.getPageWidth() && numColumnsOnPage > 0) {
            return (numColumnsOnPage * settings.getColumnWidth()) + ((numColumnsOnPage - 1) * settings.getAlleyWidth());
        }
        return widthHeader;
    }

    public Path path() {
        return headers.getInputFilePath();
    }

    public double area() {
        return width() * this.height;
    }

    public double columnInches() {
        return height() * cols();
    }

    @Override
    public Headers getHeaders() {
        return headers;
    }

    public long cols() {
        return Part.widthToNumberOfColumns(width(), settings.getAlleyWidth(), settings.getColumnWidth());
    }


    public void setNumColumnsOnPage(long maxCols) {
        this.numColumnsOnPage = maxCols;
    }

    public void copyToImpl(PrintWriter out, Path outPath) throws IOException {
        copyToTex(headers.getInputMode(), settings, out, outPath);
    }

    public String toString() {
        return String.format("[Insert:%s([%d]%fx%f)]", headers.getInputFilePath().getFileName(), cols(), width(), height);
    }

    @Override
    public Stream<String> preambleLines() {
        return headers.preambleLines();
    }

}
