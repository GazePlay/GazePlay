package net.gazeplay.games.pianosight;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.games.shooter.ShooterGamesStats;

public class PianoGameLauncher implements IGameLauncher<ShooterGamesStats, DimensionGameVariant> {

    @Override
    public ShooterGamesStats createNewStats(Scene scene) {
        return new ShooterGamesStats(scene, "Piano");
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        DimensionGameVariant gameVariant,
        ShooterGamesStats stats
    ) {
        return new Piano(gameContext, stats);
    }
}
