package net.gazeplay.games.ninja;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class NinjaGameLauncher implements IGameLauncher<Stats, EnumGameVariant<NinjaGameVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new NinjaStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<NinjaGameVariant> gameVariant, Stats stats) {
        return new Ninja(gameContext, stats, gameVariant.getEnumValue());
    }

}
