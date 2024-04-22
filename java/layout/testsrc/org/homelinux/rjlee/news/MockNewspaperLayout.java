package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class MockNewspaperLayout implements NewspaperLayout {
    private Settings settings;
    private Path[] dirs;

    private List<String> methodCallOrder = new ArrayList<>();

    public MockNewspaperLayout() {
    }

    public void init(Settings settings, Path[] dirs) {
        methodCallOrder.add("init/constructor");
        this.settings = settings;
        this.dirs = dirs;
    }

    @Override
    public Stream<String> preambleLines() {
        methodCallOrder.add("preambleLines");
        return Stream.empty();
    }

    @Override
    public Settings getSettings() {
        methodCallOrder.add("getSettings");
        return settings;
    }

    @Override
    public List<Page> getPages() {
        methodCallOrder.add("getPages");
        return Collections.emptyList();
    }

    @Override
    public void layOutNewspaper() {
        methodCallOrder.add("layOutNewspaper");
    }

    @Override
    public void validate() {
        methodCallOrder.add("validate");
    }

    public List<String> getMethodCallOrder() {
        return methodCallOrder;
    }

}
