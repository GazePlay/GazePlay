package net.gazeplay.games.paperScissorsStone;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class PaperScissorsStoneLauncher implements GameSpec.GameLauncher<PaperScissorsStoneStats, GameSpec.GameVariant> {

    @Override
    public PaperScissorsStoneStats createNewStats(Scene scene) {
        return new PaperScissorsStoneStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, PaperScissorsStoneStats stats) {
        return new PaperScissorsStoneGame(gameContext, stats);
    }
}
