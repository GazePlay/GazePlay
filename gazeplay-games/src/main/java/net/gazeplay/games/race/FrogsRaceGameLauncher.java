package net.gazeplay.games.race;

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
 * Creates the game "FrogsRace" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class FrogsRaceGameLauncher implements IGameLauncher<Stats, EnumGameVariant<RaceVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new RaceGamesStats(scene, "race");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new RaceGamesStats(scene, "race", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<RaceVariant> gameVariant,
                                       Stats stats) {
        return new Race(gameContext, stats, variantOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<RaceVariant> gameVariant,
                                    Stats stats, double gameSeed) {
        return new Race(gameContext, stats, variantOf(gameVariant), gameSeed);
    }

    /**
     * @return the variant chosen, easy for a game saved before the variants (30/10/2026)
     */
    static RaceVariant variantOf(final EnumGameVariant<RaceVariant> gameVariant) {
        return gameVariant == null ? RaceVariant.EASY : gameVariant.getEnumValue();
    }
}
