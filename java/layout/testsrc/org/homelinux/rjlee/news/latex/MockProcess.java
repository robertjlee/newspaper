package org.homelinux.rjlee.news.latex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class MockProcess extends Process {

    private InputStream stdOut, stdErr;
    private OutputStream stdIn;
    private int exitCode;

    public MockProcess(String stdOut, String stdErr, OutputStream stdIn, int exitCode, boolean errorOnStdOut, boolean errorOnStdErr, boolean errorOnStdIn) {
        this.stdOut = errorOnStdOut ? new ErroringInputStream() : new BufferedInputStream(new ByteArrayInputStream(stdOut.getBytes(StandardCharsets.UTF_8)));
        this.stdErr = errorOnStdErr ? new ErroringInputStream() : new BufferedInputStream(new ByteArrayInputStream(stdErr.getBytes(StandardCharsets.UTF_8)));
        this.stdIn = errorOnStdIn ? new ErroringOutputStream() : new BufferedOutputStream(stdIn);
        this.exitCode = exitCode;
    }

    @Override
    public OutputStream getOutputStream() {
        return stdIn;
    }

    @Override
    public InputStream getInputStream() {
        return stdOut;
    }

    @Override
    public InputStream getErrorStream() {
        return stdErr;
    }

    @Override
    public int waitFor() {
        flush();
        return exitCode;
    }

    @Override
    public boolean waitFor(long l, TimeUnit timeUnit) throws InterruptedException {
        long durationNanos = timeUnit.toNanos(l);
        long end = System.nanoTime() + durationNanos;
        while (System.nanoTime() < end) {
            flush();
            if (isClosed(stdOut) && isClosed(stdErr)) return true;
            //noinspection BusyWait
            Thread.sleep(100);
        }
        return false;
    }

    private boolean isClosed(InputStream stream) {
        try {
            if (stream.available() == 0)
                return true;
        } catch (IOException e) {
            if (e.getMessage().contains("tream closed")) return true;
        }
        return false;
    }

    @Override
    public int exitValue() {
        flush();
        return exitCode;
    }

    @Override
    public void destroy() {
        flush();
    }

    private void flush() {
        try {
            stdIn.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ErroringInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            throw new IOException("Error!");

        }
    }

    private static class ErroringOutputStream extends OutputStream {
        @Override
        public void write(int i) throws IOException {
            throw new IOException("Error!");
        }
    }
}
