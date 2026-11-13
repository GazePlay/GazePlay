package net.gazeplay.games.ladder;

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
 * Creates "Échelle" (name code Ladder) and its statistics (folder "Ladder", as before), for a new game or for the replay
 * of a saved one (see {@code IGameLauncher}). Without a variant of the review (the game had none before), watching, at
 * the normal level, with the animals, after 3 games.
 */
public class LadderGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new LadderStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new LadderStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Ladder create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                         final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final String pictures = grouped.getExtras().isEmpty() ? null : grouped.getExtras().get(0);
            return new Ladder(gameContext, stats, LadderMode.of(grouped.getGroup()), LadderLevel.of(grouped.getValue()),
                LadderEnd.of(grouped.getSubGroup()), LadderPictures.of(pictures), gameSeed);
        }
        return new Ladder(gameContext, stats, LadderMode.WATCH, LadderLevel.NORMAL, LadderEnd.GAMES_3,
            LadderPictures.ANIMALS, gameSeed);
    }
}
