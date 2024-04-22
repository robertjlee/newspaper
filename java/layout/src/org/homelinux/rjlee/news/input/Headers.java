package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.parsing.LengthParser;
import org.homelinux.rjlee.news.settings.FontCommand;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Headers are settings that are defined per input.
 * <p>
 * These are much looser than the global settings defined by {@link Settings},
 * as they depend entirely on which type of header is being used, and often on other headers,
 * so instead of preloading all possible settings, we expect callers to supply the individual header names per use.
 */
public class Headers implements PreambleLinesSupplier {


    public enum InputType {
        /**
         * An input-file that should be skipped
         */
        SKIP,
        /**
         * A paste-up article
         */
        ARTICLE,
        /**
         * A head-spanning article
         */
        HEAD_SPAN,
        /**
         * A fixed-size insert
         */
        INSERT;
    }

    public enum InputMode {
        /**
         * Use an {@code \input{}} to include the file, or copy it directly
         */
        LATEX,
        /**
         * Setting catcodes to treat punctuation characters as plain text; this is roughly plain-text format.
         */
        PLAIN,
        /**
         * Use {@code \markdownInput{}} or a {@code markdown} environment, e.g. from the markdown package,
         * to include the file as Markdown.
         */
        MARKDOWN
    }

    private final Path inputFilePath;

    private Properties headers;
    private final Settings settings;

    public Headers(Path inputFilePath, BufferedReader r, Settings settings) throws IOException {
        this(inputFilePath, readHeaders(r), settings);
    }

    static Properties readHeaders(BufferedReader r) throws IOException {
        String header = r.lines().limit(50)
                .filter(l -> l.startsWith("%#"))
                .map(s -> s.substring(2))
                .collect(Collectors.joining("\n"));

        Properties headers = new Properties();
        headers.load(new StringReader(header));
        return headers;
    }

    public Headers(Path inputFilePath, Properties headers, Settings settings) {

        this.inputFilePath = inputFilePath;
        this.headers = headers;
        this.settings = settings;

        // debug flags
        PrintWriter dumpAll = Logger.getInstance().dumpAll();
        headers.forEach((k, v) -> dumpAll.println("   " + k + " -> " + v));
    }

    @Override
    public Stream<String> preambleLines() {
        return headers.stringPropertyNames().stream()
                .filter(h -> h.startsWith("Preamble"))
                .sorted()
                .map(headers::getProperty);
    }

    public String getHeader(String s, String s1) {
        return headers.getProperty(s, s1);
    }

    /**
     * @param key           name of a header key (e.g. {@code head})
     * @param valueConsumer lambda performed on the value of header {@code key}, if it is present and contains character data
     */
    public void ifHeader(String key, Consumer<String> valueConsumer) {
        Optional.of(headers.getProperty(key, ""))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .ifPresent(valueConsumer);
    }

    public boolean isAnyHeaderSet(String... names) {
        return Stream.of(names)
                .map(key -> headers.getProperty(key, ""))
                .anyMatch(value -> !value.isEmpty());
    }


    public InputType getInputType() {
        return Optional.ofNullable(headers.getProperty("Type"))
                .map(String::toUpperCase)
                .map(str -> {
                    switch (str) {
                        case "ARTICLE":
                            return InputType.ARTICLE;
                        case "HEADSPAN":
                            return InputType.HEAD_SPAN;
                        case "INSERT":
                            return InputType.INSERT;
                        default:
                            return null;
                    }
                }).orElse(InputType.SKIP);
    }

    public InputMode getInputMode() {
        return Optional.ofNullable(headers.getProperty("Mode"))
                .map(String::toUpperCase)
                .map(String::trim)
                .map(name -> {
                    try {
                        return InputMode.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .orElse(InputMode.LATEX);
    }

    @SuppressWarnings("SameParameterValue")
    public long getIntegerHeader(String name, long minValue, long maxValue) {
        String strValue = headers.getProperty(name);
        if (strValue == null)
            throw new IllegalArgumentException(String.format("%s: Missing header value for [%s]; must be in range %d-%d", inputFilePath, name, minValue, maxValue));
        try {
            long value = Long.parseLong(strValue);
            if (value < minValue || value > maxValue)
                throw new IllegalArgumentException(String.format("%s: Bad header value [%s] for [%s]; not in range %d-%d", inputFilePath, strValue, name, minValue, maxValue));
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%s: Bad header value [%s] for [%s]; not a valid Integer. Must be in range %d-%d", inputFilePath, strValue, name, minValue, maxValue));
        }
    }

    public long getIntegerHeader(String name, long minValue, long maxValue, long defValue) {
        if (headers.getProperty(name) == null) return defValue;
        return getIntegerHeader(name, minValue, maxValue);
    }

    FontCommand getFontCommandFromHeaders(String prefix, String defFamily, String defSeries, int defFontSize) {
        return new FontCommand(settings, headers, prefix, inputFilePath, defFamily, defSeries, "n", defFontSize);
    }



    public double getLengthHeader(String name, double defaultValue) {
        String strValue = headers.getProperty(name, defaultValue + "in");
        try {
            return LengthParser.readLength(strValue);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(String.format("%s: Bad header value [%s] for length [%s]; %s", inputFilePath, name, strValue, e.getMessage()), e);
        }
    }

    public Path getInputFilePath() {
        return inputFilePath;
    }

    public Stream<Path> assets() {
        Path dir = inputFilePath.getParent();
        return headers.stringPropertyNames().stream()
                .filter(n -> n.startsWith("Asset"))
                .map(s -> headers.getProperty(s))
                .map(dir::resolve)
                .filter(p -> p.toFile().exists());
    }

}
