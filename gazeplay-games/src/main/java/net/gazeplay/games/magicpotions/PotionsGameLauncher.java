package net.gazeplay.games.magicpotions;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class PotionsGameLauncher implements GameSpec.GameLauncher<MagicPotionsStats, GameSpec.GameVariant> {

    @Override
    public MagicPotionsStats createNewStats(Scene scene) {
        return new MagicPotionsStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, MagicPotionsStats stats) {
        return new MagicPotions(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, MagicPotionsStats stats, double gameSeed) {
        return new MagicPotions(gameContext, stats, gameSeed);
    }

}
