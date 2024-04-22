package org.homelinux.rjlee.news.settings;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class FontCommand {
    private Properties properties;

    private String prefix;
    private Path inputFilePath;
    private String defFamily;
    /**
     * Series can be anything defined in the .fd file for the encoding/family pair.
     */
    private String defSeries;
    /**
     * Series can be anything defined in the .fd file for the encoding/family pair.
     */
    private String defShape;
    private int defFontSize;
    private String lazyFontCommand;
    private String defaultEncoding;

    public FontCommand(Settings settings, Properties properties,
                       String prefix, Path inputFilePathOrNullForSettings,
                       String defFamily, String defSeries, String defShape, int defFontSize) {
        this.defaultEncoding = settings.getDefaultFontEncoding();
        this.properties = properties;
        this.prefix = prefix;
        this.inputFilePath = inputFilePathOrNullForSettings;
        this.defFamily = defFamily;
        this.defSeries = defSeries;
        this.defShape = defShape;
        this.defFontSize = defFontSize;
    }

    public String getFontCommand() {
        if (lazyFontCommand == null) {
            String defTitleFont = getDefaultFontCommand();
            lazyFontCommand = properties.getProperty(prefix + "Command", defTitleFont);
        }
        return lazyFontCommand;
    }

    public String getDefaultFontCommand() {
        String encoding = getEncoding();
        String family = getFamily();
        String series = getSeries();
        String fontShape = getShape();
        int fontSize = getSize();
        int fontSpacing = getSpacing();
        return String.format("\\fontencoding{%s}\\fontfamily{%s}\\fontseries{%s}\\fontshape{%s}\\fontsize{%s}{%s}\\selectfont",
                encoding, family, series, fontShape, fontSize, fontSpacing);
    }

    public String getEncoding() {
        return properties.getProperty(prefix + "Encoding", defaultEncoding);};

    public String getFamily() {
        return properties.getProperty(prefix + "Family", defFamily);
    }

    public String getSeries() {
        return properties.getProperty(prefix + "Series", defSeries);
    }

    public String getShape() {
        return properties.getProperty(prefix + "Shape", defShape);
    }

    public int getSize() {
        return getInteger(prefix + "Size", 0, Integer.MAX_VALUE - 2, defFontSize);
    }

    public int getSpacing() {
        return getInteger(prefix + "Spacing", 1, Integer.MAX_VALUE, getSize() + 2);
    }

    private int getInteger(String name, int minValue, int maxValue, int defaultValue) {
        String strValue = properties.getProperty(name);
        if (strValue == null) return defaultValue;
        String prefix = Optional.ofNullable(inputFilePath).map(s -> s + ": ").orElse("");
        String type = inputFilePath == null ? "setting" : "header";
        try {
            int value = Integer.parseInt(strValue);
            if (value < minValue || value > maxValue)
                throw new IllegalArgumentException(String.format("%sBad %s value [%s] for [%s]; not in range %d-%d", prefix, type, strValue, name, minValue, maxValue));
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%sBad %s value [%s] for [%s]; not a valid Integer. Must be in range %d-%d", prefix, type, strValue, name, minValue, maxValue));
        }
    }

    @Override
    public String toString() {
        return getFontCommand();
    }
}
