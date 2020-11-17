package net.gazeplay;

import javafx.scene.Scene;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public interface IGameLauncher<T extends Stats, V extends IGameVariant> {

    T createNewStats(Scene scene);

    GameLifeCycle createNewGame(IGameContext gameContext, V gameVariant, T stats);

    T createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo);

    GameLifeCycle replayGame(IGameContext gameContext, V gameVariant, T stats, double gameSeed);
}
