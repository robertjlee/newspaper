package org.homelinux.rjlee.news.logging;

import org.homelinux.rjlee.news.settings.Settings;

import java.io.*;

/**
 * An implementation of Logger that simply captures output text
 */
public class CapturingLogger implements Logger {

    private StringWriter quiet = new StringWriter();
    private StringWriter elements = new StringWriter();
    private StringWriter algorithm = new StringWriter();
    private StringWriter dumpAll = new StringWriter();
    private StringWriter texOutput = new StringWriter();

    private PrintWriter quietWriter = new PrintWriter(new BufferedWriter(quiet));
    private PrintWriter elementsWriter = new PrintWriter(new BufferedWriter(elements));
    private PrintWriter algorithmWriter = new PrintWriter(new BufferedWriter(algorithm));
    private PrintWriter dumpAllWriter = new PrintWriter(new BufferedWriter(dumpAll));
    private PrintWriter texOutputWriter = new PrintWriter(new BufferedWriter(texOutput));

    public void errorOnTexOutput() {
        texOutputWriter = new PrintWriter(new OutputStreamWriter(new OutputStream() {
            @Override
            public void write(int i) throws IOException {
                throw new IOException("Error!");
            }
        }));
    }

    @Override
    public void configure(Settings settings, PrintStream systemOut, PrintStream systemErr) {
        close();

    }

    @Override
    public PrintWriter quiet() {
        return quietWriter;
    }

    @Override
    public PrintWriter elements() {
        return elementsWriter;
    }

    @Override
    public PrintWriter algorithm() {
        return algorithmWriter;
    }

    @Override
    public PrintWriter dumpAll() {
        return dumpAllWriter;
    }

    @Override
    public PrintWriter finalTexOutput() {
        return texOutputWriter;
    }

    @Override
    public void close() {
        quietWriter.flush();
        elementsWriter.flush();
        algorithmWriter.flush();
        dumpAllWriter.flush();
        texOutputWriter.flush();

        quiet.getBuffer().setLength(0);
        elements.getBuffer().setLength(0);
        algorithm.getBuffer().setLength(0);
        dumpAll.getBuffer().setLength(0);
        texOutput.getBuffer().setLength(0);
    }

    public String quietCollected() {
        quietWriter.flush();
        return quiet.toString();
    }

    public String elementsCollected() {
        elementsWriter.flush();
        return elements.toString();
    }

    public String algorithmCollected() {
        algorithmWriter.flush();
        return algorithm.toString();
    }

    public String dumpAllCollected() {
        dumpAllWriter.flush();
        return dumpAll.toString();
    }

    public String texOutputCollected() {
        texOutputWriter.flush();
        return texOutput.toString();
    }

}
