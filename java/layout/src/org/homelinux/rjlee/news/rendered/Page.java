package org.homelinux.rjlee.news.rendered;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

public interface Page {
    void write(PrintWriter w, Path outPath) throws IOException;

    long getSimplePageNo();

    boolean isEmpty();

    void setSimplePageNo(long simplePageNo);
}
