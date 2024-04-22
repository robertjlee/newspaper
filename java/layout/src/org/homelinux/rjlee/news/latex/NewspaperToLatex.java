package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.LaidOut;

public interface NewspaperToLatex {
    void compileFinalPdf();

    void writeTexFile(LaidOut laidOut);

    default void handleFinalOutput(LaidOut laidOut) {
        writeTexFile(laidOut);
        // Finally, compile the PDF
        compileFinalPdf();
    }
}
