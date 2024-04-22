package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.input.Article;

import java.nio.file.Path;

public interface ArticleFragment extends Part {
    double width();

    double height();

    Path path();

    Article getArticle();

    Long getContinuedOnPage();

    void reduce(double flowToLength);
}
