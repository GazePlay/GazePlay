package net.gazeplay.games.scratchcard;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

public class ScratchcardGamesStats extends HiddenItemsGamesStats {

    public ScratchcardGamesStats(Scene scene) {

        super(scene);
        gameName = "Scratchcard";
    }

    public ScratchcardGamesStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {

        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        gameName = "Scratchcard";
    }

}
