package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.input.PreambleLinesSupplier;
import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;

import java.util.List;
import java.util.stream.Stream;

public interface NewspaperLayout extends PreambleLinesSupplier, LaidOut {
    @Override
    Stream<String> preambleLines();

    Settings getSettings();

    @Override
    List<Page> getPages();

    void layOutNewspaper();

    void validate();

}
