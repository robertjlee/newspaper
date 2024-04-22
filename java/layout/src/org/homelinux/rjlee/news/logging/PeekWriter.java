package org.homelinux.rjlee.news.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * A writer that can copy its output to an OutputStream, such as {@link System#out}, perhaps for debugging.
 *
 * @author Robert
 */
public class PeekWriter extends Writer {
    private Writer delegate;
    private final OutputStream peekStream;

    /**
     * The passed peekStream is not closed when this writer is closed; it is intended to be reused.
     *
     * @param delegate   the writer to copy things written to
     * @param peekStream the stream to output the debugging copy to
     */
    public PeekWriter(Writer delegate, OutputStream peekStream) {
        this.delegate = delegate;
        this.peekStream = peekStream;
    }

    public void write(char[] b, int o, int l) throws IOException {
        delegate.write(b, o, l);
        for (int i = o; i < l; ++i) peekStream.write(b[i]);
    }

    public void flush() throws IOException {
        delegate.flush();
        peekStream.flush();
    }

    public void close() throws IOException {
        delegate.close();
    }
}
