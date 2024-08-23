package org.homelinux.rjlee.news.input;

import org.homelinux.rjlee.news.parsing.LengthParser;
import org.homelinux.rjlee.news.settings.FontCommand;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

/**
 * A special case of an insert, used for the titles.
 */
public class TitleInsert extends Insert {
    private final Headers headers;
    private final Settings settings;

    public TitleInsert(Headers headers, Settings settings) {
        super(headers, settings);
        this.headers = headers;
        this.settings = settings;
    }

    @Override
    public Long columnHint() {
        return null;
    }

    @Override
    public void copyToTex(Headers.InputMode inputMode, Settings settings, PrintWriter out, Path outPath) throws IOException {

        double width = width();
        String edition = headers.getHeader("Edition", "");
        String price = headers.getHeader("Price", "");
        String tagline = headers.getHeader("TagLine", "");
        String leftBox = getBoxHeader("LeftBox");
        String rightBox = getBoxHeader("RightBox");
        String date = headers.getHeader("Date", LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));
        double raiseLength = headers.getLengthHeader("RaiseLength", LengthParser.readLength("10pt"));
        double bottomLineDrop = headers.getLengthHeader("BottomLineDrop", 0.4);
        double raiseTitle = headers.getLengthHeader("RaiseTitle", 0);

        FontCommand titleFont = headers.getFontCommandFromHeaders("Font", "\\almendrafamily", "b", 54);
        FontCommand priceFont = headers.getFontCommandFromHeaders("Price", "\\rmdefault", "m", 8);
        FontCommand dateFont = headers.getFontCommandFromHeaders("Date", "\\rmdefault", "m", 8);
        FontCommand editionFont = headers.getFontCommandFromHeaders("Edition", "\\rmdefault", "m", 8);
        FontCommand tagLineFont = headers.getFontCommandFromHeaders("TagLine", "\\rmdefault", "m", 8);

        // put the title in a box; makes it easier to adjust later:
        out.printf("\\newbox\\titlebox\\setbox\\titlebox=\\hbox{%s%n", titleFont);
        super.copyToTex(inputMode, settings, out, outPath);
        out.println("}");

        out.printf("\\vbox{" +
                        "\\hbox to %fin{\\raisebox{%fin}[0pt][0pt]{%s %s}\\hfill \\raisebox{%fin}[0pt][0pt]{%s %s}}\n",
                width, raiseLength, dateFont, date, raiseLength, editionFont, edition);

        out.printf("\\hbox{\n" +
                        "\\makebox[%fin]{\n" +
                        "  %s" +
                        "  \\hfill\n" +
                        "  {%s\n",
                width, leftBox,
                titleFont);

        out.printf("\\raisebox{%fin}[\\ht\\titlebox][\\dp\\titlebox]{\\usebox\\titlebox}%n", raiseTitle);

        out.printf(
                "}\n" +
                        "  \\hfill\n" +
                        "  %s" +
                        "}\n" +
                        "  }\n" +
                        "  \\vspace{%fin}\n" +
                        "  \\hbox to %fin{\n" +
                        "    %s %s\\hfill{%s %s}\n" +
                        "}}\n", rightBox, bottomLineDrop, width,
                tagLineFont, tagline, priceFont, price);
    }

    private String getBoxHeader(String header) {
        double width = headers.getLengthHeader(header + "Width", 1);
        double height = headers.getLengthHeader(header + "Height", width);
        double offset = headers.getLengthHeader(header + "Raise", (height / 2) - 0.05);
        // FYI: It'd be okay set fontCmd="" if there were no font headers specified.
        int defaultFontSize = settings.getDefaultFontSize();
        FontCommand fontCmd = headers.getFontCommandFromHeaders("header", "\\rmdefault", settings.getDefaultFontSeries(), defaultFontSize);
        return Optional.ofNullable(headers.getHeader(header, ""))
                .filter(s -> !s.isEmpty())
                .map(s -> String.format("\\raisebox{0pt}[0pt][0pt]{\\framebox[%fin][c]{\\rule[%fin]{0pt}{%fin}%s{}%s}}", width, -offset, height, fontCmd, s))
                .orElse("");
    }
}
