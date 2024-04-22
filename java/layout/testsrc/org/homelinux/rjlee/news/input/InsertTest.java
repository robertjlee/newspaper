package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;

class InsertTest {

    private Settings settings;
    private Insert insert;
    private MockPath path;

    @BeforeEach
    void setUp() {
        this.path = MockPath.createMockPathWithNameAndContent("insert.tex", "Content");
        Properties headerProperties = new Properties();
        headerProperties.setProperty("Width","fill");
        headerProperties.setProperty("Height","2.147in");
        headerProperties.setProperty("Preamble1", "\\typeout{Preambling!}");
        Headers headers = new Headers(path, headerProperties, settings);
        settings = new Settings(new Properties());
        insert = new Insert(headers, settings);
    }

    @Test
    void skipHalley() {
        assertTrue(insert.skipHalley());
    }

    @Test
    void width() {
        insert.setNumColumnsOnPage(10);
        double expectedWidth = 10 * settings.getColumnWidth() + 9 * settings.getAlleyWidth();
        assertEquals(expectedWidth, insert.width(), 0.0001);
    }

    @Test
    void height() {
        assertEquals(2.147, insert.height(), 0.0);
    }

    @Test
    void path() {
        assertSame(path, insert.path());
    }

    @Test
    void area() {
        insert.setNumColumnsOnPage(10);
        double expectedWidth = 10 * settings.getColumnWidth() + 9 * settings.getAlleyWidth();
        assertEquals(expectedWidth * 2.147, insert.area(), 0.0);
    }

    @Test
    void columnInches() {
        insert.setNumColumnsOnPage(10);
        assertEquals(10 * 2.147, insert.columnInches(), 0.0);
    }

    @Test
    void cols() {
        insert.setNumColumnsOnPage(10);
        assertEquals(10, insert.cols());
    }


    @Test
    void preambleLines() {
        assertEquals(singleton("\\typeout{Preambling!}"), insert.preambleLines().collect(Collectors.toSet()));
    }

    @Test
    void copyTo() throws IOException {

        try (StringWriter sw = new StringWriter();
             BufferedWriter bw = new BufferedWriter(sw);
             PrintWriter pw = new PrintWriter(bw)) {
            insert.copyTo(pw, settings.getOut());
            pw.flush();

            assertEquals("\\setemergencystretch\\numnewscols\\hsize\nContent\n", sw.toString());
        }

    }

    @Test
    void testToString() {
        assertEquals("[Insert:insert.tex([16]25.590551x2.147000)]", insert.toString());
    }
}