package net.gazeplay.games.math101;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;

public class Math102GameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new MathGamesStats(scene);
    }// Need to make customized stats

    @Override
    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                                       Stats stats) {
        return new Math101(Math101.Math101GameType.SUBTRACTIONPOS, gameContext, gameVariant.getNumber(),
            stats);
    }
}
