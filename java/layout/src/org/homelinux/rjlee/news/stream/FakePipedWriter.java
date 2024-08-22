package org.homelinux.rjlee.news.stream;

import java.io.IOException;
import java.io.Writer;

/**
 * Similar to PipedWriter, but does not implement an upper bound for buffer size.
 *
 * @author Robert
 */
public class FakePipedWriter extends Writer {
    private boolean open = true;
    private final FakePipedReader reader;

    public FakePipedWriter(FakePipedReader reader) {
        this.reader = reader;
    }

    @Override
    public void write(char[] chars, int i, int i1) throws IOException {
        if (!open) throw new IOException("Write to closed stream!");

        reader.write(chars, i, i1);
    }

    @Override
    public void flush() throws IOException {
        // nothing to do; wait for the data to be read.
    }

    @Override
    public void close() throws IOException {
        flush();
        open = false;
        reader.close();
    }
}
