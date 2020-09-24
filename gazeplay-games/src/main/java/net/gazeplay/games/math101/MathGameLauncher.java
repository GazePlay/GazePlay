package net.gazeplay.games.math101;

import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

@RequiredArgsConstructor
public final class MathGameLauncher implements IGameLauncher<Stats, MathGameVariant> {

    private final MathGameType mathGameType;

    @Override
    public Stats createNewStats(final Scene scene) {
        return new MathGamesStats(scene);
    }// Need to make customized stats

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new MathGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final MathGameVariant gameVariant, final Stats stats) {
        return new Math101(mathGameType, gameContext, gameVariant, stats);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final MathGameVariant gameVariant, final Stats stats, double gameSeed) {
        return new Math101(mathGameType, gameContext, gameVariant, stats, gameSeed);
    }
}
