package org.homelinux.rjlee.news.logging;

import java.io.Writer;

public class NullWriter extends Writer {
    @Override
    public void write(char[] chars, int i, int i1) {
        // discarded
    }

    @Override
    public void flush() {
        // nothing to do

    }

    @Override
    public void close() {
        // nothing to do

    }
}
