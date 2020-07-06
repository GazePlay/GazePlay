package net.gazeplay.games.pianosight;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.shooter.ShooterGamesStats;

public class PianoGameLauncher implements GameSpec.GameLauncher<ShooterGamesStats, GameSpec.DimensionGameVariant> {

    @Override
    public ShooterGamesStats createNewStats(Scene scene) {
        return new ShooterGamesStats(scene, "Piano");
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        GameSpec.DimensionGameVariant gameVariant,
        ShooterGamesStats stats
    ) {
        return new Piano(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(
        IGameContext gameContext,
        GameSpec.DimensionGameVariant gameVariant,
        ShooterGamesStats stats, double gameSeed
    ) {
        return new Piano(gameContext, stats, gameSeed);
    }
}
