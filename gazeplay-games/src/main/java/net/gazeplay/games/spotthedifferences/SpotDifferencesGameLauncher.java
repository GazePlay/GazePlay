package net.gazeplay.games.spotthedifferences;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class SpotDifferencesGameLauncher implements GameSpec.GameLauncher<SpotTheDifferencesStats, GameSpec.GameVariant> {

    @Override
    public SpotTheDifferencesStats createNewStats(Scene scene) {
        return new SpotTheDifferencesStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant,
                                       SpotTheDifferencesStats stats) {
        return new SpotTheDifferences(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.GameVariant gameVariant,
                                       SpotTheDifferencesStats stats, double gameSeed) {
        return new SpotTheDifferences(gameContext, stats);
    }
}
