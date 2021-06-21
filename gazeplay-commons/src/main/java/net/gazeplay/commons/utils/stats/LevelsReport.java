package net.gazeplay.commons.utils.stats;

import lombok.Getter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelsReport {

    @Getter
    private final List<Long> levelsPerRounds = new ArrayList<>();

    public void addRoundDuration(final long level) {
        this.levelsPerRounds.add(level);
    }

    public List<Long> getOriginalLevelsPerRounds() {
        return Collections.unmodifiableList(levelsPerRounds);
    }
}
