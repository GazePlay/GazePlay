package net.gazeplay.games.draw;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Gribouille" (name code Scribble) and its statistics (folder "Scribble", as before), for a new game or for the
 * replay of a saved one (see {@code IGameLauncher}). Without a variant of the review of 12/11/2026: the palette on a
 * white sheet.
 */
public class ScribbleGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "Scribble");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "Scribble", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Scribble create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                           final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return new Scribble(gameContext, stats, ScribbleMode.of(grouped.getGroup()),
                ScribbleBackground.of(grouped.getValue()), gameSeed);
        }
        return new Scribble(gameContext, stats, ScribbleMode.PALETTE, ScribbleBackground.SHEET, gameSeed);
    }
}
