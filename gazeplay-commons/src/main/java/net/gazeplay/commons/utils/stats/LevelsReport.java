package net.gazeplay.commons.utils.stats;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class LevelsReport {

    @Getter
    private final List<Long> levelsPerRounds = new ArrayList<>();

    public void addRoundLevel(final long level) {
        this.levelsPerRounds.add(level);
    }

    public List<Long> getOriginalLevelsPerRounds() {
        return Collections.unmodifiableList(levelsPerRounds);
    }

    public long totalLevelSum() {
        long total = 0;
        for (long level : levelsPerRounds) {
            total += level;
        }
        return total;
    }

    public long computeAverageLevel() {
        final int count = levelsPerRounds.size();
        if (count == 0) {
            return 0L;
        }
        return totalLevelSum() / count;
    }

    public double computeVariance() {
        final double average = computeAverageLevel();
        double sum = 0;
        final int count = levelsPerRounds.size();
        for (final Long value : levelsPerRounds) {
            sum += Math.pow((value - average), 2);
        }
        double result;
        if (count == 0) {
            result = 0;
        } else {
            result = sum / count;
        }
        return result;
    }

    public double computeSD() {
        return Math.sqrt(computeVariance());
    }
}
