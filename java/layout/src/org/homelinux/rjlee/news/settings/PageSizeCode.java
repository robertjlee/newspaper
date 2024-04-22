package org.homelinux.rjlee.news.settings;

public enum PageSizeCode {
    // ISO 216


    A2("410mm", "578mm"), // A2 is used for Australian & NZ broadsheets
    A3("297mm", "420mm"),
    A4("297mm", "210mm"),
    B2("500mm", "707mm"),
    B3("353mm", "500mm"),

    // ANSI
    LETTER("11in", "8.5in"),
    LEGAL("14in", "8.5in"),

    // US "ARCH" de-facto sizes
    A("9in", "12in"),
    B("12in", "18in"),
    C("18in", "24in"),
    D("24in", "36in"),
    E("36in", "48in"),
    E1("30in", "42in"),
    E3("27in", "39in"),

    // Approximate tabloid sizes
    CANADAT("260mm", "368mm"),
    NORWAYT("280mm", "400mm"),
    BRITAINT("280mm", "430mm"),

    // Berliner size common around Europe and in pockets elsewhere:
    MIDI("315mm", "470mm"),

    // Broadsheet sizes
    USA("381mm", "578mm"),
    NORWAY("400mm", "570mm"),
    BRITAIN("375mm", "597mm"),

    // Default size: I suspect this was originally intended as a double-truck:
    BIG("650mm", "750mm")
    ;

    private final String width;
    private final String height;

    PageSizeCode(String width, String height) {

        this.width = width;
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }
}
