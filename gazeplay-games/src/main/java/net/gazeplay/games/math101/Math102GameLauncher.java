package net.gazeplay.games.math101;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class Math102GameLauncher implements GameSpec.GameLauncher<Stats, MathGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new MathGamesStats(scene);
    }// Need to make customized stats

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, MathGameVariant gameVariant, Stats stats) {
        return new Math101(Math101GameType.SUBTRACTIONPOS, gameContext, gameVariant, stats);
    }
}
