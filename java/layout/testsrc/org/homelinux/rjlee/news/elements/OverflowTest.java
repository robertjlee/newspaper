package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.input.Article;
import org.homelinux.rjlee.news.input.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

class OverflowTest {

    private static final double LENGTH = 2.5;
    private static final long SPLIT_COUNT = 3;
    private Article article;
    private Overflow overflow;

    @BeforeEach
    void setUp() {
        // create an overflow for the third split of an article, with 2.5 column-inches still to typeset
        article = new MockArticle();
        overflow = new Overflow(article, LENGTH, SPLIT_COUNT);
    }

    @Test
    void getArticle() {
        Assertions.assertSame(article, overflow.getArticle());
    }

    @Test
    void getLength() {
        Assertions.assertEquals(LENGTH, overflow.getLength(), 0.0);
    }

    @Test
    void getSplitCounter() {
        Assertions.assertEquals(SPLIT_COUNT, overflow.getSplitCounter());
    }

    @Test
    void testToString() {
        Assertions.assertEquals("Overflowing: Last 2.500000 inches of MockArticle{}", overflow.toString());
    }

    private static class MockArticle implements Article {
        @Override
        public void registerFragment(double length) {

        }

        @Override
        public Path path() {
            return null;
        }

        @Override
        public double area() {
            return 0;
        }

        @Override
        public double columnInches() {
            return 0;
        }

        @Override
        public Headers getHeaders() {
            return null;
        }

        @Override
        public Stream<String> preambleLines() {
            return null;
        }

        @Override
        public double recalculateLength() {
            return 0;
        }

        @Override
        public List<Double> getFragments() {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public long getOutCtr() {
            return 0;
        }

        @Override
        public long countOutput() {
            return 0;
        }

        @Override
        public void copyTo(PrintWriter w, Path outPath) {

        }

        @Override
        public ArticleFragment splitRemainingArticle(double availableHeight) {
            return null;
        }

        @Override
        public ArticleFragment splitArticle(double columnInches) {
            return null;
        }

        @Override
        public Overflow createOverflow(double alen, long simplePageNo) {
            return null;
        }

        @Override
        public Long getContinuedOn(long splitCounter) {
            return null;
        }

        @Override
        public String toString() {
            return "MockArticle{}";
        }
    }
}