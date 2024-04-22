package org.homelinux.rjlee.news.logging;

import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.DebugLevel;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.homelinux.rjlee.news.file.TmpFileUtils.recursiveDeleteOnExit;
import static org.junit.jupiter.api.Assertions.*;

class LoggerImpl_ConfiguredTest {

    private static final String LOG = "out.log";
    private static final String TEX = "outputConfigured.tex";
    private static Path tmpDir;
    private static Logger instance = Logger.getInstance();
    private ByteArrayOutputStream outBytes;
    private ByteArrayOutputStream errBytes;
    private PrintStream out;
    private PrintStream err;

    @BeforeAll
    static void beforeAll() throws IOException {

        tmpDir = Files.createTempDirectory(LoggerImpl_ConfiguredTest.class.getSimpleName());
        System.out.println("tmpDir = " + tmpDir);

    }

    private static Properties buildProperties(DebugLevel logFileLevel, DebugLevel stdOutLevel, DebugLevel stdErrLevel) {
        Properties properties = new Properties();
        properties.put("out", tmpDir.toString());
        properties.put("logFile", LOG);
        properties.put("jobName", TEX.replace(".tex", ""));
        properties.put("logFileLevel", logFileLevel.toString());
        properties.put("stdOutLevel", stdOutLevel.toString());
        properties.put("stdErrLevel", stdErrLevel.toString());
        return properties;
    }

    @BeforeEach
    void setUp() throws IOException {

        Files.deleteIfExists(tmpDir.resolve(LOG));
        Files.deleteIfExists(tmpDir.resolve(TEX));

        outBytes = new ByteArrayOutputStream();
        out = new PrintStream(new BufferedOutputStream(outBytes));
        errBytes = new ByteArrayOutputStream();
        err = new PrintStream(new BufferedOutputStream(errBytes));

    }

    @AfterEach
    void tearDown() {
        instance.close();
    }

    @AfterAll
    static void afterAll() {
        if (tmpDir != null) recursiveDeleteOnExit(tmpDir);
    }

    String readLogFile() {
        Path logFile = tmpDir.resolve(LOG);
        try (BufferedReader r = Files.newBufferedReader(logFile)) {
            return r.lines().collect(Collectors.joining("\n"));
        } catch (NoSuchFileException e) {
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

   String readTexFile() {
        Path logFile = tmpDir.resolve(TEX);
        try (BufferedReader r = Files.newBufferedReader(logFile)) {
            return r.lines().collect(Collectors.joining("\n"));
        } catch (NoSuchFileException e) {
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = "" +
            "QUIET, QUIET, QUIET, false, true, true\n" +
            "QUIET, ALGORITHM, DUMP_ALL, false, true, true\n" +
            "DUMP_ALL, QUIET, ELEMENTS, false, true, true\n" +
            "DUMP_ALL, DUMP_ALL, DUMP_ALL, false, true, true\n"
    )
    void quiet(DebugLevel stdOutLevel, DebugLevel stdErrLevel, DebugLevel logFileLevel,
               boolean expectInSysOut, boolean expectInSysErr, boolean expectInLog) {
        Settings settings = new Settings(buildProperties(logFileLevel, stdOutLevel, stdErrLevel));
        instance.configure(settings, out, err);

        PrintWriter pw = instance.quiet();
        pw.println("Test");
        pw.flush();
        assertAll(
                () -> assertEquals(expectInSysOut ? "Test\n" : "", outBytes.toString(), "System out"),
                () -> assertEquals(expectInSysErr ? "Test\n" : "", errBytes.toString(), "System err"),
                () -> assertEquals(expectInLog ? "Test" : "", readLogFile(), "Log file")
        );

    }
    @ParameterizedTest
    @CsvSource(textBlock = "" +
            "QUIET, QUIET, QUIET, false, false, false\n" +
            "QUIET, ALGORITHM, DUMP_ALL, false, true, true\n" +
            "DUMP_ALL, QUIET, ELEMENTS, true, false, true\n" +
            "DUMP_ALL, DUMP_ALL, DUMP_ALL, false, true, true\n"
    )
    void elements(DebugLevel stdOutLevel, DebugLevel stdErrLevel, DebugLevel logFileLevel,
               boolean expectInSysOut, boolean expectInSysErr, boolean expectInLog) {
        Settings settings = new Settings(buildProperties(logFileLevel, stdOutLevel, stdErrLevel));
        instance.configure(settings, out, err);

        PrintWriter pw = instance.elements();
        pw.println("Test");
        pw.flush();
        assertAll(
                () -> assertEquals(expectInSysOut ? "Test\n" : "", outBytes.toString(), "System out"),
                () -> assertEquals(expectInSysErr ? "Test\n" : "", errBytes.toString(), "System err"),
                () -> assertEquals(expectInLog ? "Test" : "", readLogFile(), "Log file")
        );

    }
    @ParameterizedTest
    @CsvSource(textBlock = "" +
            "QUIET, QUIET, QUIET, false, false, false\n" +
            "QUIET, ALGORITHM, DUMP_ALL, false, true, true\n" +
            "DUMP_ALL, QUIET, ELEMENTS, true, false, false\n" +
            "DUMP_ALL, DUMP_ALL, DUMP_ALL, false, true, true\n"
    )
    void algorithm(DebugLevel stdOutLevel, DebugLevel stdErrLevel, DebugLevel logFileLevel,
               boolean expectInSysOut, boolean expectInSysErr, boolean expectInLog) {
        Settings settings = new Settings(buildProperties(logFileLevel, stdOutLevel, stdErrLevel));
        instance.configure(settings, out, err);

        PrintWriter pw = instance.algorithm();
        pw.println("Test");
        pw.flush();
        assertAll(
                () -> assertEquals(expectInSysOut ? "Test\n" : "", outBytes.toString(), "System out"),
                () -> assertEquals(expectInSysErr ? "Test\n" : "", errBytes.toString(), "System err"),
                () -> assertEquals(expectInLog ? "Test" : "", readLogFile(), "Log file")
        );

    }
    @ParameterizedTest
    @CsvSource(textBlock = "" +
            "QUIET, QUIET, QUIET, false, false, false\n" +
            "QUIET, ALGORITHM, DUMP_ALL, false, false, true\n" +
            "DUMP_ALL, QUIET, ELEMENTS, true, false, false\n" +
            "DUMP_ALL, DUMP_ALL, DUMP_ALL, false, true, true\n"
    )
    void dumpAll(DebugLevel stdOutLevel, DebugLevel stdErrLevel, DebugLevel logFileLevel,
               boolean expectInSysOut, boolean expectInSysErr, boolean expectInLog) {
        Settings settings = new Settings(buildProperties(logFileLevel, stdOutLevel, stdErrLevel));
        instance.configure(settings, out, err);

        PrintWriter pw = instance.dumpAll();
        pw.println("Test");
        pw.flush();
        assertAll(
                () -> assertEquals(expectInSysOut ? "Test\n" : "", outBytes.toString(), "System out"),
                () -> assertEquals(expectInSysErr ? "Test\n" : "", errBytes.toString(), "System err"),
                () -> assertEquals(expectInLog ? "Test" : "", readLogFile(), "Log file")
        );

    }


    @Test
    void errorOnLogFileOpen() {
        Settings settings = new Settings(buildProperties(DebugLevel.QUIET, DebugLevel.QUIET, DebugLevel.QUIET)) {
            @Override
            public Path getOut() {
                return MockPath.createMockPathForErrorOnOpenOutput(true);
            }

            @Override
            public Path getLogFile() {
                return MockPath.createMockPathForErrorOnOpenOutput(false);
            }
        };
        instance.configure(settings, out, err);
        PrintWriter pw = instance.quiet();
        pw.println("Test");
        pw.flush();
        assertAll(
                () -> assertEquals("", outBytes.toString(), "System out"),
                () -> assertTrue( errBytes.toString().startsWith("Failed to configure logger! Error!\n" +
                        "java.io.IOException: Error!\n"), "System err should contain error from closing log file; was " + errBytes.toString()),
                () -> assertTrue( errBytes.toString().endsWith("Test\n"), "System err should contain the logged error"),
                () -> assertEquals("", readLogFile(), "Log file")
        );
    }

    @Test
    void errorOnLogFileClose_shutdown() {
        Settings settings = new Settings(buildProperties(DebugLevel.QUIET, DebugLevel.QUIET, DebugLevel.QUIET)) {
            @Override
            public Path getOut() {
                return MockPath.createMockPathForErrorOnCloseOutput(true);
            }

            @Override
            public Path getLogFile() {
                return MockPath.createMockPathForErrorOnCloseOutput(false);
            }
        };
        instance.configure(settings, out, err);

        // now reconfigure to get the error message
        instance.close();


        assertAll(
                () -> assertEquals("", outBytes.toString(), "System out"),
                () -> assertTrue( errBytes.toString().startsWith("Failed to close logger! Error!\n" +
                        "java.io.IOException: Error!"), "System err should contain error from closing log file; was " + errBytes.toString()),
                () -> assertEquals("", readLogFile(), "Log file")
        );
    }

    @Test
    void errorOnLogFileClose_reconfigure() {
        Settings settings = new Settings(buildProperties(DebugLevel.QUIET, DebugLevel.QUIET, DebugLevel.QUIET)) {
            @Override
            public Path getOut() {
                return MockPath.createMockPathForErrorOnCloseOutput(true);
            }

            @Override
            public Path getLogFile() {
                return MockPath.createMockPathForErrorOnCloseOutput(false);
            }
        };
        instance.configure(settings, out, err);

        // now reconfigure to get the error message
        instance.configure(new Settings(buildProperties(DebugLevel.QUIET, DebugLevel.QUIET, DebugLevel.QUIET)), out, err);


        PrintWriter pw = instance.quiet();
        pw.println("Test");
        pw.flush();
        assertAll(
                () -> assertEquals("", outBytes.toString(), "System out"),
                () -> assertTrue( errBytes.toString().startsWith("Failed to configure logger! Error!\n" +
                        "java.io.IOException: Error!\n"), "System err should contain error from closing log file; was " + errBytes.toString()),
                () -> assertTrue( errBytes.toString().endsWith("Test\n"), "System err should contain the logged error"),
                () -> assertEquals("Test", readLogFile(), "Log file")
        );
    }


    @Test
    void texOutput() {
        Settings settings = new Settings(buildProperties(DebugLevel.QUIET, DebugLevel.QUIET, DebugLevel.QUIET));
        instance.configure(settings, out, err);

        PrintWriter pw = instance.finalTexOutput();
        pw.println("\\endinput"); // a very short TeX file!
        pw.flush();

        assertEquals("\\endinput", readTexFile());
    }
}