package net.gazeplay.games.horses;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "Horses" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class HorsesGameLauncher implements IGameLauncher<Stats, EnumGameVariant<HorsesVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "horses");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "horses", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<HorsesVariant> gameVariant,
                                       Stats stats) {
        return new Horses(gameContext, stats, variantOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<HorsesVariant> gameVariant,
                                    Stats stats, double gameSeed) {
        return new Horses(gameContext, stats, variantOf(gameVariant), gameSeed);
    }

    /**
     * @return the variant chosen; 2 players for a game saved before the variants against the computer (30/10/2026),
     *   whose variant (a number of players) is no longer offered
     */
    static HorsesVariant variantOf(final EnumGameVariant<HorsesVariant> gameVariant) {
        return gameVariant == null ? HorsesVariant.PLAYERS_2 : gameVariant.getEnumValue();
    }
}
