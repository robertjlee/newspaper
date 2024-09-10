package org.homelinux.rjlee.news.settings;

import java.util.Arrays;
import java.util.Objects;

public class SemVer implements Comparable<SemVer> {
    private final int[] parts;

    public SemVer(String from) {
        String[] bits = from.split("\\.", 3);
        int[] parts = Arrays.stream(bits).mapToInt(Integer::parseInt).toArray();
        this.parts = Arrays.copyOf(parts, 3);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", parts[0], parts[1], parts[2]);
    }

    @Override
    public int compareTo(SemVer semVer) {
        int rtn = Integer.compare(parts[0], semVer.parts[0]);
        if (rtn == 0) {
            rtn = Integer.compare(parts[1], semVer.parts[1]);
        }
        if (rtn == 0) {
            rtn = Integer.compare(parts[2], semVer.parts[2]);
        }
        return rtn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemVer semVer = (SemVer) o;
        return Objects.deepEquals(parts, semVer.parts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }

    public static SemVer valueOf(String value) {
        return new SemVer(value);
    }
}
