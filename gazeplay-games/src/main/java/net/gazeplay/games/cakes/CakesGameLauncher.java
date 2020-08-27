package net.gazeplay.games.cakes;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class CakesGameLauncher implements IGameLauncher<Stats, EnumGameVariant<CakeGameVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new CakeStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<CakeGameVariant> gameVariant, Stats stats) {
        return new CakeFactory(gameContext, stats, gameVariant.getEnumValue());
    }
}
