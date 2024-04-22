package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class LatexProcessFactoryTest {

    private final LatexProcessFactory factory = new LatexProcessFactory();
    private Settings settings;
    private Path wdPath;

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        settings = new Settings(properties);
        wdPath = FileSystems.getDefault().getPath("/");
    }

    @Test
    void run() {
        ProcessBuilder pb = factory.build(settings, wdPath, "--version", "--help", "/?");

        assertAll(
                () -> assertEquals(wdPath.toFile(), pb.directory()),
                () -> assertEquals(Arrays.asList("pdflatex","--interaction=nonstopmode","--version","--help","/?"), pb.command()),
                () -> assertTrue(pb.redirectErrorStream()),
                () -> assertSame(ProcessBuilder.Redirect.INHERIT, pb.redirectInput()),
                () -> assertSame(ProcessBuilder.Redirect.PIPE, pb.redirectOutput()),
                () -> assertSame(ProcessBuilder.Redirect.PIPE, pb.redirectError())
        );
    }
}