package net.gazeplay.games.race;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "FrogsRace" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before 26/11/2026 (the speed only) catches the bugs at once.
 */
public class FrogsRaceGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new RaceGamesStats(scene, "race");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new RaceGamesStats(scene, "race", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new Race(gameContext, stats, variantOf(gameVariant)).withCatch(catchOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new Race(gameContext, stats, variantOf(gameVariant), gameSeed).withCatch(catchOf(gameVariant));
    }

    /**
     * @return the speed chosen, easy for a game saved before the variants (30/10/2026)
     */
    static RaceVariant variantOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return RaceVariant.of(grouped.getValue());
        }
        if (gameVariant instanceof EnumGameVariant<?> former && former.getEnumValue() instanceof RaceVariant speed) {
            return speed;
        }
        return RaceVariant.EASY;
    }

    /**
     * @return how the bugs are caught, at once for a variant of before 26/11/2026
     */
    static RaceCatch catchOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? RaceCatch.of(grouped.getSubGroup())
            : RaceCatch.AT_ONCE;
    }
}
