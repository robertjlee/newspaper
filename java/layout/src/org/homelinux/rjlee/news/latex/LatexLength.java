package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.file.TmpFileUtils;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.logging.TeeWriter;
import org.homelinux.rjlee.news.parsing.LengthParser;
import org.homelinux.rjlee.news.settings.Settings;
import org.homelinux.rjlee.news.stream.FakePipedReader;
import org.homelinux.rjlee.news.stream.FakePipedWriter;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * typeset something, and return a length. Single use.
 */
public class LatexLength extends LatexInteraction {
    private final Stream<String> preambleLines;
    private final FakePipedReader pr = new FakePipedReader();
    private final double width;
    private final List<Double> fragments;
    private final ShellProcessFactory latexProcessFactory;
    private final Logger logger;
    private Path tmpDir;
    private Path systemTmpDir = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"));

    public LatexLength(double width, List<Double> fragments, Stream<String> preambleLines, Settings settings, ShellProcessFactory latexProcessFactory, Logger logger) {
        super(settings);
        this.preambleLines = preambleLines;
        if (width <= 0) throw new IllegalArgumentException("Width must be positive");
        this.width = width;
        this.fragments = fragments;
        this.latexProcessFactory = latexProcessFactory;
        this.logger = logger;
    }

    public double getWidth() {
        return width;
    }

    public List<Double> getFragments() {
        return fragments;
    }

    public ShellProcessFactory getLatexProcessFactory() {
        return latexProcessFactory;
    }

    public PrintWriter writer() throws IOException {
        return new PrintWriter(new BufferedWriter(new FakePipedWriter(pr)));
    }

    public double calculate() {
        StringWriter sb = new StringWriter();
        StringBuilder readLength = new StringBuilder();
        StringBuilder readDepth = new StringBuilder();
        Process p = null;
        Path tmpDir = getTmpDir();
        try {
            writeTempFile(tmpDir);
            p = runLaTeXProcess(tmpDir, readLength, readDepth, sb);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(new PrintWriter(sb, true));
        } finally {
            if (tmpDir != null) TmpFileUtils.recursiveDeleteOnExit(tmpDir);
        }
        if (p == null) {
            logger.quiet().println("  Process not run!");
            return 0;
        }
        if (readLength.length() == 0 || readDepth.length() == 0) {
            logger.quiet().println(sb);
            logger.dumpAll().println("  Process completed without length: " + p.exitValue());
            throw new RuntimeException("Failed to calculate article length: Process completed without producing length: " + p.exitValue());
        }
        if (p.exitValue() != 0) {
            logger.quiet().println("  Process completed: " + p.exitValue());
        }
        double length = LengthParser.readLength(readLength.toString(), true) + fragments.stream().mapToDouble(d -> d).sum()
                + LengthParser.readLength(readDepth.toString(), true);
        logger.dumpAll().println("  Calculated length: " + readLength + "=>" + length + "in");
        return length;
    }

    /**
     * @param tmpDir     temporary directory containing art.tex
     * @param readLength buffer to hold returned length of box
     * @param readDepth  buffer to hold returned depth of box
     * @param sb         buffer to hold LaTeX content, for logging purposes
     * @return process definition, after completion (with 1 hour timeout)
     * @throws IOException          error communicating with the process
     * @throws InterruptedException interrupted before process completed
     */
    private Process runLaTeXProcess(Path tmpDir, StringBuilder readLength, StringBuilder readDepth, StringWriter sb) throws IOException, InterruptedException {
        Process process = latexProcessFactory.run(getSettings(), tmpDir, "art");

        // read lines from the process and parse out the length.
        if (process != null) {
            Thread t = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    r.lines()
                            .peek(logger.dumpAll()::println)
                            .peek(l -> extract(l, "ART HEIGHT", readLength))
                            .peek(l -> extract(l, "ART DEPTH", readDepth))
                            .map(l -> " >" + l + "<\n").forEach(sb::append);
                } catch (IOException | UncheckedIOException e) {
                    e.printStackTrace(new PrintWriter(sb, true));
                }
            });
            t.start();
            process.waitFor(1, java.util.concurrent.TimeUnit.HOURS);
        }
        return process;
    }

    private void writeTempFile(Path tmpDir) throws IOException {
        pr.close(); // output is complete by this point.
        // create a blank aux file to suppress the warning:
        Path artAux = tmpDir.resolve("art.aux");
        //noinspection EmptyTryBlock
        try (Writer ignored = Files.newBufferedWriter(artAux, StandardOpenOption.CREATE)) {
        }
        Path artTex = tmpDir.resolve("art.tex");
        try (BufferedWriter bw = Files.newBufferedWriter(artTex, StandardOpenOption.CREATE_NEW);
             Writer w = new TeeWriter(bw, logger.dumpAll());
             PrintWriter out = new PrintWriter(w)) {
            writeTo(out);
        }
    }

    private Path getTmpDir() {
        if (tmpDir == null) {
            Path tmpDir;
            tmpDir = getTmpDirImpl(systemTmpDir, logger);
            this.tmpDir = tmpDir;
            logger.dumpAll().println("Temp dir " + tmpDir);
        }
        return tmpDir;
    }

    static Path getTmpDirImpl(Path systemTmpDir, Logger logger) {
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory(systemTmpDir, "art");
        } catch (IOException e) {
            e.printStackTrace(logger.quiet());
            throw new RuntimeException(e);
        }
        return tmpDir;
    }

    private void writeTo(PrintWriter out) throws IOException {
        try (BufferedReader in = new BufferedReader(pr)) {
            out.println("\\documentclass{article}");
            out.println("\\usepackage[british]{babel}");

            getSettings().preambleLines().forEach(out::println);
            preambleLines.forEach(out::println);

            out.println("\\makeatletter");
            printAtPreamble(out);
            // create a box to hold the page output:
            out.println("\\newbox\\sb@junkbox");
            out.println("\\newbox\\sb@junkbox@");

            // TODO: Dup cade in NewspaperToLatexImpl
            // tolerance & emergencystretch based loosely on multicols:
            out.printf("\\newcount\\nmulticoltolerance \\nmulticoltolerance=%d\n", getSettings().getTolerance());
            out.printf("\\def\\setemergencystretch#1#2{%s}\n", getSettings().getEmergencyStretch());
            out.printf("\\def\\numnewscols{%s}\n", getSettings().getMaxColsPerPage());

            // create an output routine to discard data but extract the height of the column:
            // start the document
            out.println("\\begin{document}");
            // for now, just copying tolerance from multicols:
            out.println("\\vbadness\\@Mi \\hbadness5000 \\tolerance\\nmulticoltolerance");

            out.printf("\\hsize=%fin\\linewidth=%fin\\columnwidth=%fin\\textwidth=%fin\\vsize=\\maxdimen", width, width, width, width); // maxdimen=16383pt?
            // Suppress overfull hbox warnings while working out the page size:
            out.println("\\setlength{\\hfuzz}{\\maxdimen}");
            out.println("\\setlength{\\vfuzz}{\\maxdimen}");
            out.println("\\setbox\\sb@junkbox=\\vbox{");
            out.println("\\makeatother");
            out.println("\\setemergencystretch\\numnewscols\\hsize");
            in.lines().forEach(out::println);
            out.println("\\dumpfootnotes}\\makeatletter");
            out.println("\\eject");// force the output routine to run
            for (Double toChop : fragments) {
                // split the box, trapping the removed contents into a new box to avoid producing dvi output:
                out.printf("\\expandafter\\setbox\\sb@junkbox@=\\vbox{\\vsplit\\sb@junkbox to %fin}%%\n", toChop);
            }
            out.println("\\typeout{ART HEIGHT:\\the\\ht\\sb@junkbox}");// to be captured
            out.println("\\typeout{ART DEPTH:\\the\\dp\\sb@junkbox}");// to be captured
            out.println("\\end{document}");
        }
    }

    public Path outPath() {
        return getTmpDir();
    }
}
