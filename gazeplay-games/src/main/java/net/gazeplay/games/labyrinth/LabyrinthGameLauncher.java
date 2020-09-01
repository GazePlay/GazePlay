package net.gazeplay.games.labyrinth;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class LabyrinthGameLauncher implements IGameLauncher<Stats, EnumGameVariant<LabyrinthGameVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new LabyrinthStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<LabyrinthGameVariant> gameVariant, Stats stats) {
        return new Labyrinth(gameContext, stats, gameVariant.getEnumValue());
    }

}
