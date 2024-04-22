package org.homelinux.rjlee.news.logging;

import org.homelinux.rjlee.news.settings.Settings;

import java.io.PrintStream;
import java.io.PrintWriter;

public interface Logger {

    void configure(Settings settings, PrintStream systemOut, PrintStream systemErr);

    /**
     * @return writer for error messages that should show even when running quiet
     */
    PrintWriter quiet();

    /**
     * @return writer for element summary messages
     */
    PrintWriter elements();

    /**
     * @return writer for tracing the algorithm
     */
    PrintWriter algorithm();

    /**
     * @return writer for full programme tracing
     */
    PrintWriter dumpAll();

    /**
     * While the output TeX file isn't a log, and doesn't obey logging levels, its
     * use-case is basically identical, so we might as well supply it here.
     *
     * @return an output stream to the final TeX file.
     */
    PrintWriter finalTexOutput();

    void close();

    static Logger getInstance() {
        return LoggerImpl.getInstance();
    }
}
