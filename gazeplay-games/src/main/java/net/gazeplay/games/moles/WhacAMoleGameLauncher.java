package net.gazeplay.games.moles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class WhacAMoleGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new MoleStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        GameSpec.DimensionGameVariant gameVariant,
        Stats stats
    ) {
        return new Moles(gameContext, stats);
    }

}
