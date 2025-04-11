package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.latex.LengthCalculator;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class InputFactory {

    private final Logger logger;
    private Settings settings;
    private LengthCalculator lengthCalculator;

    private boolean markdownUsed = false;

    public InputFactory(Settings settings, LengthCalculator lengthCalculator, Logger logger) {
        this.settings = settings;
        this.lengthCalculator = lengthCalculator;
        this.logger = logger;
    }

    public Input readInputFile(Path f, Settings settings, Logger logger) {
        logger.dumpAll().println("Considering <" + f + ">");
        try (BufferedReader r = Files.newBufferedReader(f)) {
            return newInput(f, settings, r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Input newInput(final Path p, Settings settings, final BufferedReader r) {
        try {
            Headers headers = readHeaders(p, settings, r);
            final String type = headers.getHeader("Type", "");
            markdownUsed |= headers.getInputMode() == Headers.InputMode.MARKDOWN;
            switch (type.toLowerCase().trim()) {
                case "fixed":
                    return new Insert(headers, this.settings);
                case "headspan":
                    return new HeadSpanArticle(headers, this.settings, lengthCalculator);
                case "title":
                    return new TitleInsert(headers, this.settings);
                case "truck":
                    return new Truck(headers, this.settings);
                case "article":
                    return new ArticleImpl(headers, this.settings, lengthCalculator);
                default:
                    logger.dumpAll().println(p + " has no Type header, so it's not considered to be an input.");
                    return null; // skip; this is perhaps input by another document.
            }
        } catch (RuntimeException | IOException e) {
            PrintWriter logger = this.logger.quiet();
            logger.println("Error reading " + p + ":");
            e.printStackTrace(logger);
            return null;
        }
    }

    protected Headers readHeaders(Path p, Settings settings, BufferedReader r) throws IOException {
        return new Headers(p, r, settings);
    }

    public boolean isMarkdownUsed() {
        return markdownUsed;
    }
} // InputFactory
