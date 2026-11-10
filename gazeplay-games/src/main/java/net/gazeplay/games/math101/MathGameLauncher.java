package net.gazeplay.games.math101;

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
 * Creates "Apprends les Maths" (name code Math101) and its statistics, for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). Another variant than those of the review of 10/11/2026 plays the additions up to 10,
 * without end, looking again after a wrong answer.
 */
public final class MathGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new MathGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new MathGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence,
            lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    final double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    private static Math101 create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                  final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final MathTopic topic = MathTopic.of(grouped.getGroup());
            final MathLevel level = MathLevel.of(grouped.getValue());
            return new Math101(gameContext, stats, topic,
                level != null && topic.levels().contains(level) ? level : topic.levels().get(0),
                MathEnd.of(grouped.getSubGroup()),
                MathMistakes.of(grouped.getExtras().isEmpty() ? null : grouped.getExtras().get(0)), gameSeed);
        }
        return new Math101(gameContext, stats, MathTopic.ADDITION, MathLevel.UP_TO_10, MathEnd.ENDLESS,
            MathMistakes.SEARCH_AGAIN, gameSeed);
    }
}
