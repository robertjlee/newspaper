package org.homelinux.rjlee.news.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

class PeekWriterTest {

    public static final String THIS_IS_A_TEST = "This is a test!";
    private StringWriter normalOutput = new StringWriter();
    private BufferedWriter normalWriter = new BufferedWriter(normalOutput);
    private final ByteArrayOutputStream copyOutput = new ByteArrayOutputStream();
    private OutputStream copyOutputStream = new BufferedOutputStream(copyOutput);
    private final PeekWriter pw = new PeekWriter(normalWriter, copyOutputStream);

    @Test
    void poke() throws IOException {
        try (PrintWriter out = new PrintWriter(pw)) {
            out.println(THIS_IS_A_TEST);
            pw.flush();
        }
        Assertions.assertAll(
                () -> Assertions.assertEquals(THIS_IS_A_TEST + "\n", normalOutput.toString()),
                () -> Assertions.assertEquals(THIS_IS_A_TEST + "\n", copyOutput.toString())
        );
    }
}