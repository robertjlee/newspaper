package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.rendered.Page;

import java.util.List;
import java.util.stream.Stream;

public interface LaidOut {
    Stream<String> preambleLines();

    List<Page> getPages();
}
