package org.homelinux.rjlee.news.layout;

/**
 * An algorithm class to calculate pulling an article away from its position on the column, based on available free space.
 * <p>
 * It's called a magnet because it pulls the article north (up) or south (down), but may not be very strong.
 *
 * @author Robert
 */
public class Magnet {
    private final double northAttraction;

    public Magnet(double northAttractionPercent, double southAttractionPercent) {
        if (southAttractionPercent != 0 && northAttractionPercent != 0 && southAttractionPercent != -northAttractionPercent)
            throw new IllegalArgumentException("Incompatible North and South attractions specified");

        if (northAttractionPercent != 0)
            this.northAttraction = northAttractionPercent / 100.;
        else
            this.northAttraction = 1 - (southAttractionPercent / 100.);
    }

    /**
     * @param totalSpace how much space the article can slide up and dow in
     * @return the amount of space to leave above the article.
     */
    public double calculateTopSpace(double totalSpace) {
        return (1. - northAttraction) * totalSpace;
    }
}
