package net.gazeplay.commons.utils.stats;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Levels reached in the rounds of a game with levels, for the statistics screen.
 */
@Slf4j
public class LevelsReport {

    @Getter
    private final List<Long> levelsPerRounds = new ArrayList<>();

    /**
     * Adds the level of a round.
     */
    public void addRoundLevel(final long level) {
        this.levelsPerRounds.add(level);
    }

    public List<Long> getOriginalLevelsPerRounds() {
        return Collections.unmodifiableList(levelsPerRounds);
    }

    /**
     * @return the sum of the levels of the rounds
     */
    public long totalLevelSum(){
        long total = 0;
        for (long level: levelsPerRounds) {
            total += level;
        }
        return total;
    }

    /**
     * @return the average level, rounded down (integer division); 0 without round
     */
    public long computeAverageLevel() {
        final int count = levelsPerRounds.size();
        if (count == 0) {
            return 0L;
        }
        return totalLevelSum() / count;
    }

    /**
     * @return the population variance (divided by n) of the levels, around the rounded average; 0 without round
     */
    public double computeVariance() {
        final double average = computeAverageLevel();
        double sum = 0;
        final int count = levelsPerRounds.size();
        for (final Long value : levelsPerRounds) {
            sum += Math.pow((value - average), 2);
        }
        double result;
        if (count == 0)
            result = 0;
        else
            result = sum / count;
        return result;
    }

    /**
     * @return the standard deviation of the levels (square root of {@link #computeVariance()})
     */
    public double computeSD() {
        return Math.sqrt(computeVariance());
    }
}
