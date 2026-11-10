package net.gazeplay.games.math101;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.SelectionGamesStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of "Apprends les Maths" (name code Math101; before the review of 10/11/2026, the 5 games "Math101:
 * Addition", "Math101: All operations", "Math101: Division", "Math101: Multiplication" and "Math101: Substraction"), in
 * the same folder "Math 101" as before (see {@code SelectionGamesStats}).
 */
@Slf4j
public class MathGamesStats extends SelectionGamesStats {
    public MathGamesStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Math 101";
    }

    public MathGamesStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "Math 101";
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {

        SavedStatsInfo statsInfo = super.saveStats();

        log.debug("Stats saved");
        return statsInfo;
    }

}
