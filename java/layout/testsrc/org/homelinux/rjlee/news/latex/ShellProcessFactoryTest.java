package org.homelinux.rjlee.news.latex;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Sadly, ProcessBuilder is final, so this class can only be tested by actually shelling out.
 * At least we can write a shell-out program that won't work!
 *
 * @author Robert
 */
class ShellProcessFactoryTest {

    @Test
    void run() {
        ShellProcessFactory factory = (settings, wdPath, extraCmdLine) -> new ProcessBuilder("illegal-command-name/!\"£$%^&*(#-;wvz)@',.");
        assertThrows(IOException.class, () -> factory.run(null, null));
    }
}