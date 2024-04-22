package org.homelinux.rjlee.news.logging;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MyPrintWriter extends PrintWriter {
    private final PrintStream printStream;

    public MyPrintWriter(PrintStream printStream) {
        super(printStream, true);
        this.printStream = printStream;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }
}
