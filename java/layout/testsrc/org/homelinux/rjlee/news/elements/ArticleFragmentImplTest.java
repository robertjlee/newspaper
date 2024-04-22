package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.input.Article;
import org.homelinux.rjlee.news.input.Headers;
import org.homelinux.rjlee.news.input.InputFactory;
import org.homelinux.rjlee.news.latex.MockLengthCalculator;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Properties;

class ArticleFragmentImplTest {

    private ArticleFragment articleFragment;
    private Settings settings;
    private Path path;
    private Article article;

    @BeforeEach
    void setUp() throws IOException {
        settings = new Settings(new Properties());
        path = MockPath.createMockPathWithName("art4.tex");
        Headers headers = new Headers(path, new Properties(), settings);
        try (BufferedReader br = new BufferedReader(new StringReader("%#Type: article"))) {
            article = (Article) new InputFactory(settings, new MockLengthCalculator(), Logger.getInstance())
                    .newInput(path, settings, br);
        }
        articleFragment = new ArticleFragmentImpl(article, 2, 4.2, settings);
    }

    @Test
    void skipHalley() {
        Assertions.assertFalse(articleFragment.skipHalley());
    }

    @Test
    void width() {
        Assertions.assertEquals(settings.getColumnWidth(), articleFragment.width(), 0.0);
    }

    @Test
    void height() {
        Assertions.assertEquals(4.2, articleFragment.height(), 0.0);
    }

    @Test
    void path() {
        Assertions.assertSame(path, articleFragment.path());
    }

    @Test
    void getArticle() {
        Assertions.assertSame(article, articleFragment.getArticle());
    }

    @Test
    void testToString() {
        Assertions.assertEquals("[art4.tex:2 => 4.200000 in]", articleFragment.toString());
    }
}