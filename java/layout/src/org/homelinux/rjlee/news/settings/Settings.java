package org.homelinux.rjlee.news.settings;

import org.homelinux.rjlee.news.CmdLineOptions;
import org.homelinux.rjlee.news.input.PreambleLinesSupplier;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.homelinux.rjlee.news.parsing.LengthParser.readLength;

/**
 * Manage the user-supplied settings that persist across a run.
 * <p>
 * The settings are loaded from files named {@code settings.properties}, which are Java properties files.
 * <p>
 * Each field name corresponds exactly with a key name in the properties file.
 * <p>
 * Additionally, the field {@code columnHeightRatioOfPage} has a value read as a floating-point number between 0.0
 * (exclusive) and 1.0 (inclusive), and is multiplied by the value of {@code pageHeight} to get the <strong>default</strong>
 * value of {@code columnHeight}. This allows {@code columnHeight} to be specified exactly, or as a percentage.
 *
 * @author Robert Lee <newspaper@rjlee.homelinux.org>
 */
public class Settings implements PreambleLinesSupplier {


    private enum Flag {
        allowTexFileOverwrite,
        inputWithoutCopy,
        defaultFontFamilyFromHeaders,
        enableLaTeXHooks
    }

    /**
     * Factory method to create the Settings object.
     *
     * @param cmdLineOptions          command-line options for the script
     * @param shutdownOnErrorFunction callback to report a fatal error response code; -1 for failed.
     * @return a settings object.
     */
    public static Settings build(CmdLineOptions cmdLineOptions, IntConsumer shutdownOnErrorFunction) {
        Settings settings = null;
        try {
            settings = new Settings(cmdLineOptions.getInputDirectories());
        } catch (IOException e) {
            System.err.println("Failed to load settings file " + e.getMessage() + " - details follow:");
            e.printStackTrace(System.err);
            shutdownOnErrorFunction.accept(-1);
        }
        return settings;
    }

    public enum ColumnStrategy {
        BALANCE,
        FILLFIRST
    }

    private Path[] srcDirs;

    private String[] inputFilters;


    private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class); // initialize to empty; all flags default false

    private double pageWidth;
    private double pageHeight;
    private double columnWidth;
    private double columnHeight;
    private double alleyWidth;
    private double alleyHeight;
    private double alleyThickWidth;
    private double alleyThickHeight;

    private int tolerance;
    private String emergencyStretch;
    private ColumnStrategy columnStrategy;

    private Path out;

    private String texInputs;
    private String latex;
    private String[] latexCmdLine;

    private String jobName;
    private String lengthsCache;

    private List<String> extraPreambleLines;
    private String markdown;
    private double minSideMargins;
    private String defaultFontEncoding;
    private String defaultFontFamily;
    private String defaultFontSeries;
    private int defaultFontSize;
    private String defaultFontSizeClo;
    private String defaultTeletypeFamily;
    private String defaultTeletypeSeries;
    private FontCommand headerFont;

    private String continuedOnPageText;
    private String continuedFromPageText;

    private DebugLevel stdOutLevel;
    private DebugLevel stdErrLevel;
    private DebugLevel logFileLevel;
    private Path logFile;

    private SemVer version;

    /**
     * Load properties files from the provided paths. Defaults are used if no file is found. If the same setting
     * appears in more than one file, the last-found setting overrides the first.
     *
     * @param srcDir One or more source directories.
     * @return A properties file containing settings from each file
     * @throws IOException if an error occurs reading the settings file.
     */
    private static Properties load(Path[] srcDir) throws IOException {
        Properties p = new Properties();
        for (Path src : srcDir) {
            Path settingsFile = src.resolve("settings.properties");
            try (BufferedReader reader = Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8)) {
                p.load(reader);
            } catch (NoSuchFileException ignored) {
                // if there is no settings file, just don't load it. We can use defaults.
            }
        }
        return p;
    }

    /**
     * Construct the settings object by loading the {@code settings.properties} file from the supplied source directories.
     * Directories not containing a {@code settings.properties} file will be skipped.
     *
     * @param srcDir source directories
     * @throws IOException if an error occurs reading one or more settings files.
     */
    Settings(final Path... srcDir) throws IOException {
        this(load(srcDir), srcDir);
    }

    /**
     * Load the settings from the given Properties
     *
     * @param properties to be loaded
     */
    public Settings(final Properties properties, Path... srcDirs) {
        this.srcDirs = srcDirs;
        final FileSystem fileSystem = FileSystems.getDefault();

        for (Flag flag : Flag.values()) {
            if (Boolean.parseBoolean(properties.getProperty(flag.name(), "false"))) {
                flags.add(flag);
            }
        }

        PageSizeCode defaultPageSize = readEnum(properties, "pageSize", PageSizeCode.class, PageSizeCode.BIG);

        this.version = readVersion(properties.getProperty("version"), SemVer.valueOf("0.0.1"));

        this.pageWidth = readLength(properties.getProperty("pageWidth", defaultPageSize.getWidth()));
        this.pageHeight = readLength(properties.getProperty("pageHeight", defaultPageSize.getHeight()));
        this.columnWidth = readLength(properties.getProperty("columnWidth", "1.5in"));
        // Set columnHeight to be an exact multiple of TeX points; 72.27 pts per inch
        // this avoids various problems when we can't introduce a rubber baselineskip:
	/*final double ppi = 1 / 72.27;
	  long columnHeightPt = Math.round((pageHeight * 0.9) / ppi);
	  this.columnHeight = columnHeightPt * ppi;
	*/
        double columnHeightRatio = Double.parseDouble(properties.getProperty("columnHeightRatioOfPage", "0.9")); // 90% is arbitrary, but it doesn't look wrong to me
        this.columnHeight = readLength(properties.getProperty("columnHeight", (columnHeightRatio * getPageHeight()) + "in"));
        this.alleyWidth = readLength(properties.getProperty("alleyWidth", "0.125in"));
        this.alleyHeight = readLength(properties.getProperty("alleyHeight", "0.125in"));
        this.alleyThickWidth = readLength(properties.getProperty("alleyThickWidth", "0.0125in"));
        this.alleyThickHeight = readLength(properties.getProperty("alleyThickHeight", "0.0125in"));
        this.minSideMargins = readLength(properties.getProperty("minSideMargins", "0.125in"));

        this.columnStrategy = readEnum(properties, "columnStrategy", ColumnStrategy.class, ColumnStrategy.BALANCE);

        // https://tex.stackexchange.com/questions/470976/are-there-cases-where-fontenc-luatex-or-xetex-cause-problems
        this.defaultFontEncoding = properties.getProperty("defaultFontEncoding", "TU").toUpperCase(); // may need to set to T1 for pdflatex / systems not based on lua or xetex
        this.defaultFontFamily = properties.getProperty("defaultFontFamily", "ptm").toLowerCase();
        this.defaultFontSeries = properties.getProperty("defaultFontSeries", "m").toLowerCase();
        this.defaultFontSize = readInt(properties, "defaultFontSize", 10, 2, Integer.MAX_VALUE - 2); // RL: I've heard of pixel fonts as small as 4px for minimal display, so leaving a very low value.
        this.defaultFontSizeClo = properties.getProperty("defaultFontSizeClo");
        this.defaultTeletypeFamily = properties.getProperty("defaultTeletypeFamily", "lmtt").toLowerCase();
        this.defaultTeletypeSeries = properties.getProperty("defaultTeletypeSeries", "lc").toLowerCase();

        this.tolerance = readInt(properties, "tolerance", 500, 0, 10000);
        this.emergencyStretch = properties.getProperty("emergencyStretch", "\\emergencystretch=0.1\\hsize");

        this.inputFilters = Arrays.stream(properties.getProperty("inputFilter", ".tex,.md,.txt,.text").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        this.out = fileSystem.getPath(properties.getProperty("out", "out"));
        this.texInputs = properties.getProperty("texinputs", ":");
        this.latex = properties.getProperty("latex", "pdflatex");
        this.latexCmdLine = properties.getProperty("latexCmdLine", "--interaction=nonstopmode").split("\\s+");
        this.jobName = properties.getProperty("jobName", "newspaper");
        this.lengthsCache = properties.getProperty("lengthsCache", "lengths.cache");
        this.logFile = fileSystem.getPath(properties.getProperty("logFile", "layout.log"));

        this.stdOutLevel = readEnum(properties, "stdOutLevel", DebugLevel.class, DebugLevel.ELEMENTS);
        this.stdErrLevel = readEnum(properties, "stdErrLevel", DebugLevel.class, DebugLevel.SILENT);
        this.logFileLevel = readEnum(properties, "logFileLevel", DebugLevel.class, DebugLevel.ALGORITHM);

        this.continuedOnPageText = properties.getProperty("continuedOnPageText", "\\makebox[\\textwidth]{\\hfill\\textit{\\scriptsize Continued on page \\otherpage\\dots\\hspace{-1em}}}");
        this.continuedFromPageText = properties.getProperty("continuedFromPageText", "\\makebox[\\textwidth]{\\textit{\\scriptsize\\hspace{-1em}\\dots continued from page \\otherpage}\\hfill}");

        // preamble has specific defaults:
        Properties preambleProperties = new Properties();
        preambleProperties.put("preamble!00head", "\\usepackage{indentfirst}");
        preambleProperties.put("preamble!01head", "\\usepackage[british]{babel}");
        preambleProperties.put("preamble!02head", "\\usepackage[utf8]{inputenc}");
        preambleProperties.put("preamble!03head", "\\usepackage{newtxmath,newtxtext}");// must come after {babel}
        preambleProperties.put("preamble!04head", "\\usepackage{csquotes}"); // make " an automatic double-quote character
        preambleProperties.putAll(properties); // there's a faster way, but this is *way* tidier

        this.markdown = preambleProperties.getProperty("markdown", "\\usepackage[smartEllipses,fancyLists]{markdown}");

        this.extraPreambleLines = preambleProperties.stringPropertyNames().stream()
                .filter(name -> name.toLowerCase().startsWith("preamble"))
                .sorted()
                .map(preambleProperties::getProperty)
                .collect(Collectors.toList());
        // NB: fontenc is repeatable the last one is set as default, so we should really defer this!
        // ... but we just set the \\fontencoding each time instead.
        extraPreambleLines.add("\\usepackage[" + defaultFontEncoding + "]{fontenc}");

        this.headerFont = new FontCommand(this, properties, "head", null, "\\rmdefault", "bc", "n", 18);
    }

    private SemVer readVersion(String version, SemVer defaultValue) {
        try {
            return Optional.ofNullable(version).filter(s -> !s.isEmpty()).map(SemVer::valueOf).orElse(defaultValue);
        } catch (NullPointerException | NumberFormatException e) {
            return defaultValue;
        }
    }

    static <E extends Enum<E>> E readEnum(Properties properties, String propertyName, Class<E> clazz, E defaultValue) {
        String strValue = properties.getProperty(propertyName, defaultValue.name());
        try {
            return Enum.valueOf(clazz, strValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Bad setting value [%s] for [%s]; expected one of %s",
                    strValue, propertyName,
                    EnumSet.allOf(clazz).stream()
                            .map(Enum::name)
                            .map(name -> "[" + name + "]")
                            .collect(Collectors.joining(";"))
            ), e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private int readInt(Properties properties, String name, int defaultValue, int minValue, int maxValue) {
        String strValue = properties.getProperty(name);
        if (strValue == null || strValue.trim().isEmpty()) return defaultValue;
        try {
            int rtn = Integer.parseInt(strValue);
            if (rtn < minValue || rtn > maxValue)
                throw new IllegalArgumentException(String.format("Bad setting value [%s] for [%s]; not in range %d-%d", strValue, name, minValue, maxValue));
            return rtn;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(String.format("Bad setting value [%s] for [%s]; not a valid Integer. Must be in range %d-%d", strValue, name, minValue, maxValue));
        }
    }

    /**
     * Size of the page
     */
    public double getPageWidth() {
        return pageWidth;
    }

    public double getPageHeight() {
        return pageHeight;
    }

    /**
     * Width of each column, excluding gutters/alleys
     */
    public double getColumnWidth() {
        return columnWidth;
    }

    /**
     * Height of the page without margins
     */
    public double getColumnHeight() {
        return columnHeight;
    }

    /**
     * Space between columns
     */
    public double getAlleyWidth() {
        return alleyWidth;
    }

    /**
     * Vertical Space between article fragmetns
     */
    public double getAlleyHeight() {
        return alleyHeight;
    }

    /**
     * Thickness of alley lines between columns
     * (0 for none; must be less than alleyWidth). Only affects {@code \halley} command.
     */
    public double getAlleyThickWidth() {
        return alleyThickWidth;
    }

    /**
     * Thickness of alley lines between rows
     * (0 for none; must be less than alleyHeight). Only affects {@code \valley} command.
     */
    public double getAlleyThickHeight() {
        return alleyThickHeight;
    }

    /**
     * Black arts time: the TeX primitive \hbadness affects the line-breaking algorithm.
     * A cube of the ratio of two lengths, or 10000 for lines that will never fit.
     * Higher numbers mean worse lines.
     */
    public int getTolerance() {
        return tolerance;
    }

    /**
     * Dark arts: The TeX primitive \emergencyStretch affects the line-breaking algorithm,
     * trading overfull lines for extra horizontal spacing. Newspapers usually want this quite high
     * but I'm trying a different tack.
     *
     * @return LaTeX code: definition of a function taking #cols (#1) and \hsize (#2) and setting \emergencyStretch
     */
    public String getEmergencyStretch() {
        return emergencyStretch;
    }

    public ColumnStrategy getColumnStrategy() {
        return columnStrategy;
    }


    public Path getOut() {
        return out;
    }

    public String getJobName() {
        return jobName;
    }

    /**
     * @return name of the file to use for caching lengths between runs
     */
    public String getLengthsCache() {
        return lengthsCache;
    }

    public String getLatex() {
        return latex;
    }

    public String[] getLatexCmdLine() {
        return latexCmdLine;
    }

    @Override
    public Stream<String> preambleLines() {
        return extraPreambleLines.stream();
    }

    public String getMarkdown() {
        return markdown;
    }

    public double getMinSideMargins() {
        return minSideMargins;
    }

    /**
     * @return the maximum number of columns that can fit on one page.
     */
    public long getMaxColsPerPage() {
        // (each column is columnWidth+alleyWidth, but for one extra alleyWidth)
        // casting to long ensures we always round down.
        return (long) ((getPageWidth() - getMinSideMargins() + getAlleyWidth()) / (getColumnWidth() + getAlleyWidth()));
    }

    public String getDefaultFontFamily() {
        return defaultFontFamily; // ptm = Adobe times
    }

    public String getDefaultFontSeries() {
        return defaultFontSeries;
    }

    public int getDefaultFontSize() {
        return defaultFontSize; // 10pt
    }

    public String getDefaultFontSizeClo() {
        return defaultFontSizeClo; // null
    }

    public String getDefaultFontEncoding() {
        return defaultFontEncoding; // T1 for pdflatex; TU for Lualatex
    }

    public String getDefaultTeletypeFamily() {
        return defaultTeletypeFamily;
    }

    public String getDefaultTeletypeSeries() {
        return defaultTeletypeSeries;
    }

    public FontCommand getHeaderFont() {
        return headerFont;
    }

    public boolean isAllowTexFileOverwrite() {
        return flags.contains(Flag.allowTexFileOverwrite);
    }

    public boolean isInputWithoutCopy() {
        return flags.contains(Flag.inputWithoutCopy);
    }

    public boolean isDefaultFontFamilyFromHeaders() {
        return flags.contains(Flag.defaultFontFamilyFromHeaders);
    }

    public boolean isEnableLateXHooks() {
        return flags.contains(Flag.enableLaTeXHooks);
    }

    public DebugLevel getStdOutLevel() {
        return stdOutLevel;
    }

    public DebugLevel getStdErrLevel() {
        return stdErrLevel;
    }

    public DebugLevel getLogFileLevel() {
        return logFileLevel;
    }

    public Path getLogFile() {
        return logFile;
    }

    public Path[] getSrcDirs() {
        return srcDirs;
    }


    public String[] getInputFilters() {
        return inputFilters;
    }

    public String getContinuedOnPageText() {
        return continuedOnPageText;
    }

    public String getContinuedFromPageText() {
        return continuedFromPageText;
    }

    public String getTexInputs() {
        return texInputs;
    }

    public SemVer getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "version=" + getVersion() +
                ", pageWidth=" + getPageWidth() +
                ", pageHeight=" + getPageHeight() +
                ", columnWidth=" + getColumnWidth() +
                ", columnHeight=" + getColumnHeight() +
                ", alleyWidth=" + getAlleyWidth() +
                ", alleyHeight=" + getAlleyHeight() +
                ", alleyThickWidth=" + getAlleyThickWidth() +
                ", alleyThickHeight=" + getAlleyThickHeight() +
                ", columnStrategy=" + getColumnStrategy() +
                ", minSideMargins=" + getMinSideMargins() +
                ", defaultFontEncoding=" + getDefaultFontEncoding() +
                ", defaultFontSize=" + getDefaultFontSize() +
                ", defaultFontSizeClo=" + getDefaultFontSizeClo() +
                ", defaultFontFamily=" + getDefaultFontFamily() +
                ", defaultFontSeries=" + getDefaultFontSeries() +
                ", defaultTeletypeFamily=" + getDefaultTeletypeFamily() +
                ", defaultTeletypeSeries=" + getDefaultTeletypeSeries() +
                ", tolerance=" + getTolerance() +
                ", emergencyStretch=" + getEmergencyStretch() +
                ", inputFilters=" + Arrays.toString(getInputFilters()) +
                ", out=" + getOut() +
                ", jobName=" + getJobName() +
                ", lengthsCache=" + getLengthsCache() +
                ", texinputs=" + getTexInputs() +
                ", latex='" + getLatex() + '\'' +
                ", latexCmdline=" + Arrays.toString(getLatexCmdLine()) +
                ", extraPreambleLines=" + extraPreambleLines +
                ", markdown=" + getMarkdown() +
                ", continuedOnPageText=" + getContinuedOnPageText() +
                ", continuedFromPageText=" + getContinuedFromPageText() +
                ", logFile=" + getLogFile() +
                ", stdOutLevel=" + getStdOutLevel() +
                ", stdErrLevel=" + getStdErrLevel() +
                ", logFileLevel=" + getLogFileLevel() +
                ", headerFont=" + getHeaderFont() +
                Arrays.stream(Flag.values()).map(e -> e.name() + "=" + flags.contains(e)).collect(Collectors.joining(", ", ", ", "}"));
    }
}
