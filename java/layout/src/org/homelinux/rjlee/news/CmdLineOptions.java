package org.homelinux.rjlee.news;

import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Parse command-line options. Currently, we expect only directories, as everything else is in a settings file.
 *
 * @author Robert
 */
public class CmdLineOptions {

    private Path[] inputDirectories;

    public CmdLineOptions(PrintStream stdOut, String... args) {
        this(stdOut, Arrays.stream(args).map(FileSystems.getDefault()::getPath).toArray(Path[]::new));

    }

    public CmdLineOptions(PrintStream stdOut, Path... inputDirectories) {
        this.inputDirectories = inputDirectories;
        // NB: This goes to stdout directly, as we haven't set up the logger yet (the settings files are needed
        // for that)
        if (inputDirectories.length == 0)
            stdOut.println("Usage: java -jar layout.jar <srcdir1> [ <srdir2> ... ]");

    }

    public Path[] getInputDirectories() {
        return inputDirectories;
    }
}
