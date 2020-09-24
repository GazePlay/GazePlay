package net.gazeplay.games.colors;

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
 * Stats for the color game.
 *
 * @author Thomas Medard
 */
@Slf4j
public class ColorsGamesStats extends SelectionGamesStats {

    public ColorsGamesStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Colors";
    }

    public ColorsGamesStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "Colors";
    }

    @Override
    public SavedStatsInfo saveStats() throws IOException {

        SavedStatsInfo statsInfo = super.saveStats();

        log.debug("Stats saved");
        return statsInfo;
    }
}
