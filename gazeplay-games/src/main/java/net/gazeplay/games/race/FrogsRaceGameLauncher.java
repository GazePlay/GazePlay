package net.gazeplay.games.race;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class FrogsRaceGameLauncher implements IGameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new RaceGamesStats(scene, "race");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new Race(gameContext, stats, "race");
    }
}
