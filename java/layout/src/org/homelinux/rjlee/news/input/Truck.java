package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A full-truck insert. Generally, this is a full-size spread.
 * (A "truck" is the device that holds the inked hot metal to the paper to be printed).
 */
public class Truck implements Input, FixedSize, Page {
    private final Headers headers;
    private final Settings settings;
    private long simplePageNo;

    public Truck(Headers headers, Settings settings) {
        this.headers = headers;
        this.settings = settings;
    }

    public boolean isRotate() {
        return Boolean.parseBoolean(headers.getHeader("Rotate", ""));
    }

    @Override
    public double width() {
        long cols = cols();
        return (cols * settings.getColumnWidth()) + ((cols - 1) * settings.getAlleyWidth());
    }

    @Override
    public double height() {
        return getHeight();
    }

    @Override
    public Path path() {
        return headers.getInputFilePath();
    }

    @Override
    public double area() {
//        return width() * getHeight();
        return 0; // don't count the area of truck pages; this is used for the initial page count but these are always additional pages anyway.
    }

    @Override
    public double columnInches() {
        return 0; // don't count the area of truck pages; this is used for the initial page count but these are always additional pages anyway.
//        return cols() * getHeight();
    }

    private double getHeight() {
        return settings.getPageHeight();
    }

    public long cols() {
        return settings.getMaxColsPerPage();
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public void setNumColumnsOnPage(long maxCols) {
        // not sure why I need this; maybe an IDE bug?
        Input.super.setNumColumnsOnPage(maxCols);
    }

    @Override
    public void copyToImpl(PrintWriter out, Path outPath) throws IOException {
        if (isRotate())
            out.println("\\afterpage{\\begin{landscape}");
        copyToTex(headers.getInputMode(), settings, out, outPath);
        if (isRotate())
            out.println("\\end{landscape}}");
    }

    @Override
    public Stream<String> preambleLines() {
        if (isRotate())
            return Stream.concat(headers.preambleLines(),
                    Stream.of("\\usepackage{pdflscape}", "\\usepackage{afterpage}"));
        else
            return headers.preambleLines();
    }

    @Override
    public void write(PrintWriter w, Path outPath) throws IOException {
        copyToImpl(w, outPath);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getSimplePageNo() {
        return simplePageNo;
    }

    @Override
    public void setSimplePageNo(long simplePageNo) {
        this.simplePageNo = simplePageNo;
    }

    public String toString() {
        return String.format("[Truck:%s([%d]%fx%f)]", headers.getInputFilePath().getFileName(), cols(), width(), getHeight());
    }
}
