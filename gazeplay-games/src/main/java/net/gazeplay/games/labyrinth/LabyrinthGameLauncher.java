package net.gazeplay.games.labyrinth;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class LabyrinthGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new LabyrinthStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.IntGameVariant gameVariant,
                                       Stats stats) {
        return new Labyrinth(gameContext, stats, gameVariant.getNumber());
    }

}
