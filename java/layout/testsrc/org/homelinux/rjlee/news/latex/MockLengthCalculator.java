package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.input.ArticleText;
import org.homelinux.rjlee.news.settings.Settings;

import java.util.List;
import java.util.stream.Stream;

public class MockLengthCalculator implements LengthCalculator {

    private double length = 3.1415;

    public void setLength(double length) {
        this.length = length;
    }

    @Override
    public double calculateLength(double widthForSizing, List<Double> fragments, Stream<String> preambleLines, Settings settings1, ArticleText articleText) {
        return length;
    }
}
