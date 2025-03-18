package org.homelinux.rjlee.news.partial;

import org.homelinux.rjlee.news.elements.FixedSize;
import org.homelinux.rjlee.news.elements.Part;
import org.homelinux.rjlee.news.input.Headers;
import org.homelinux.rjlee.news.mockpath.MockPath;
import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FixedElementsRelativeLayoutTest {

    private Settings settings;
    private FixedElementsRelativeLayout relativeLayout;

    @BeforeEach
    void setUp() {

        settings = new Settings(new Properties());
        Page page = new Page(1, 5, settings);
        relativeLayout = new FixedElementsRelativeLayout(5, 0, page, settings);
    }

    @Test
    void insert_cols() {
        FixedSize a = new FixedSizeInsert("A", 0.5, 0.5);
        FixedElementsRelativeLayout.LayoutSection sectionA = relativeLayout.new LayoutSection(a, 1);
        assertEquals(1, sectionA.cols(), "Should be in columns, not inches");
    }

    @Test
    void insert_length() {
        FixedSize a = new FixedSizeInsert("A", 0.5, 0.5);
        FixedElementsRelativeLayout.LayoutSection sectionA = relativeLayout.new LayoutSection(a, 1);
        assertEquals(0.5, sectionA.getLength(), "Should be in inches");
    }

    @Test
    void insert_startCol() {
        FixedSize a = new FixedSizeInsert("A", 0.5, 0.5);
        FixedElementsRelativeLayout.LayoutSection sectionA = relativeLayout.new LayoutSection(a, 2);
        assertEquals(2, sectionA.getStartCol());
    }

    @Test
    void insert_inserts() {
        FixedSize a = new FixedSizeInsert("A", 0.5, 0.5);
        FixedElementsRelativeLayout.LayoutSection sectionA = relativeLayout.new LayoutSection(a, 1);
        FixedSize fixedSize = new FixedSizeInsert("B", 1, 5.1);
        sectionA.merge(fixedSize, true, 1);
        assertEquals(Arrays.asList(a, fixedSize), sectionA.getInserts());
    }

    @Test
    void offPageLeft() {
        FixedSize fixedSize = new FixedSizeInsert("A", 5, 5.1);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> relativeLayout.new LayoutSection(fixedSize, -1));
        assertEquals("Insert A(5.0×5.1) attempt to set at col -1; off the page!", ex.getMessage());
    }

    @Test
    void offPageRight() {
        FixedSize fixedSize = new FixedSizeInsert("A", 5, 5.1);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> relativeLayout.new LayoutSection(fixedSize, 5));
        assertEquals("Insert A(5.0×5.1) from col 5 will overflow 5 columns by using column 8", ex.getMessage());
    }

    @Test
    void merge_offPageLeft() {
        FixedSize a = new FixedSizeInsert("A", 0.5, 0.5);
        FixedElementsRelativeLayout.LayoutSection sectionA = relativeLayout.new LayoutSection(a, 1);
        FixedSize fixedSize = new FixedSizeInsert("B", 5, 5.1);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> sectionA.merge(fixedSize, true, -1));
        assertEquals("Insert B(5.0×5.1) attempt to set at col -1; off the page!", ex.getMessage());
    }

    @Test
    void merge_offPageRight() {
        FixedSize a = new FixedSizeInsert("A", 0.5, 0.5);
        FixedElementsRelativeLayout.LayoutSection sectionA = relativeLayout.new LayoutSection(a, 1);
        FixedSize fixedSize = new FixedSizeInsert("B", 5, 5.1);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> sectionA.merge(fixedSize, true, 5));
        assertEquals("Insert B(5.0×5.1) from col 5 will overflow 5 columns by using column 9", ex.getMessage());
    }


    @Test
    void testToString() {
        assertEquals("PartialLayout:0.000000in:[]", relativeLayout.toString());
    }

    @Test
    void fit_oversize() {
        FixedSize fixedSize = new FixedSizeInsert("A", settings.getPageWidth(), 5);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> relativeLayout.fit(fixedSize, 0));
        assertEquals("Insert or spanning article too wide for page! Insert width=16; available columns 0-4", ex.getMessage());
    }

    @Test
    void fit_enlarge() {
        FixedSize fixedSize = new FixedSizeInsert("A", settings.getColumnWidth() * 6 + settings.getAlleyWidth() * 5, 5);
        relativeLayout.fit(fixedSize, 1);
        assertEquals("PartialLayout:5.000000in:[VBit{0-5@5.000000:[A(9.625×5.0)]}]", relativeLayout.toString());
    }

    @Test
    void fit() {
        FixedSize fixedSize = new FixedSizeInsert("A", 5, 5);
        relativeLayout.fit(fixedSize, 0);
        assertEquals("PartialLayout:5.000000in:[VBit{0-3@5.000000:[A(5.0×5.0)]}]", relativeLayout.toString());
    }

    @Test
    void fitVertically() {
        FixedSize a = new FixedSizeInsert("A", 5, 5);
        relativeLayout.fit(a, 0);
        FixedSize b = new FixedSizeInsert("B", 5, 5);
        relativeLayout.fit(b, 0);
        // we try to stair-step fixed articles, so the second one will be one column to the left:
        assertEquals("PartialLayout:10.125000in:[VBit{0-3@5.000000:[A(5.0×5.0)]}, VBit{1-4@5.000000:[B(5.0×5.0)]}]", relativeLayout.toString());
    }

    @Test
    void fitHorizontally() {
        double columnHeight = settings.getColumnHeight();
        FixedSize a = new FixedSizeInsert("A", 3, columnHeight);
        relativeLayout.fit(a, 0);
        FixedSize b = new FixedSizeInsert("B", 3, columnHeight);
        relativeLayout.fit(b, 1);
        // we try to stair-step fixed articles, so the second one will be one column to the left:
        assertEquals(String.format("PartialLayout:%fin:[VBit{0-3@%f:[B(3.0×%s), A(3.0×%s)]}]", columnHeight, columnHeight, columnHeight, columnHeight), relativeLayout.toString());
    }

    @Test
    void fallback_firstSectionInsufficientHeight() {
        double columnHeight = settings.getColumnHeight();
        FixedSize a = new FixedSizeInsert("A", 2, columnHeight - 1);
        relativeLayout.fit(a, 0);
        FixedSize b = new FixedSizeInsert("B", 2, 1); // to fall below A, and take up all free space
        relativeLayout.fit(b, 0);
        FixedSize c = new FixedSizeInsert("B", 3, columnHeight - 0.5);
        assertFalse(relativeLayout.fitByFirstFit(c, c.height(), c.cols()));
    }

    @Test
    void fallback_fitLeftOfExisting() {
        double columnHeight = settings.getColumnHeight();
        FixedSize a = new FixedSizeInsert("A", 2, columnHeight);
        relativeLayout.fit(a, 0);
        FixedSize b = new FixedSizeInsert("B", 1, columnHeight);
        assertTrue(relativeLayout.fitByFirstFit(b, b.height(), b.cols()));
    }

    @Test
    void fallback_rightOfExisting() {
        double columnHeight = settings.getColumnHeight();
        FixedSize a = new FixedSizeInsert("A", settings.getColumnWidth() * 5, columnHeight / 2);
        relativeLayout.fit(a, 0);
        FixedSize b = new FixedSizeInsert("B", settings.getColumnWidth() * 4, columnHeight / 3); // due to stair-stepping, goes left
        relativeLayout.fit(b, 0);
        FixedSize c = new FixedSizeInsert("B", settings.getColumnWidth(), columnHeight / 3);
        assertTrue(relativeLayout.fitByFirstFit(c, c.height(), c.cols()));
    }

    class FixedSizeInsert implements FixedSize {
        private final String identifier;
        double width;
        double height;

        public FixedSizeInsert(String identifier, double width, double height) {
            this.identifier = identifier;
            this.width = width;
            this.height = height;
        }

        @Override
        public long cols() {
            return Part.widthToNumberOfColumns(width, settings.getAlleyWidth(), settings.getColumnWidth());
        }

        @Override
        public Settings getSettings() {
            return new Settings(new Properties());
        }

        @Override
        public void copyToImpl(PrintWriter out, Path outPath) {

        }

        @Override
        public void setNumColumnsOnPage(long colsPerPage) {

        }

        @Override
        public Headers getHeaders() {
            Path path = MockPath.createMockPathWithName(identifier);
            return new Headers(path, new Properties(), settings);
        }

        @Override
        public double width() {
            return width;
        }

        @Override
        public double height() {
            return height;
        }

        @Override
        public Path path() {
            return null;
        }

        @Override
        public String toString() {
            return identifier + '(' + width + '×' + height + ')';
        }
    }

}