package net.gazeplay.games.moles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class WhacAMoleGameLauncher implements IGameLauncher<Stats, DimensionGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new MoleStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        DimensionGameVariant gameVariant,
        Stats stats
    ) {
        return new Moles(gameContext, stats);
    }

}
