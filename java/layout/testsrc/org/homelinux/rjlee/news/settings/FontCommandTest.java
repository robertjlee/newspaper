package org.homelinux.rjlee.news.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FontCommandTest {
    public static final String DEFAULT_COMMAND = "\\fontencoding{TU}\\fontfamily{cmr}\\fontseries{m}\\fontshape{n}\\fontsize{12}{14}\\selectfont";

    private Properties p;
    private FontCommand cmd;
    private Settings settings;

    @BeforeEach
    void setUp() {
        settings = new Settings(new Properties());
        p = new Properties();
        cmd = new FontCommand(settings, p, "", null, "cmr", "m", "n", 12);
    }

    @Test
    void getFontCommand() {
        assertEquals(DEFAULT_COMMAND, cmd.getFontCommand());
    }

    @Test
    void getFontCommand_Calculated() {
        p.put("Encoding", "T1");
        p.put("Family", "ptm");
        p.put("Series", "bx");
        p.put("Size", "8");
        assertEquals("\\fontencoding{T1}\\fontfamily{ptm}\\fontseries{bx}\\fontshape{n}\\fontsize{8}{10}\\selectfont", cmd.getFontCommand());
    }

    @Test
    void getFontCommand_Overridden() {
        p.put("Encoding", "T1");
        p.put("Family", "ptm");
        p.put("Series", "bx");
        p.put("Size", "8");
        p.put("Command", "\\huge");
        assertEquals("\\huge", cmd.getFontCommand());
    }

    @Test
    void getDefaultFontCommand() {
        assertEquals(DEFAULT_COMMAND, cmd.getDefaultFontCommand());
    }

    @Test
    void getDefaultFontCommand_Overridden() {
        p.put("Family", "ptm");
        p.put("Series", "bx");
        p.put("Size", "8");
        p.put("Command", "\\huge");
        assertEquals("\\fontencoding{TU}\\fontfamily{ptm}\\fontseries{bx}\\fontshape{n}\\fontsize{8}{10}\\selectfont", cmd.getDefaultFontCommand());
    }

    @Test
    void getFamily() {
        assertEquals("cmr", cmd.getFamily());
    }

    @Test
    void getFamily_Overridden() {
        p.put("Family", "ptm");
        assertEquals("ptm", cmd.getFamily());
    }

    @Test
    void getSeries() {
        assertEquals("m", cmd.getSeries());
    }

    @Test
    void getSeries_Overridden() {
        p.put("Series", "bx");
        assertEquals("bx", cmd.getSeries());
    }

    @Test
    void getShape() {
        assertEquals("n", cmd.getShape());
    }

    @Test
    void getShape_Overridden() {
        p.put("Shape", "sl");
        assertEquals("sl", cmd.getShape());
    }

    @Test
    void getSize() {
        assertEquals(12, cmd.getSize());
    }

    @Test
    void getSize_Overridden() {
        p.put("Size", "8");
        assertEquals(8, cmd.getSize());
    }

    @Test
    void getSize_Negative() {
        p.put("Size", "-1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cmd.getSize());
        assertEquals("Bad setting value [-1] for [Size]; not in range 0-2147483645", ex.getMessage());
    }

    // Integer.MAX_VALUE and Integer.MAX_VALUE - 1 are impossible, because then the spacing would exceed
    // Integer.MAX_VALUE:
    @ParameterizedTest
    @ValueSource(ints = {2147483646, 2147483647})
    void getSize_TooHigh(long value) {
        p.put("Size", "" + value);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cmd.getSize());
        assertEquals(String.format("Bad setting value [%d] for [Size]; not in range 0-2147483645", value), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2147483648", "some", "\n"})
    void getSize_BadInt(String value) {
        p.put("Size", value);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cmd.getSize());
        assertEquals("Bad setting value [" + value + "] for [Size]; not a valid Integer. Must be in range 0-2147483645", ex.getMessage());
    }

    @Test
    void getSpacing() {
        assertEquals(14, cmd.getSpacing());
    }

    @Test
    void getSpacing_calculated() {
        p.put("Size", "8");
        assertEquals(10, cmd.getSpacing());
    }

    @Test
    void getSpacing_Overridden() {
        p.put("Size", "8");
        p.put("Spacing", "11");
        assertEquals(11, cmd.getSpacing());
    }

    @Test
    void testToString() {
        assertEquals(DEFAULT_COMMAND, cmd.toString());
    }
}