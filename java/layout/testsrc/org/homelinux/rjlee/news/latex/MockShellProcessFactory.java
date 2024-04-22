package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public class MockShellProcessFactory implements ShellProcessFactory {

    private boolean errorOnStdOut;
    private boolean errorOnStdErr;
    private boolean errorOnStdIn;

    private StringBuilder stdOut;
    private StringBuilder stdErr;
    private OutputStream stdIn;

    private boolean crash; // return null process
    private boolean errorOnStart; // throw IOException
    private int exitCode = 0;

    public MockShellProcessFactory(StringBuilder stdOut, StringBuilder stdErr, OutputStream stdIn) {
        this(stdOut, stdErr, stdIn, false, false);
    }

    public MockShellProcessFactory(StringBuilder stdOut, StringBuilder stdErr, OutputStream stdIn, boolean crash, boolean errorOnStart) {
        this.errorOnStart = errorOnStart;
        errorOnStdOut = false;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.stdIn = stdIn;
        this.crash = crash;
    }

    public MockShellProcessFactory withErrorOnStdOut() {
        this.errorOnStdOut = true;
        return this;
    }

    public MockShellProcessFactory withErrorOnStdErr() {
        this.errorOnStdErr = true;
        return this;
    }

    public MockShellProcessFactory withErrorOnStdIn() {
        this.errorOnStdIn = true;
        return this;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public ProcessBuilder build(Settings settings, Path wdPath, String... extraCmdLine) {
        return null;
    }

    @Override
    public Process run(Settings settings, Path wdPath, String... extraCmdLine) throws IOException {
        if (errorOnStart) throw new IOException("Error!");
        return crash ? null :
                new MockProcess(stdOut.toString(), stdErr.toString(), stdIn, exitCode, errorOnStdOut, errorOnStdErr, errorOnStdIn);
    }
}
