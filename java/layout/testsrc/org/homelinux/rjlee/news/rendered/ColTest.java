package org.homelinux.rjlee.news.rendered;

import org.homelinux.rjlee.news.elements.Part;
import org.homelinux.rjlee.news.elements.Valley;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ColTest {

    private Settings settings;
    private Col col;

    @BeforeEach
    void setUp() {
        settings = new Settings(new Properties());
        col = new Col(settings, null);
    }

    @Test
    void set() {
        Part p = new Valley(settings, 2);
        Col.ColFragment fragment = col.new ColFragment(p, 1, 2, null);
        col.set(fragment);

        assertEquals("[Fragment empty@[0.0-1.0], Fragment for part V-mode alley{cols=2}@[1.0-2.0], Fragment empty@[2.0-26.5748031496063]]", col.toString());
    }

    @Test
    void set_mergeAlleys() {
        Part alley = new Valley(settings, 2); // will be kept across both columns
        col.set(col.new ColFragment(alley, 1));
        Part alley2 = new Valley(settings, 2); // will be discarded after merge
        Col col2 = new Col(settings, col);
        col2.set(col.new ColFragment(alley2, 1));

        assertAll(
                () -> assertSame(alley, col.getFrags().get(1).getPart()),
                () -> assertSame(alley, col2.getFrags().get(1).getPart())
        );
    }

    @Test
    void setFailed() {
        Part p = new Valley(settings, 2);
        Col.ColFragment fragment = col.new ColFragment(p, 1, settings.getColumnHeight() * 2, null);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> col.set(fragment));
        assertEquals(String.format("Failed to place fixed-place element %s on col %s", fragment, col), ex.getMessage());
    }

    @Test
    void longestEmpty() {
        Part p = new Valley(settings, 2);
        Col.ColFragment fragment = col.new ColFragment(p, 1);
        col.set(fragment);

        assertEquals("Fragment empty@[1.125-26.5748031496063]", col.longestEmpty().toString());
    }

    @Test
    void empty() {
        Col.ColFragment[] initialEmpty = col.empty().toArray(Col.ColFragment[]::new);
        assertAll(
                () -> assertEquals(1, initialEmpty.length),
                () -> assertEquals("Fragment empty@[0.0-26.5748031496063]", initialEmpty[0].toString())
        );
    }

    @Test
    void getFrags() {
        Part p = new Valley(settings, 2);
        Col.ColFragment fragment = col.new ColFragment(p, 1, 2, null);
        col.set(fragment);

        List<Col.ColFragment> actualFrags = col.getFrags();
        assertEquals("[Fragment empty@[0.0-1.0], Fragment for part V-mode alley{cols=2}@[1.0-2.0], Fragment empty@[2.0-26.5748031496063]]", actualFrags.toString());
    }

}