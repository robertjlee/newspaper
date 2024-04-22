package org.homelinux.rjlee.news.logging;

import java.io.IOException;
import java.io.Writer;

/**
 * As {@link PeekWriter}, but writes to two Writers.
 *
 * @author Robert
 */
public class TeeWriter extends Writer {
    private final Writer bw;
    private final Writer logger;

    public TeeWriter(Writer bw, Writer logger) {
        this.bw = bw;
        this.logger = logger;
    }

    @Override
    public void write(char[] chars, int i, int i1) throws IOException {
        bw.write(chars, i, i1);
        logger.write(chars, i, i1);
    }

    @Override
    public void flush() throws IOException {
        try {
            bw.flush();
        } finally {
            logger.flush();
        }
    }

    @Override
    public void close() throws IOException {
        bw.close();
        // don't close the logger
    }
}
