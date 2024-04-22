package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.LaidOut;
import org.homelinux.rjlee.news.elements.ArticleFragment;
import org.homelinux.rjlee.news.elements.ArticleFragmentImpl;
import org.homelinux.rjlee.news.input.Article;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.rendered.Col;
import org.homelinux.rjlee.news.rendered.Page;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class NewspaperToLatexImpl extends LatexInteraction implements NewspaperToLatex {

    private final ShellProcessFactory latexProcessFactory;
    private Logger logger;

    public NewspaperToLatexImpl(Settings settings, Logger logger, ShellProcessFactory processFactory) {
        super(settings);

        this.logger = logger;
        latexProcessFactory = processFactory;
    }

    @Override
    public void compileFinalPdf() {
        // NB: We require a unicode engine for newtx, so we must use pdflatex, never latex.
//        Stream.Builder<String> cmdLine = Stream.builder();
//        cmdLine.add(getSettings().getLatex());
//        Arrays.stream(getSettings().getLatexCmdLine()).forEach(cmdLine::add);
//        cmdLine.add(getSettings().getJobName());
        try {
            Process proc = latexProcessFactory.run(getSettings(), getSettings().getOut(), getSettings().getJobName());
            // read lines from the process and log as needed etc
            Thread t = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    r.lines().forEach(logger.dumpAll()::println);
                } catch (UncheckedIOException | IOException e) {
                    PrintWriter quiet = logger.quiet();
                    quiet.println(getSettings().getJobName() + ".tex LaTeX call invoked, but there was an error reading the output.");
                    e.printStackTrace(quiet);
                }
            });
            t.start();
            // wait for the process to complete, then busy-wait for the thread calling the process to complete.
            // we won't be busy-waiting for very long, and only in the case of an error; after the process completes,
            // the thread just copies a few strings around in the error handler.
            while (t.isAlive()) {
                //noinspection BusyWait
                Thread.sleep(50);
                proc.waitFor(1, TimeUnit.HOURS);
            }
        } catch (IOException | InterruptedException e) {
            PrintWriter quiet = logger.quiet();
            quiet.println(getSettings().getJobName() + ".tex was generated, but the call to LaTeX failed.");
            e.printStackTrace(quiet);
        }

    }

    /**
     * Write the given newspaper layout to the output TeX file.
     * @param laidOut to be written
     */
    @Override
    public void writeTexFile(LaidOut laidOut) {
        try {
            PrintWriter out = logger.finalTexOutput();
            // Originally I was going to base this on "minimal", but this causes missing macro definitions with the markdown package.
            // So instead, we use the "article" class and simply redefine everything instead.
            out.println("\\documentclass{article}");
            // This may break things for some users, but it's the version I've been testing with, and old enough that
            // most LTS OS versions should have it:
            out.println("\\NeedsTeXFormat{LaTeX2e}[2020/02/02]"); // latex '\typeout{\fmtversion}\stop' | grep LaTeX2e
            out.printf("\\setlength{\\paperwidth}{%fin}\n", getSettings().getPageWidth());
            out.printf("\\setlength{\\paperheight}{%fin}\n", getSettings().getPageHeight());
            out.printf("\\usepackage[text={%fin,%fin},margin=0pt]{geometry}\n", getSettings().getPageWidth(), getSettings().getPageHeight());
            // pdftex ignores the geometry values and uses its own variables for paper size:
            out.printf("\\ifcsname pdfpagewidth\\endcsname\\pdfpagewidth=%fin\\fi\n", getSettings().getPageWidth());
            out.printf("\\ifcsname pdfpageheight\\endcsname\\pdfpageheight=%fin\\fi\n", getSettings().getPageHeight());
            out.println("\\usepackage[british]{babel}");
            out.printf("\\setlength{\\textwidth}{%fin}\n", getSettings().getPageWidth());
            out.printf("\\setlength{\\textheight}{%fin}\n", getSettings().getPageHeight());
            out.printf("\\newlength{\\columnheight}");
            out.printf("\\setlength{\\columnwidth}{%fin}\n", getSettings().getColumnWidth());
            out.printf("\\setlength{\\columnheight}{%fin}\n", getSettings().getColumnHeight());
            // NB: halley goes between columns (horizontal layout)
            // and valley goes above/below fragments (vertical layout)
            out.printf("\\newcommand{\\halleyline}[1]{\\hbox{\\makebox[%fin]{\\rule{%fin}{#1}}}\\vskip 0pt}\n", getSettings().getAlleyWidth(), getSettings().getAlleyThickWidth());
            //	    w.printf("\\newcommand{\\halleygap}[1]{\\hbox{\\makebox[0pt]{\\rule{0pt}{#1}}}}\n");
            out.printf("\\newcommand{\\halleygap}[1]{\\vspace*{#1}}\n");

            if (getSettings().getAlleyWidth() <= 0) {
                out.printf("\\newcommand{\\valley}[1][%fin]{\\vspace*{%fin}}\n", getSettings().getColumnWidth(), getSettings().getAlleyHeight());
            } else {
                double vAlleyGap = (getSettings().getAlleyHeight() - getSettings().getAlleyThickHeight()) / 2;
                out.printf("\\newcommand{\\valley}[1][%fin]{\\vspace*{%fin}\\rule{#1}{%fin}\\vspace*{%fin}}\n", getSettings().getColumnWidth(), vAlleyGap, getSettings().getAlleyThickHeight(), vAlleyGap);
            }
            out.println("\\makeatletter");
            printAtPreamble(out);
            out.println("\\makeatother");
            getSettings().preambleLines().forEach(out::println);
            laidOut.preambleLines().forEach(out::println);
            out.println();
            out.println("\\setlength{\\hfuzz}{\\maxdimen}");
            out.println("\\setlength{\\vfuzz}{\\maxdimen}");
            // TODO: Dup cade in LatexLength
            // tolerance & emergencystretch based loosely on multicols:
            out.printf("\\newcount\\nmulticoltolerance \\nmulticoltolerance=%d\n", getSettings().getTolerance());
            out.printf("\\def\\setemergencystretch#1#2{%s}\n", getSettings().getEmergencyStretch());
            out.printf("\\def\\numnewscols{%s}\n", getSettings().getMaxColsPerPage());
            out.println("\\begin{document}");
            // for now, just copying tolerance from multicols:
            out.println("\\makeatletter\\vbadness\\@Mi \\hbadness5000 \\tolerance\\nmulticoltolerance\\makeatother");
            List<Page> allPages = laidOut.getPages();
            // step 1: create boxes for each article
            Stream<Article> allArticles = allPages.stream()
                    .flatMap(pa -> pa.getColumns().stream())
                    .flatMap(co -> co.getFrags().stream())
                    .map(Col.ColFragment::getPart)
                    .filter(pa -> pa instanceof ArticleFragmentImpl)
                    .map(a -> ((ArticleFragment) a).getArticle())
                    .distinct();
            Iterator<Article> ia = allArticles.iterator();
            while (ia.hasNext()) {
                Article a = ia.next();
                String name = a.name();
                out.printf("\\begin{newsplitbox}{%s}\n", name);
                out.printf("\\hsize=%fin\n", getSettings().getColumnWidth());
                out.printf("\\textwidth=%fin\n", getSettings().getColumnWidth());
                out.printf("\\linewidth=%fin\n", getSettings().getColumnWidth());
                out.printf("\\columnwidth=%fin\n", getSettings().getColumnWidth());
                a.copyTo(out, getSettings().getOut());
                out.println("\\end{newsplitbox}");
            }

            // step 2: output each page
            for (int i = 0; i < allPages.size(); ) {
                out.printf("%% page %d\n", i + 1);
                out.println("\\hbox{}\\vfil");
                allPages.get(i).write(out, getSettings().getOut());
                if (++i < allPages.size())
                    //		    w.println("\\eject");
                    //w.println("\\\\\\hbox{}\\vfill");
                    out.println("\\pagebreak");
            }
            //	    if (overflow != null) {
            //		w.println("Overflow!");
            //	    }
            out.println("\\end{document}");
            if (out.checkError()) {
                throw new IOException("Error writing to " + getSettings().getJobName() + ".tex!");
            }
            logger.algorithm().println("Written output file ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
