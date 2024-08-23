package org.homelinux.rjlee.news;

import org.homelinux.rjlee.news.latex.FileCache;
import org.homelinux.rjlee.news.latex.LatexProcessFactory;
import org.homelinux.rjlee.news.latex.NewspaperToLatexImpl;
import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

/**
 * Main method to invoke the newspaper layout algorithm.
 * <p>
 * See {@link NewspaperLayoutImpl} for further details, or consult the comprehensive manual that
 * you should have received with this distribution.
 *
 * @author Robert Lee
 */
public class Layout implements Runnable{

    private String[] cmdLine;
    private Logger logger;

    public Layout(String... cmdLine) {
        this(Logger.getInstance(), cmdLine);
    }

    public Layout(Logger logger, String... cmdLine) {
        this.logger = logger;
        this.cmdLine = cmdLine;
    }


    /**
     * Entrypoint; process command-line options.
     */
    public static void main(final String[] args) {
        new Layout(args).run();
    }

    public void run() {
        System.out.println("Newspaper layout algorithm by Robert Lee. Version 1.0.1.");

        CmdLineOptions cmdLineOptions = new CmdLineOptions(System.out, cmdLine);
        Settings settings = Settings.build(cmdLineOptions, System::exit);
        logger.configure(settings, System.out, System.err);

        FileCache.getInstance().init(settings);
        NewspaperLayout layout = createEmptyLayout(settings, cmdLineOptions);

        layout.layOutNewspaper();

        layout.validate();

        // Now output some kind of file readable by tex
        NewspaperToLatexImpl newspaperToLatex = new NewspaperToLatexImpl(settings, Logger.getInstance(), new LatexProcessFactory());
        newspaperToLatex.handleFinalOutput(layout);

        FileCache.getInstance().save();

        // Flush any remaining logs
        logger.close();
    }

    protected NewspaperLayout createEmptyLayout(Settings settings, CmdLineOptions cmdLineOptions) {
        return new NewspaperLayoutImpl(settings, Logger.getInstance(), cmdLineOptions.getInputDirectories());
    }

    public String[] getCmdLine() {
        return cmdLine;
    }
}
