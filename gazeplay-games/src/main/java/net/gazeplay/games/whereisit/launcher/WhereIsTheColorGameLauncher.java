package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;


public class WhereIsTheColorGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionDifficultyGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.COLOR_NAME.getGameName());
    }

    @Override

    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionDifficultyGameVariant gameVariant, Stats stats) {
        if (gameVariant.getDifficulty().equals("easy")) {
            return new WhereIsIt(WhereIsItGameType.COLOR_NAME_EASY, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats);
        } else {
            return new WhereIsIt(WhereIsItGameType.COLOR_NAME, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats);
        }
    }
}
