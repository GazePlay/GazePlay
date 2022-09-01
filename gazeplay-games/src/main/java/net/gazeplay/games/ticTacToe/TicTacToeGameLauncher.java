package net.gazeplay.games.ticTacToe;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class TicTacToeGameLauncher implements IGameLauncher<Stats, EnumGameVariant<TicTacToeGameVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new TicTacToeStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new TicTacToeStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        EnumGameVariant<TicTacToeGameVariant> gameVariant,
        Stats stats
    ) {
        return new TicTacToe(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(
        IGameContext gameContext,
        EnumGameVariant<TicTacToeGameVariant> gameVariant,
        Stats stats, double gameSeed
    ) {
        return new TicTacToe(gameContext, stats, gameVariant.getEnumValue());
    }

}
