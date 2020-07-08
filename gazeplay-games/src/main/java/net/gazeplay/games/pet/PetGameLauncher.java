package net.gazeplay.games.pet;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class PetGameLauncher implements GameSpec.GameLauncher<Stats, DimensionGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new PetStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        DimensionGameVariant gameVariant,
        Stats stats
    ) {
        return new PetHouse(gameContext, stats);
    }

}
