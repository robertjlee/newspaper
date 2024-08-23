package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.input.ArticleText;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Calculate the length of something by shelling out to LaTeX.
 * Reusable.
 *
 * @author Robert
 */
public class LaTeXLengthCalculator implements LengthCalculator {
    @Override
    public double calculateLength(double widthForSizing, List<Double> fragments, Stream<String> preambleLines, Settings settings, ArticleText articleText) {
        LatexLength ll = new LatexLength(widthForSizing, fragments, preambleLines, settings, new LatexProcessFactory(), Logger.getInstance());
        return calculateLength(articleText, ll);
    }

    double calculateLength(ArticleText articleText, LatexLength ll) {
        Path tmpDir = ll.outPath();
        Logger logger = Logger.getInstance();
        logger.algorithm().println("Shelling to LaTeX to calculate length of article " + articleText.name());
        try (PrintWriter out = ll.writer()) {
            articleText.copyTo(out, tmpDir);
        } catch (IOException e) {
            PrintWriter quiet = logger.quiet();
            quiet.println("I/O Error calculating length of LaTeX article:");
            e.printStackTrace(quiet);
        }
        articleText.getHeaders().assets().forEach(asset -> {
            try {
                logger.dumpAll().println("Copying asset " + asset);
                Files.copy(asset, tmpDir.resolve(asset.getFileName()));
            } catch (IOException e) {
                PrintWriter warningLogger = logger.elements();
                warningLogger.printf("Skipping asset %s; failed to copy to %s; %s%n", asset, tmpDir, e.getMessage());
                e.printStackTrace(warningLogger);
            }
        });
        return ll.calculate();
    }
}
