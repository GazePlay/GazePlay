package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class FlagsGameLauncher implements GameSpec.GameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.FLAGS.getGameName());
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant,
                                       Stats stats) {
        return new WhereIsIt(WhereIsItGameType.FLAGS, 2, 2, false, gameContext, stats);
    }
}
