package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.input.Article;
import org.homelinux.rjlee.news.settings.Settings;

import java.nio.file.Path;
import java.util.List;

/**
 * A single fragment of a paste-up article
 */
public class ArticleFragmentImpl implements ArticleFragment {
    private Article article;
    /**
     * Incrementing counter for the splits within article
     */
    private long fragment; // > 0
    private double height;

    private final Settings settings;
    public ArticleFragmentImpl(Article a, long fragment, double height, Settings settings) {
        this.article = a;
        this.fragment = fragment;
        this.height = height;
        this.settings = settings;
        a.registerFragment(height);
    }

    @Override
    public double width() {
        return settings.getColumnWidth();
    }

    /**
     * Height of this fragment; how much of the article is split off here.
     */
    @Override
    public double height() {
        return this.height;
    }

    @Override
    public Path path() {
        return article.path();
    }

    /**
     * Article we're a portion of
     */
    @Override
    public Article getArticle() {
        return article;
    }

    @Override
    public Long getContinuedOnPage() {
        return article.getContinuedOn(fragment);
    }

    @Override
    public void reduce(double flowToLength) {
        height = Math.max(0, height - flowToLength);
        List<Double> allFragments = article.getFragments();
        if (!allFragments.isEmpty()) {
            int lastIdx = allFragments.size() - 1;
            allFragments.set(lastIdx, Math.max(0, allFragments.get(lastIdx) - flowToLength));
        }
    }

    public String toString() {
        return String.format("[%s:%d => %f in]", article, fragment, height);
    }
}
