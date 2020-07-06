package net.gazeplay.games.ninja;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class NinjaGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<NinjaGameVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new NinjaStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.EnumGameVariant<NinjaGameVariant> gameVariant, Stats stats) {
        return new Ninja(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.EnumGameVariant<NinjaGameVariant> gameVariant, Stats stats, double gameSeed) {
        return new Ninja(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }

}
