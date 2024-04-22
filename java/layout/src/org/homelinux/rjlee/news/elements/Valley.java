package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.settings.Settings;

import java.nio.file.Path;

/**
 * A vertical spacer (alley) between articles
 */
public class Valley implements Part {
    private final Settings settings;
    private long numCols;

    public Valley(Settings settings, long numCols) {
        this.settings = settings;
        this.numCols = numCols;
    }

    public double width() {
        return numCols * settings.getColumnWidth() + (numCols - 1) * settings.getAlleyWidth();
    }

    public double height() {
        return settings.getAlleyHeight();
    }

    public Path path() {
        return null;
    }

    @Override
    public boolean skipHalley() {
        return true;
    }

    public void addColumn() {
        numCols++;
    }

    @Override
    public String toString() {
        return "V-mode alley{cols=" + numCols + '}';
    }
}
