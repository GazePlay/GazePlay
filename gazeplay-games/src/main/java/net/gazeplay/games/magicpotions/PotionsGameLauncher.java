package net.gazeplay.games.magicpotions;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;

public class PotionsGameLauncher implements IGameLauncher<MagicPotionsStats, IGameVariant> {

    @Override
    public MagicPotionsStats createNewStats(Scene scene) {
        return new MagicPotionsStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, MagicPotionsStats stats) {
        return new MagicPotions(gameContext, stats);
    }

}
