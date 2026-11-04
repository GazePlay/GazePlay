package net.gazeplay.games.biboulejump;

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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "BibJump" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). Without a variant (or with one of before the review of 4/11/2026): the normal level, without
 * end.
 */
public class BibouleJumpGameLauncher implements IGameLauncher<BibouleJumpStats, IGameVariant> {

    @Override
    public BibouleJumpStats createNewStats(Scene scene) {
        return new BibouleJumpStats(scene);
    }

    @Override
    public BibouleJumpStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BibouleJumpStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, BibouleJumpStats stats) {
        return new BibouleJump(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, BibouleJumpStats stats, double gameSeed) {
        return new BibouleJump(gameContext, stats, levelOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static BibouleJumpLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? BibouleJumpLevel.of(grouped.getSubGroup())
            : BibouleJumpLevel.NORMAL;
    }

    static BibouleJumpEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? BibouleJumpEnd.of(grouped.getValue())
            : BibouleJumpEnd.ENDLESS;
    }
}
