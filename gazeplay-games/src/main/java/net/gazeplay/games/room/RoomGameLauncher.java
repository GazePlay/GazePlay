package net.gazeplay.games.room;

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
 * Creates "La pièce" (name code Room) and its statistics (as before), for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). Without a variant of the review (the game had none before), the exploration of the
 * living room, the closest to the game of before.
 */
public class RoomGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new RoomStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new RoomStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Room create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                       final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final String level = grouped.getExtras().isEmpty() ? null : grouped.getExtras().get(0);
            return new Room(gameContext, stats, RoomMode.of(grouped.getGroup()), RoomPlace.of(grouped.getValue()),
                RoomEnd.of(grouped.getSubGroup()), RoomLevel.of(level), gameSeed);
        }
        return new Room(gameContext, stats, RoomMode.EXPLORE, RoomPlace.LIVING, RoomEnd.ENDLESS, RoomLevel.PICTURE,
            gameSeed);
    }
}
