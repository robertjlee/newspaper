package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.input.ArticleText;
import org.homelinux.rjlee.news.settings.Settings;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.stream.Stream;

public class FileCachingLengthCalculator implements LengthCalculator {

    private final LengthCalculator delegate;
    private final FileCache lengthCache = FileCache.getInstance();

    public FileCachingLengthCalculator(LengthCalculator delegate) {
        this.delegate = delegate;
    }

    @Override
    public double calculateLength(double widthForSizing, List<Double> fragments, Stream<String> preambleLines, Settings settings1, ArticleText articleText) {
        DoubleSupplier cacheFunction = () -> delegate.calculateLength(widthForSizing, fragments, preambleLines, settings1, articleText);
        return lengthCache.calculate(articleText.getPath(), fragments, cacheFunction);
    }
}
