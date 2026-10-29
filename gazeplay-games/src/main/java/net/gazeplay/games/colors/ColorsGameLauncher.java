package net.gazeplay.games.colors;

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
 * Creates the game "Coloring" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class ColorsGameLauncher implements IGameLauncher<Stats, EnumGameVariant<ColoringVariant>> {

    private ColorsGamesStats gameStat;

    @Override
    public Stats createNewStats(Scene scene) {

        gameStat = new ColorsGamesStats(scene);
        return gameStat;
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        gameStat = new ColorsGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        return gameStat;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<ColoringVariant> gameVariant,
                                       Stats stats) {
        return new ColorsGame(gameContext, gameStat, gameContext.getTranslator(), variantOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<ColoringVariant> gameVariant,
                                       Stats stats, double gameSeed) {
        return new ColorsGame(gameContext, gameStat, gameContext.getTranslator(), variantOf(gameVariant));
    }

    /**
     * @return the variant of a game; the easy coloring pages for a game saved before the variants (none)
     */
    static ColoringVariant variantOf(final EnumGameVariant<ColoringVariant> gameVariant) {
        return gameVariant == null ? ColoringVariant.EASY : gameVariant.getEnumValue();
    }
}
