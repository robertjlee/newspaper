package org.homelinux.rjlee.news.stream;

import java.io.IOException;
import java.io.Reader;

/**
 * Similar to PipedReader, but does not implement an upper bound for buffer size.
 *
 * @author Robert
 */
public class FakePipedReader extends Reader {

    private final StringBuffer buffer = new StringBuffer();
    private boolean closed;

    @Override
    public int read(char[] charBuffer, int offset, int length) throws IOException {
        synchronized (this.buffer) {
            int charsAvailable = buffer.length();
            int numCharsToRead = Math.min(length, charsAvailable);
            int numCharsToWrite = Math.min(length, charBuffer.length);
            int numChars = Math.min(numCharsToRead, numCharsToWrite);
            buffer.getChars(0, numChars, charBuffer, offset);
            buffer.delete(0, numChars);
            if (numChars == 0 && closed) return -1;
            return numChars;
        }
    }

    void write(char[] charBuffer, int offset, int length) throws IOException {
        synchronized (buffer) {
            buffer.append(charBuffer, offset, length);
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }


}
