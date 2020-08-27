package net.gazeplay.games.cups;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cups.utils.CupsAndBallsStats;

public class CupsBallsGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new CupsAndBallsStats(scene);
    }

    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant,
                                       Stats stats) {
        return new CupsAndBalls(gameContext, stats, gameVariant.getNumber(), 3);
    }
}
