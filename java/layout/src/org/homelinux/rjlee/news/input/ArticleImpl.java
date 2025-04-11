package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.elements.ArticleFragment;
import org.homelinux.rjlee.news.elements.ArticleFragmentImpl;
import org.homelinux.rjlee.news.elements.Overflow;
import org.homelinux.rjlee.news.latex.LengthCalculator;
import org.homelinux.rjlee.news.settings.Settings;

import java.util.HashMap;
import java.util.Map;

/**
 * A complete article, which may be broken up
 */
public class ArticleImpl extends ArticleText implements Article {

    private long splitCounter=0;
    private Map<Long, Long> continuedOn = new HashMap<>();

    public ArticleImpl(Headers headers, Settings settings, LengthCalculator lengthCalculator) {
        super(headers, settings, lengthCalculator);
    }

    @Override
    public void registerFragment(double length) {
        getFragments().add(length);
    }

    /**
     * To estimate the area, we need to actually typeset it in a document.
     */
    @Override
    public double area() {
        return getSettings().getColumnWidth() * height();
    }

    @Override
    public double columnInches() {
        return height();
    }

    @Override
    protected double widthForSizing() {
        return getSettings().getColumnWidth();
    }

    @Override
    public ArticleFragment splitRemainingArticle(double availableHeight) {
        double height = columnInches();
        return new ArticleFragmentImpl(this, this.splitCounter++, height, getSettings());
    }

    @Override
    public ArticleFragment splitArticle(double columnInches) {
        return new ArticleFragmentImpl(this, this.splitCounter++, columnInches, getSettings());
    }

    @Override
    public Overflow createOverflow(double alen, long simplePageNo) {
        // TODO: we assume that any article that overflows will continue on the next page. This is correct for now, but not very flexible.
        continuedOn.put(splitCounter-1, simplePageNo+1);
        return new Overflow(this, alen, splitCounter);
    }

    @Override
    public Long getContinuedOn(long splitCounter) {
        return continuedOn.get(splitCounter);
    }
}
