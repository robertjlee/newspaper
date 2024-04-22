package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.latex.NewspaperToLatex;

import java.util.ArrayList;
import java.util.List;

public class MockNewspaperToLatex implements NewspaperToLatex {
    private List<String> methodCallOrder = new ArrayList<>();
    @Override
    public void compileFinalPdf() {
        methodCallOrder.add("compileFinalPdf");
    }

    @Override
    public void writeTexFile(LaidOut laidOut) {
        methodCallOrder.add("writeTexFile");
    }

    public List<String> getMethodCallOrder() {
        return methodCallOrder;
    }
}
