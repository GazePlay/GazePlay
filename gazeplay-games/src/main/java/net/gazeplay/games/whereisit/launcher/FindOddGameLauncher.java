package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;

public class FindOddGameLauncher implements GameSpec.GameLauncher<Stats, DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.FIND_ODD.getGameName());
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(WhereIsItGameType.FIND_ODD, gameVariant.getWidth(),
            gameVariant.getHeight(), false, gameContext, stats);
    }
}
