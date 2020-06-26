package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;


public class WhereIsTheColorGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionDifficultyGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.COLORNAME.getGameName());
    }

    @Override

    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionDifficultyGameVariant gameVariant, Stats stats) {
        if (gameVariant.getDifficulty().equals("easy")) {
            return new WhereIsIt(WhereIsItGameType.COLORNAMEEASY, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats);
        } else {
            return new WhereIsIt(WhereIsItGameType.COLORNAME, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats);
        }
    }
}
