package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.LinkedList;

public class BibouleJumpGameLauncher implements GameSpec.GameLauncher<BibouleJumpStats, GameSpec.EnumGameVariant<BibouleJumpVariant>> {

    @Override
    public BibouleJumpStats createNewStats(Scene scene) {
        return new BibouleJumpStats(scene);
    }

    @Override
    public BibouleJumpStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, LinkedList<FixationPoint> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BibouleJumpStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue());
    }
    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats, double gameSeed) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }

}
