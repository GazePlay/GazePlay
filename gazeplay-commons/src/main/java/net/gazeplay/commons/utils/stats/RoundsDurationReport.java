package net.gazeplay.commons.utils.stats;

import lombok.Getter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundsDurationReport {

    @Getter
    private long totalAdditiveDuration;

    private final List<Long> durationBetweenGoals = new ArrayList<>();

    public void addRoundDuration(final long lastRoundDuration) {
        this.durationBetweenGoals.add(lastRoundDuration);
        this.totalAdditiveDuration += lastRoundDuration;
    }

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

    public long computeAverageLength() {
        final int count = durationBetweenGoals.size();
        if (count == 0) {
            return 0L;
        }
        return totalAdditiveDuration / count;
    }

    public double computeVariance() {
        final double average = computeAverageLength();
        double sum = 0;
        final int count = durationBetweenGoals.size();
        for (final Long value : durationBetweenGoals) {
            sum += Math.pow((value - average), 2);
        }
        return sum / count;
    }

    public double computeSD() {
        return Math.sqrt(computeVariance());
    }

    public List<Long> getOriginalDurationsBetweenGoals() {
        return Collections.unmodifiableList(durationBetweenGoals);
    }

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

    void printLengthBetweenGoalsToString(final PrintWriter out) {
        for (final Long value : durationBetweenGoals) {
            out.print(value);
            out.print(',');
        }
    }

}
