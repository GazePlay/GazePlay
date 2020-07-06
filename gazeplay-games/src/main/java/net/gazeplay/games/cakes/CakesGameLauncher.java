package net.gazeplay.games.cakes;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class CakesGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<CakeGameVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new CakeStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.EnumGameVariant<CakeGameVariant> gameVariant, Stats stats) {
        return new CakeFactory(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.EnumGameVariant<CakeGameVariant> gameVariant, Stats stats, double gameSeed) {
        return new CakeFactory(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }
}
