package org.homelinux.rjlee.news;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

/**
 * Most of the CmdLineOptions are tested by SettingsTest
 *
 * @author Robert
 */
class CmdLineOptionsTest {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PrintStream mockStdOut = new PrintStream(baos);

    @Test
    void noArgs() throws UnsupportedEncodingException {
        new CmdLineOptions(mockStdOut, new String[0]);
        mockStdOut.flush();

        Assertions.assertEquals("Usage: java -jar layout.jar <srcdir1> [ <srdir2> ... ]\n", baos.toString("UTF-8"));
    }
}