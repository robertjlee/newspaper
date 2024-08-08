package org.homelinux.rjlee.news.parsing;

public interface LengthParser {

    double PT_PER_IN = 0.013837;

    static double readLength(final String str) {
        return readLength(str, false);
    }
    static double readLength(final String str, boolean negativeMeansOverflow) {
        if (str == null) throw new RuntimeException("Length required, but none was supplied");
        String s = str.trim().toLowerCase();
        if ("fill".equalsIgnoreCase(s)) return Double.MAX_VALUE;
        if (s.startsWith("0") && s.replaceAll("0", "").isEmpty()) return 0;
        try {
            if (s.endsWith("in")) return Double.parseDouble(s.substring(0, s.length() - 2));
            if (s.endsWith("inch")) return Double.parseDouble(s.substring(0, s.length() - 4));
            if (s.endsWith("inches")) return Double.parseDouble(s.substring(0, s.length() - 6));
            // TeX points are helpfully defined as 0.35145980 mm ≈ 0.13837 in
            if (s.endsWith("pt")) return parsePt(s, 2, negativeMeansOverflow);
            if (s.endsWith("pts")) return parsePt(s, 3, negativeMeansOverflow);
            // millimeter lengths are exact since the adoption of the International Yard.
            if (s.endsWith("mm")) return Double.parseDouble(s.substring(0, s.length() - 2)) / 25.4;
            if (s.endsWith("cm")) return Double.parseDouble(s.substring(0, s.length() - 2)) / 02.54;
            if (s.endsWith("dm")) return Double.parseDouble(s.substring(0, s.length() - 2)) / 0.254;
            if (s.endsWith("m")) return Double.parseDouble(s.substring(0, s.length() - 1)) / 0.0254;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parse length [s]; try mm, inches or TeX pts. Message is: " + e.getMessage(), e);
        }
        throw new RuntimeException("Not implemented to read length [" + s + "]; try mm, inches or TeX points?");
    }

    static double parsePt(String s, int cropEnd, boolean negativeMeansOverflow) {
        double pts = Double.parseDouble(s.substring(0, s.length() - cropEnd));
        // LaTeX overflows 16384 points when going over about 5m of text; this hack
        // allows us to support longer article lengths.
        double len = negativeMeansOverflow && pts < 0 ? 16384 - pts : pts;
        return len * PT_PER_IN;
    }
}
