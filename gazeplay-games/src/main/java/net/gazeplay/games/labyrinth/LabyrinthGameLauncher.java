package net.gazeplay.games.labyrinth;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "Labyrinth" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). With a variant of before the review of 9/11/2026: its way of moving and its character, the
 * normal level, without end.
 */
public class LabyrinthGameLauncher implements IGameLauncher<LabyrinthStats, IGameVariant> {

    @Override
    public LabyrinthStats createNewStats(Scene scene) {
        return new LabyrinthStats(scene);
    }

    @Override
    public LabyrinthStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new LabyrinthStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, LabyrinthStats stats) {
        return new Labyrinth(gameContext, stats, characterOf(gameVariant), levelOf(gameVariant), moveOf(gameVariant),
            endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, LabyrinthStats stats, double gameSeed) {
        return new Labyrinth(gameContext, stats, characterOf(gameVariant), levelOf(gameVariant), moveOf(gameVariant),
            endOf(gameVariant), gameSeed);
    }

    private static LabyrinthGameVariant old(final IGameVariant gameVariant) {
        return gameVariant instanceof EnumGameVariant<?> old && old.getEnumValue() instanceof LabyrinthGameVariant value
            ? value : null;
    }

    static LabyrinthCharacter characterOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return LabyrinthCharacter.of(grouped.getGroup());
        }
        final LabyrinthGameVariant old = old(gameVariant);
        return old != null ? old.getCharacter() : LabyrinthCharacter.MOUSE;
    }

    static LabyrinthLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? LabyrinthLevel.of(grouped.getSubGroup())
            : LabyrinthLevel.NORMAL;
    }

    static LabyrinthMove moveOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return LabyrinthMove.of(grouped.getExtras().isEmpty() ? null : grouped.getExtras().get(0));
        }
        final LabyrinthGameVariant old = old(gameVariant);
        return old != null ? old.getMove() : LabyrinthMove.LOCAL_ARROWS;
    }

    static LabyrinthEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? LabyrinthEnd.of(grouped.getValue())
            : LabyrinthEnd.ENDLESS;
    }
}
