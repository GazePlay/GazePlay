package net.gazeplay.commons.utils.stats;

import lombok.Getter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Durations of the rounds of a game (time to reach each goal), for the statistics.
 */
public class RoundsDurationReport {

    @Getter
    private long totalAdditiveDuration;
    @Getter
    private final List<Long> durationBetweenGoals = new ArrayList<>();

    /**
     * Adds the duration of a round, in ms.
     */
    public void addRoundDuration(final long lastRoundDuration) {
        this.durationBetweenGoals.add(lastRoundDuration);
        this.totalAdditiveDuration += lastRoundDuration;
    }

    /**
     * @return the median duration (average of the two middle values, rounded down, for an even count); 0 without round
     */
    public long computeMedianDuration() {
        final int count = durationBetweenGoals.size();
        if (count == 0) {
            return 0L;
        }

        final List<Long> sortedList = new ArrayList<>(durationBetweenGoals);
        Collections.sort(sortedList);

        int middle = count / 2;

        if (count % 2 == 0) {// number of elements is even, median is the average of the two central numbers

            middle -= 1;
            return (sortedList.get(middle) + sortedList.get(middle + 1)) / 2;

        } else {// number of elements is odd, median is the central number

            return sortedList.get(middle);
        }
    }

    /**
     * @return the average duration, rounded down; 0 without round
     */
    public long computeAverageLength() {
        final int count = durationBetweenGoals.size();
        if (count == 0) {
            return 0L;
        }
        return totalAdditiveDuration / count;
    }

    /**
     * @return the population variance (divided by n) of the durations, around the rounded average; NaN without round
     */
    public double computeVariance() {
        final double average = computeAverageLength();
        double sum = 0;
        final int count = durationBetweenGoals.size();
        for (final Long value : durationBetweenGoals) {
            sum += Math.pow((value - average), 2);
        }
        return sum / count;
    }

    /**
     * @return the standard deviation of the durations (square root of {@link #computeVariance()})
     */
    public double computeSD() {
        return Math.sqrt(computeVariance());
    }

    public List<Long> getOriginalDurationsBetweenGoals() {
        return Collections.unmodifiableList(durationBetweenGoals);
    }

    /**
     * @return the durations sorted for the chart of the statistics screen: the shortest at both ends and the longest in
     *   the middle (alternately placed from the start and from the end)
     */
    public List<Long> getSortedDurationsBetweenGoals() {
        final int count = durationBetweenGoals.size();

        final List<Long> normalList = new ArrayList<>(durationBetweenGoals);
        final List<Long> sortedList = new ArrayList<>(normalList);
        Collections.sort(sortedList);

        int j = 0;
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                normalList.set(j, sortedList.get(i));
            } else {
                normalList.set(count - 1 - j, sortedList.get(i));
                j++;
            }
        }

        return normalList;
    }

    /**
     * Writes the durations, each followed by a comma.
     */
    void printLengthBetweenGoalsToString(final PrintWriter out) {
        for (final Long value : durationBetweenGoals) {
            out.print(value);
            out.print(',');
        }
    }

}
