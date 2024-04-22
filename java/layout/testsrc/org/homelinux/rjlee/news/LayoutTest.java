package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class LayoutTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void constructor() {
        String[] cmdLine = {"a", "b", "c"};
        Layout layout = new Layout(cmdLine);
        assertSame(cmdLine, layout.getCmdLine());
    }

    @Test
    void main() {
        String[] cmdLine = {null};
        assertThrows(NullPointerException.class, () -> Layout.main(cmdLine));
    }

    @Test
    void run() {
        MockNewspaperLayout mockNewspaperLayout = new MockNewspaperLayout();
        CapturingLogger logger = new CapturingLogger();
        new Layout(logger) {
            @Override
            protected NewspaperLayout createEmptyLayout(Settings settings, CmdLineOptions cmdLineOptions) {
                mockNewspaperLayout.init(settings, cmdLineOptions.getInputDirectories());
                return mockNewspaperLayout;
            }
        }.run();
        List<String> methodCallOrder = mockNewspaperLayout.getMethodCallOrder();
        System.err.println("methodCallOrder = " + methodCallOrder);
        List<String> expected = Arrays.asList("init/constructor", "layOutNewspaper", "validate", "preambleLines", "getPages");
        System.err.println("expected = " + expected);
        assertEquals(expected, methodCallOrder);
        System.err.println("done");

    }

    @Test
    void createEmptyLayout() {
        Settings settings = new Settings(new Properties());
        CmdLineOptions cmdLineOptions = new CmdLineOptions(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        NewspaperLayout emptyLayout = new Layout().createEmptyLayout(settings, cmdLineOptions);
        assertInstanceOf(NewspaperLayoutImpl.class, emptyLayout);
    }
}