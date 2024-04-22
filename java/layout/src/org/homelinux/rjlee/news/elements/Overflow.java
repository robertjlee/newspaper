package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.input.Article;

/**
 * Value object to store metadata about any article overflowing the page being set
 */
public class Overflow {
    /**
     * What is overflowing
     */
    private Article article;
    /**
     * Length remaining
     */
    private double length;
    /**
     * Which split of the article to use
     */
    private long splitCounter;

    public Overflow(Article article, double length, long splitCounter) {
        this.article = article;
        this.length = length;
        this.splitCounter = splitCounter;
    }

    /**
     * During layout, is there an article overflowing from the last page
     */
    public Article getArticle() {
        return article;
    }

    /**
     * How many column inches are required for the overflow article?
     */
    public double getLength() {
        return length;
    }

    /**
     * Value of split counter for overflow
     */
    public long getSplitCounter() {
        return splitCounter;
    }

    public String toString() {
        return String.format("Overflowing: Last %f inches of %s", length, article);
    }
}
