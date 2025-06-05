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
        private boolean stretch;

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
                    "@[" + start() + "-" + end() + "]";
        }

        public double height() {
            return end() - start();
        }


        public Part getPart() {
            return part;
        }

        public Long getContinuedFrom() {
            return continuedFrom;
        }

        /**
         * change the stretch of this column fragment
         * @param end new end
         */
        public void adjustEnd(double end) {
            this.stretch = true;
            this.end = end;
        }

        /**
         *
         * @return true if this fragment should be adjusted from its natural size; false to maintain the standard sizing.
         */
        public boolean isStretch() {
            return stretch;
        }

        /**
         * Calculate the space above this fragment, if any, not including alleys. Returns 0 if there is no space above this fragment.
         * @return a non-negative space value
         */
        public double spaceAbove() {
            int i = frags.indexOf(this);
            if (i > 0) {
                ColFragment above = frags.get(i - 1);
                if (above.part == null)
                    return above.height();
            }
            return 0;
        }
        /**
         * Calculate the space below this fragment, if any, not including alleys. Returns 0 if there is no space below this fragment.
         * @return a non-negative space value
         */
        public double spaceBelow() {
            int i = frags.indexOf(this);
            if (i+1 < frags.size()) {
                ColFragment below = frags.get(i + 1);
                if (below.part == null)
                    return below.height();
            }
            return 0;
        }

        /**
         * Slide {@code this} column fragment down by the specified amount.
         *
         * @param delta a distance (negative to slide up).
         * @throws IllegalStateException if there is no free space available to slide into
         */
        public void adjustDown(double delta) {
            if (delta < 0) {
                adjustUp(-delta);
                return;
            }
            if (delta == 0) return;
            int i = frags.indexOf(this);
            if (i+1 < frags.size()) {
                ColFragment below = frags.get(i + 1);
                if (below.part != null)
                    throw new IllegalStateException(String.format("Attempting to adjust fragment downwards caused collision between %s and %s", this, below));
                if (below.end < below.start + delta)
                    throw new IllegalStateException(String.format("Attempting to adjust fragment downwards: not enough space in %s to move %s by %s", below, this, delta));
                // reduce space below
                below.start += delta;
                if (below.start == below.end) frags.remove(below); // filled the space
                // move this
                start += delta;
                end += delta;
                if (i == 0 || frags.get(i-1).part != null) {
                    // insert new space above
                    setEmpty(new ColFragment(start-delta, start));
                } else {
                    // increase space above
                    frags.get(i-1).end += delta;
                }
            } else throw new IllegalStateException(String.format("Attempting to adjust bottom fragment downwards by %s: %s", delta, this));

        }

        /**
         * Slide {@code this} column fragment up by the specified amount.
         *
         * @param delta a distance (negative to slide down).
         * @throws IllegalStateException if there is no free space available to slide into
         */
        public void adjustUp(double delta) {
            if (delta < 0) {
                adjustDown(-delta);
                return;
            }
            if (delta == 0) return;
            int i = frags.indexOf(this);
            if (i == 0) throw new IllegalStateException(String.format("Attempting to adjust fragment upwards out of page%s", this));
            ColFragment above = frags.get(i - 1);
            if (above.part != null) throw new IllegalStateException(String.format("Attempting to adjust fragment upwards caused collision between %sand %s", this, above));
            if (above.height() < delta) throw new IllegalStateException(String.format("Attempting to adjust fragment downwards: not enough space in %s to move %s by %s", above, this, delta));
            // reduce space above
            above.end -= delta;
            // move this
            start -= delta;
            end -= delta;
            if (i == frags.size()-1 || frags.get(i+1).part != null) {
                // insert new space below
                setEmpty(new ColFragment(end, end+delta));
            } else {
                // increase space below
                frags.get(i+1).start -= delta;
            }
            if (above.start == above.end) frags.remove(above); // filled the space
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

    /**
     * Used internally to set an empty space into the column.
     * @param ff blank column fragment to set
     * @throws IllegalStateException if {@code ff} is non-blank, extends beyond the space of this column, or overlaps an existing fragment.
     */
    private void setEmpty(ColFragment ff) {
        double fro = ff.start;
        if (fro < 0) throw new IllegalArgumentException(String.format("Empty fragment above column start: %s", ff));
        double to = ff.end;
        if (to > settings.getColumnHeight()) throw new IllegalArgumentException(String.format("Empty fragment below column end: %s", ff));
        if (ff.part != null) throw new IllegalArgumentException(String.format("Non-empty fragment must be set onto blank space: %s", ff));
        for (int i = 0; i < frags.size(); i++) {
            ColFragment cf = frags.get(i);
            if (cf.start < fro) continue;
            if (cf.start < to)
                throw new IllegalStateException(String.format("Overlapping blank space setting %s onto %s", ff, frags));
            frags.add(i, ff);
            break;
        }
    }

    /**
     * Set a columnfragment onto this column, adjusting free space.
     * @param ff column fragment to set.
     * @throws IllegalStateException if {@code ff} extends beyond the space of this column, or overlaps an existing fragment.
     */
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
                // track the empty space
                if (cf.end() - to < settings.getMaxSquashVSpace()) {
                    ff.adjustEnd(cf.end());
                } else {
                    frags.add(i, new ColFragment(to, cf.end()));
                }
            }
            frags.add(i, ff);
            //System.out.println(String.format("Placed fixed-place element %s on col %s",ff, this));
            if (cf.start() < fro) {
                // track the empty space
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
