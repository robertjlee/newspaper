package org.homelinux.rjlee.news.logging;

import org.homelinux.rjlee.news.settings.DebugLevel;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class LoggerImpl implements Logger {
    private static final Logger instance = new LoggerImpl();
    private PrintWriter quiet;
    private PrintWriter elements;
    private PrintWriter algorithm;
    private PrintWriter dumpAll;
    private OutputStream logWriter;
    private PrintStream systemErr = System.err;
    private PrintWriter texOutput;
    private Path outTexFile;
    private boolean allowTexFileOverwrite;

    static Logger getInstance() {
        return instance;
    }

    LoggerImpl() {
    }

    @Override
    public void configure(Settings settings, PrintStream systemOut, PrintStream systemErr) {
        this.systemErr = systemErr;
        try {
            closeLogWriter();
        } finally {
            // now actually configure the logs - even if the shutdown failed!
            configureImpl(settings, systemOut, systemErr);
        }

    }

    private void configureImpl(Settings settings, PrintStream systemOut, PrintStream systemErr) {
        try {
            Path logFile = settings.getOut().resolve(settings.getLogFile());
            logWriter = new BufferedOutputStream(Files.newOutputStream(logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
        } catch (IOException e) {
            final PrintWriter errorWriter = quiet();
            errorWriter.println("Failed to configure logger! " + e.getMessage());
            e.printStackTrace(errorWriter);

            logWriter = new OutputStream() {
                @Override
                public void write(int i) {
                }
            };
        }

        outTexFile = settings.getOut().resolve(settings.getJobName() + ".tex");
        allowTexFileOverwrite = settings.isAllowTexFileOverwrite();

        DebugLevel logLevel = settings.getLogFileLevel();
        DebugLevel outLevel = settings.getStdOutLevel();
        DebugLevel errLevel = settings.getStdErrLevel();
        // we should only call "quiet" to log critical errors:
        // always go to System err, but also to logWriter if configured.
        quiet = new PrintWriter(new PeekWriter(new PrintWriter(logWriter, true), systemErr), true);

        elements = configureLevel(DebugLevel.ELEMENTS, systemOut, systemErr, logLevel, outLevel, errLevel);
        algorithm = configureLevel(DebugLevel.ALGORITHM, systemOut, systemErr, logLevel, outLevel, errLevel);
        dumpAll = configureLevel(DebugLevel.DUMP_ALL, systemOut, systemErr, logLevel, outLevel, errLevel);
    }

    private PrintWriter configureLevel(DebugLevel level, PrintStream systemOut, PrintStream systemErr, DebugLevel logLevel, DebugLevel outLevel, DebugLevel errLevel) {
        boolean logElementsToLog = logLevel.compareTo(level) >= 0;
        boolean logElementsToErr = errLevel.compareTo(level) >= 0;
        boolean logElementsToOut = !logElementsToErr && outLevel.compareTo(level) >= 0;
        PrintWriter writer;
        if (!logElementsToOut && !logElementsToErr)
            writer = new PrintWriter(new NullWriter());
        else if (logElementsToOut)
            writer = new PrintWriter(systemOut);
        else
            writer = new PrintWriter(systemErr);
        return logElementsToLog ? new PrintWriter(new PeekWriter(writer, logWriter), true) : writer;
    }

    private void closeLogWriter() {
        // shut down any existing configuration
        try {
            if (logWriter != null)
                logWriter.close();
        } catch (IOException e) {
            final PrintWriter errorWriter = quiet();
            errorWriter.println("Failed to configure logger! " + e.getMessage());
            e.printStackTrace(errorWriter);
        } finally {
            logWriter = null;
            texOutput = null;
        }
    }

    @Override
    public PrintWriter quiet() {
        if (quiet == null) return new MyPrintWriter(systemErr);
        return quiet;
    }

    @Override
    public PrintWriter elements() {
        if (elements == null) return new MyPrintWriter(systemErr);
        return elements;
    }

    @Override
    public PrintWriter algorithm() {
        if (algorithm == null) return new MyPrintWriter(systemErr);
        return algorithm;
    }

    @Override
    public PrintWriter dumpAll() {
        if (dumpAll == null) return new MyPrintWriter(systemErr);
        return dumpAll;
    }

    @Override
    public PrintWriter finalTexOutput() {
        if (texOutput == null) {
            PrintWriter allLogs = dumpAll();
            try {
                StandardOpenOption[] openOptions = allowTexFileOverwrite ?
                        new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING} :
                        new StandardOpenOption[]{StandardOpenOption.CREATE_NEW};
                OutputStream texOutRaw = Files.newOutputStream(outTexFile, openOptions);
                PeekWriter outWriter = new PeekWriter(allLogs, new BufferedOutputStream(texOutRaw));
                texOutput = new PrintWriter(outWriter, true);
                algorithm().println("Opened output file " + outTexFile);
            } catch (IOException | NullPointerException e) {
                PrintStream err = systemErr;
                e.printStackTrace(err);
                err.println("Error opening output TeX file " + outTexFile);
                e.printStackTrace(err);
                err.println("Continuing with logging output only, but final compile will fail.");
                texOutput = allLogs;
            }
        }
        return texOutput;
    }

    @Override
    public void close() {
        Stream<Closeable> toClose = Stream.of(elements,
                algorithm,
                dumpAll,
                logWriter,
                texOutput,
                quiet);
        elements = algorithm = dumpAll = quiet = texOutput = null;
        logWriter = null;
        toClose.forEach(this::close);
        texOutput = null;
        systemErr = System.err;
    }

    private <T extends Closeable> void close(T e) {
        try {
            if (e != null) e.close();
        } catch (IOException ex) {
            // if we can't close the logger, best log it!
            PrintWriter logger = quiet();
            logger.println("Failed to close logger! " + ex.getMessage());
            ex.printStackTrace(logger);
        }
    }
}
