package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.nio.file.Path;

public interface ShellProcessFactory {

    ProcessBuilder build(Settings settings, Path wdPath, String... extraCmdLine);

    default Process run(Settings settings, Path wdPath, String... extraCmdLine) throws IOException {
        return build(settings, wdPath, extraCmdLine).start();
    }

}
