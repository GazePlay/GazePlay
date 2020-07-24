package net.gazeplay.games.paperScissorsStone;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;

public class PaperScissorsStoneLauncher implements IGameLauncher<PaperScissorsStoneStats, IGameVariant> {

    @Override
    public PaperScissorsStoneStats createNewStats(Scene scene) {
        return new PaperScissorsStoneStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, PaperScissorsStoneStats stats) {
        return new PaperScissorsStoneGame(gameContext, stats);
    }
}
