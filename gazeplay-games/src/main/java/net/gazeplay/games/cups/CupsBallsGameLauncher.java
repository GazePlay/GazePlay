package net.gazeplay.games.cups;

import javafx.scene.Scene;
import net.gazeplay.IGameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cups.utils.CupsAndBallsStats;

public class CupsBallsGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.CupsGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new CupsAndBallsStats(scene);
    }

    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.CupsGameVariant gameVariant,
                                       Stats stats) {
        return new CupsAndBalls(gameContext, stats, gameVariant.getNoCups(), 3);
    }
}
