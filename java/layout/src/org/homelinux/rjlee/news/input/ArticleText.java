package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.latex.LengthCalculator;
import org.homelinux.rjlee.news.settings.FontCommand;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An article consisting of an unknown length of text
 */
public abstract class ArticleText implements Input {
    private Headers headers;
    private final Settings settings;
    private double length = -1;
    private long outCtr;

    public Path path() {
        return headers.getInputFilePath();
    }

    private List<Double> fragments = new ArrayList<>(); // only set for splittable articles

    private LengthCalculator lengthCalculator;

    ArticleText(Headers headers, Settings settings, LengthCalculator lengthCalculator) {
        this.headers = headers;
        this.settings = settings;
        this.lengthCalculator = lengthCalculator;
    }

    /**
     * Before setting the final fragment of an article, we need to recalculate its length.
     * <p>
     * That's because splitting an article may change the number of column inches, as articles can't be split
     * exactly: lines must be kept together, and the interline spacing is discarded on a split. We can even have
     * vertical objects, like images or equations, that appear where we want to split, that end up in the next box.
     * <p>
     * Generally, TeX will try to remove as much content as it sensibly can up to the requested split size, then
     * stretch that content to fit the box (e.g. with spacing between paragraphs).
     *
     * @return remaining length in article not yet typeset.
     */
    public double recalculateLength() {
        Stream<String> preambleLines = preambleLines();
//        if (headers.getInputMode() == Headers.InputMode.MARKDOWN) // RL: Always load markdown headers regardless. A LaTeX article may \markdownInput a .md.
        preambleLines = Stream.concat(preambleLines, Stream.of(settings.getMarkdown()));
        length = lengthCalculator.calculateLength(widthForSizing(), fragments, preambleLines, settings, this);
        if (length == 0)
            throw new IllegalArgumentException(path() + " has 0 column inches!");
        return length - fragments.stream().mapToDouble(d -> d).sum();
    }

    protected abstract double widthForSizing();

    public void copyTo(PrintWriter out, Path outPath) throws IOException {
        headers.ifHeader("Head", headline -> {
            out.print("\\headline{");
            if (headers.isAnyHeaderSet("HeadFamily", "HeadSeries", "HeadShape", "HeadSize", "HeadSpacing", "HeadCommand")) {
                FontCommand def = settings.getHeaderFont();
                out.print(headers.getFontCommandFromHeaders("Head", def.getFamily(), def.getSeries(), def.getSize()));
                out.println("{}"); // ensure the last output command-name is treated as is.
            }
            out.print(headline);
            out.println("}\n\\par");
        });
        copyToTex(headers.getInputMode(), settings, out, outPath);
    }

    public String toString() {
        return headers.getInputFilePath().getFileName().toString();
    }

    public Stream<String> preambleLines() {
        return headers.preambleLines();
    }

    public double height() {
        if (length < 0)
            recalculateLength();
        return length;
    }

    // A unique (hopefully!) TeX identifier for the article.
    public String name() {
        try {
            return nameByEncoding("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex); // unexpected!
        }
    }

    // This method only really exists to get test coverage; all JVM implementations
    // support UTF-8 encoding.
    String nameByEncoding(String encoding) throws UnsupportedEncodingException {
        return java.net.URLEncoder.encode(path().toString(), encoding)
                .replace("+", "_")
                .replace("%", "");
    }

    public Headers getHeaders() {
        return headers;
    }

    public Settings getSettings() {
        return settings;
    }

    public Path getPath() {
        return headers.getInputFilePath();
    }

    public List<Double> getFragments() {
        return fragments;
    }

    public long countOutput() {
        return ++outCtr;
    }

    /**
     * Incrementing counter for the output parts
     */
    public long getOutCtr() {
        return outCtr;
    }

}
