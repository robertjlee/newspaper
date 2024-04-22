package org.homelinux.rjlee.news.input;

import java.util.stream.Stream;

public interface PreambleLinesSupplier {
    Stream<String> preambleLines();
}
