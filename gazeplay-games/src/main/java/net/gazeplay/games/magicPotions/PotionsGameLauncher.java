package net.gazeplay.games.magicPotions;

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

}
