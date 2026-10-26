package net.gazeplay.games.magiccards;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of the game "MagicCards", with the info-game.csv file of the games where items are found (see
 * {@code HiddenItemsGamesStats}).
 */
public class MagicCardsGamesStats extends HiddenItemsGamesStats {

    public MagicCardsGamesStats(Scene scene) {

        super(scene);
        gameName = "MagicCards";
    }

    public MagicCardsGamesStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {

        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        gameName = "MagicCards";
    }

}
