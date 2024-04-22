package org.homelinux.rjlee.news.settings;

public enum DebugLevel implements Comparable<DebugLevel> {
    /**
     * Absolutely no output, except on stderr when aborting in error.
     */
    SILENT,
    /**
     * Output minimal summary information only
     */
    QUIET,
    /**
     * Output stats on elements inserted, eg column-inches per article
     */
    ELEMENTS,
    /**
     * Tracing for the layout algorithm: which elements are added per page
     */
    ALGORITHM,
    /**
     * Output all TeX files, along with other information.
     */
    DUMP_ALL
}
