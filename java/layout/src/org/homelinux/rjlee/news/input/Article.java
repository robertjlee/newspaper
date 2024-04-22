package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.elements.ArticleFragment;
import org.homelinux.rjlee.news.elements.Overflow;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

public interface Article extends Input {
    void registerFragment(double length);

    double area();

    @Override
    double columnInches();

    double recalculateLength();

    List<Double> getFragments();

    String name();

    long getOutCtr();

    long countOutput();

    void copyTo(PrintWriter w, Path outPath) throws IOException;

    /**
     * Create a single ArticleFragment containing the entire (remaining) article.
     * <p>
     * If the article fits within about 90-100% of the free space, it will be stretched to the entire column.
     * Otherwise, free space will remain.
     *
     * @return a new article fragment, containing the entire contents of the article.
     * @param availableHeight how much space (in inches of height) is available in the column
     */
    ArticleFragment splitRemainingArticle(double availableHeight);

    /**
     * Create a single ArticleFragment containing a portion of the article.
     * When an article is split across columns, this is generally the length of the split.
     *
     * @param columnInches length of article to split
     * @return a new article fragment, containing {@code columnInches} of the article length.
     */
    ArticleFragment splitArticle(double columnInches);

    /**
     * Construct an overflow, to record how much of an article has <em>not</em> been set on the page, due to length.
     *
     * @param alen         length of the article to be set on another page
     * @param simplePageNo
     * @return Overflow object
     */
    Overflow createOverflow(double alen, long simplePageNo);

    Long getContinuedOn(long splitCounter);
}
