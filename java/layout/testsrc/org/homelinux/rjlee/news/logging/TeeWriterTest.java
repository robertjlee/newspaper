package org.homelinux.rjlee.news.logging;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TeeWriterTest {

    @Test
    void testWriter() throws IOException {
        try (StringWriter a = new StringWriter();
             Writer wa = new BufferedWriter(a);
             StringWriter b = new StringWriter();
             Writer wb = new BufferedWriter(b);
             TeeWriter tw = new TeeWriter(wa, wb);
             PrintWriter pw = new PrintWriter(tw)) {
            pw.println("Test!");

            pw.flush();
            assertAll(
                    () -> assertEquals("Test!\n", a.toString()),
                    () -> assertEquals("Test!\n", b.toString())
            );
        }

    }


}