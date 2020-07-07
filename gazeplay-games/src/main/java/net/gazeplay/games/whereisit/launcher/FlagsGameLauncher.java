package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;

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
