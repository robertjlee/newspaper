package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.settings.Settings;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A factory to shell out to LaTeX. Allows us to mock out the external process for unit testing.
 *
 * @author Robert
 */
public class LatexProcessFactory implements ShellProcessFactory {

    public ProcessBuilder build(Settings settings, Path wdPath, String... extraCmdLine) {
        // NB: We require a unicode engine for newtx, so we must use pdflatex, never latex.
        Stream<String> cmdLine = Stream.concat(
                Stream.of(settings.getLatex()),
                Arrays.stream(settings.getLatexCmdLine()));
        if (extraCmdLine.length > 0) {
            cmdLine = Stream.concat(cmdLine, Arrays.stream(extraCmdLine));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(cmdLine.toArray(String[]::new));
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);

        // the trailing colon means "add the default value of TEXINPUTS
        processBuilder.environment()
                        .put("TEXINPUTS", settings.getTexInputs());

        processBuilder.directory(wdPath.toFile());

        return processBuilder;
    }
}
