package net.gazeplay.games.bottle;

import javafx.scene.Scene;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Bouteille" (name code Bottle) and its statistics (folder "BottleGame", as before), for a new game or for the
 * replay of a saved one (see {@code IGameLauncher}). The variants of before the review (a number of bottles and a type)
 * are read as breaking all the bottles, as many, tiny and small bottles small, normal and high ones medium, big ones
 * big, and the bottles "à l'infini" without end (maintainer's choice of 13/11/2026).
 */
public class BottleGameLauncher implements IGameLauncher<BottleGameStats, IGameVariant> {

    @Override
    public BottleGameStats createNewStats(Scene scene) {
        return new BottleGameStats(scene);
    }

    @Override
    public BottleGame createNewGame(IGameContext gameContext, IGameVariant gameVariant, BottleGameStats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public BottleGameStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BottleGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public BottleGame replayGame(IGameContext gameContext, IGameVariant gameVariant, BottleGameStats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static BottleGame create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                             final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final String size = grouped.getExtras().isEmpty() ? null : grouped.getExtras().get(0);
            return new BottleGame(gameContext, stats, BottleMode.of(grouped.getGroup()),
                BottleCount.of(grouped.getValue()), BottleSize.of(size), BottleEnd.of(grouped.getSubGroup()), gameSeed);
        }
        if (gameVariant instanceof IntStringGameVariant former) {
            final String type = former.getStringValue();
            final BottleSize size = switch (type) {
                case "TinyBottles", "SmallBottles" -> BottleSize.SMALL;
                case "BigBottles" -> BottleSize.LARGE;
                default -> BottleSize.MEDIUM;
            };
            final BottleEnd end = "InfinityBottles".equals(type) ? BottleEnd.ENDLESS : BottleEnd.SERIES_1;
            return new BottleGame(gameContext, stats, BottleMode.ALL, BottleCount.of(former.getNumber()), size, end,
                gameSeed);
        }
        return new BottleGame(gameContext, stats, BottleMode.ALL, BottleCount.EIGHT, BottleSize.MEDIUM,
            BottleEnd.SERIES_1, gameSeed);
    }
}
