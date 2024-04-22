package org.homelinux.rjlee.news.elements;

import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ValleyTest {

    public static final int NUM_COLS = 5;
    private Settings settings = new Settings(new Properties());
    private Valley valley;

    @BeforeEach
    void setUp() {
        valley = new Valley(settings, NUM_COLS);
    }

    @Test
    void width() {
        assertEquals(settings.getColumnWidth() * NUM_COLS + (settings.getAlleyWidth() * (NUM_COLS - 1)), valley.width(), 0.00001);
    }

    @Test
    void height() {
        assertEquals(settings.getAlleyHeight(), valley.height(), 0.0);
    }

    @Test
    void path() {
        assertNull(valley.path());
    }

    @Test
    void skipHalley() {
        assertTrue(valley.skipHalley());
    }

    @Test
    void addColumn() {
        valley.addColumn();
        assertEquals(settings.getColumnWidth() * (NUM_COLS + 1) + (settings.getAlleyWidth() * NUM_COLS), valley.width(), 0.00001);
    }

    @Test
    void testToString() {
        assertEquals("V-mode alley{cols=5}", valley.toString());
    }
}