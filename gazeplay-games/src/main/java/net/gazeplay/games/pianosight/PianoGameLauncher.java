package net.gazeplay.games.pianosight;

import javafx.scene.Scene;
import net.gazeplay.IGameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;

public class PianoGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "Piano");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, Stats stats) {
        return new Piano(gameContext, stats);
    }
}
