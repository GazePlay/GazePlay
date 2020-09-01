package net.gazeplay.games.scratchcard;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.blocs.Blocs;

public class ScratchCardGameLauncher implements IGameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new ScratchcardGamesStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new Blocs(gameContext, 100, 100, false, 0.6f, true, stats);
    }
}
