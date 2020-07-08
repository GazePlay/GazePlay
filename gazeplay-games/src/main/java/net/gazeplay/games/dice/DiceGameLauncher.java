package net.gazeplay.games.dice;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class DiceGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "dice");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       IntGameVariant gameVariant, Stats stats) {
        return new Dice(gameContext, stats, gameVariant.getNumber());
    }

}
