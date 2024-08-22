package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Input unit
 */
public interface Input extends PreambleLinesSupplier {
    Path path();

    /**
     * Estimate the area of the content, for initial page-breaking
     */
    double area();

    double columnInches();

    Headers getHeaders();

    /**
     * Called once the expected columns per page is known
     */
    default void setNumColumnsOnPage(long maxCols) {
    }

    Stream<String> preambleLines();

    default void copyToTex(Headers.InputMode inputMode, final Settings settings, final PrintWriter out, Path outPath) throws IOException {
        out.println("\\setemergencystretch\\numnewscols\\hsize");
        if (settings.isEnableLateXHooks()) getHeaders().ifHeader("BeforeContent", out::println);
        try (BufferedReader in = Files.newBufferedReader(path())) {
            // plain currently only supports the copy, not the \input, mode, because of the way comments are stripped.
            if (settings.isInputWithoutCopy() || inputMode != Headers.InputMode.LATEX) {
                switch (inputMode) {
                    case PLAIN:
                        out.print("\\inputPlainStripComments");
                        break;
                    case LATEX:
                        out.print("\\input");
                        break;
                    case MARKDOWN:
                        out.print("\\inputMdStripComments");
                        break;
                }
                Path relPath = /*outPath.toAbsolutePath().relativize(*/path()/*.toAbsolutePath())*/;
                String filename = relPath.getFileName().toString().replace('\\', '/');
                out.println("{" + filename + "}%");
            } else {
                in.lines().forEach(out::println);
            }

        }
        if (settings.isEnableLateXHooks()) getHeaders().ifHeader("AfterContent", out::println);
    }

    default void logInput(Logger logger) {
        if (this instanceof FixedSize) {
            FixedSize in = (FixedSize) this;
            logger.elements().printf("Input %s; %f column inches (%f x %f)\n", this, columnInches(), in.width(), in.height());
        } else {
            logger.elements().printf("Input %s; %f column inches\n", this, columnInches());
        }
    }
}
