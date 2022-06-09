package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class BibouleJumpGameLauncher implements IGameLauncher<BibouleJumpStats, EnumGameVariant<BibouleJumpVariant>> {

    @Override
    public BibouleJumpStats createNewStats(Scene scene) {
        return new BibouleJumpStats(scene);
    }

    @Override
    public BibouleJumpStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BibouleJumpStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats, double gameSeed) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }

}
