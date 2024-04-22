package org.homelinux.rjlee.news.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

class LoggerImpl_UnconfiguredTest {

    private Logger instance = new LoggerImpl();

    @Test
    void quiet() {
        PrintWriter pw = instance.quiet();
        Assertions.assertTrue(pw instanceof MyPrintWriter && ((MyPrintWriter) pw).getPrintStream() == System.err);
    }

    @Test
    void elements() {
        PrintWriter pw = instance.elements();
        Assertions.assertTrue(pw instanceof MyPrintWriter && ((MyPrintWriter) pw).getPrintStream() == System.err);
    }

    @Test
    void algorithm() {
        PrintWriter pw = instance.algorithm();
        Assertions.assertTrue(pw instanceof MyPrintWriter && ((MyPrintWriter) pw).getPrintStream() == System.err);
    }

    @Test
    void dumpAll() {
        PrintWriter pw = instance.dumpAll();
        Assertions.assertTrue(pw instanceof MyPrintWriter && ((MyPrintWriter) pw).getPrintStream() == System.err);
    }

    @Test
    void texOutput() {
        PrintWriter pw  = instance.finalTexOutput();
        Assertions.assertTrue(pw instanceof MyPrintWriter && ((MyPrintWriter) pw).getPrintStream() == System.err);
    }
}