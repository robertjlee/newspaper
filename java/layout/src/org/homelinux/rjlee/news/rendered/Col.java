package org.homelinux.rjlee.news.rendered;

import org.homelinux.rjlee.news.elements.Part;
import org.homelinux.rjlee.news.elements.Valley;
import org.homelinux.rjlee.news.settings.Settings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * An individual column on a page. Each column is divided into a number of "fragments" {@see ColFragment},
 * which can be empty (space yet to be allocated), or contain a Part.
 * <p>
 * A part may appear in more than one column, such as a column-spanning header.
 *
 * @author Robert
 */
public class Col {

    /**
     * A part of a column representing a fixed-place object, or fixed empty space
     */
    public class ColFragment {
        private final Long continuedFrom; // null usually, otherwise page continued from
        private double start;
        private double end;
        private Part part;

        /**
         * Construct an empty fragment for a whole column
         */
        ColFragment() {
            start = 0;
            end = settings.getColumnHeight();
            part = null;
            continuedFrom = null;
        }

        /**
         * A partial empty fragment
         */
        ColFragment(double start, double end) {
            this.start = start;
            this.end = end;
            if (start >= end) throw new IllegalArgumentException("0-height column!" + start + "-" + end + " in");
            continuedFrom = null;
        }

        /**
         * A filled fragment
         */
        public ColFragment(Part i, double start, double end, Long continuedFrom) {
            part = i;
            this.start = start;
            this.end = end;
            this.continuedFrom = continuedFrom;
            if (start >= end) throw new IllegalArgumentException("0-height column! " + start + "-" + end + " in");
        }

        /**
         * A filled fragment
         */
        public ColFragment(Part i, double start) {
            part = i;
            this.start = start;
            this.end = start + i.height();
            if (start >= end) throw new IllegalArgumentException("0-height column!" + start + "-" + end + " in");
            continuedFrom = null;
        }

        public double start() {
            return start;
        }

        public double end() {
            return end;
        }

        public Part part() {
            return part;
        }



        public String toString() {
            return (part == null ? "Fragment empty" : "Fragment for part " + part) +
                    "@[" + getStart() + "-" + getEnd() + "]";
        }

        public double height() {
            return end() - start();
        }

        public double getStart() {
            return start;
        }


        public double getEnd() {
            return end;
        }

        public Part getPart() {
            return part;
        }

        public Long getContinuedFrom() {
            return continuedFrom;
        }

    }

    private final Settings settings;
    private Col last;
    private List<ColFragment> frags = new ArrayList<>();

    public Col(Settings settings, Col last) {
        this.settings = settings;
        this.last = last;
        frags.add(new ColFragment());
    }

    public void set(ColFragment ff) {
        //System.out.println("Placing fixed fragment " + ff);
        double fro = ff.start;
        double to = ff.end;
        for (int i = 0; i < frags.size(); i++) {
            ColFragment cf = frags.get(i);
            if (cf.part() != null) continue;
            //System.out.printf("CF:end=%f, CF:start=%f, to=%f, fro=%f\n",cf.end(), cf.start(), to, fro);
            if (cf.end() < to || cf.start() > fro) continue;
            frags.remove(cf);
            if (cf.end() > to) {
                frags.add(i, new ColFragment(to, cf.end()));
            }
            frags.add(i, ff);
            //System.out.println(String.format("Placed fixed-place element %s on col %s",ff, this));
            if (cf.start() < fro) {
                frags.add(i, new ColFragment(cf.start(), fro));
            }
            // merge alleys if we can
            if (last != null && ff.part instanceof Valley) {
                for (ColFragment lastFrag : last.frags) {
                    if (lastFrag.start() == ff.start() && lastFrag.part instanceof Valley) {
                        Valley vAlley = ((Valley) lastFrag.part);
                        vAlley.addColumn();
                        ff.part = vAlley;
                    }
                }
            }
            return;
        }
        throw new IllegalStateException(String.format("Failed to place fixed-place element %s on col %s", ff, this));
    }

    public ColFragment longestEmpty() {
        return empty()
                .max(Comparator.comparing(ColFragment::height))
                .orElse(null);
    }

    /**
     * @return all empty fragments in this column
     */
    public Stream<ColFragment> empty() {
        return frags.stream()
                .filter(f -> f.part() == null);
    }

    public List<ColFragment> getFrags() {
        return frags;
    }


    public String toString() {
        return frags.toString();
    }

} // Col
