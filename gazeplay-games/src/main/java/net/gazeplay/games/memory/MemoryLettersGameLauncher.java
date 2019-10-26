package net.gazeplay.games.memory;

import javafx.scene.Scene;
import net.gazeplay.IGameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;

public class MemoryLettersGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new MagicCardsGamesStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, Stats stats) {
        return new Memory(Memory.MemoryGameType.LETTERS, gameContext, gameVariant.getWidth(),
            gameVariant.getHeight(), stats, false);
    }
}
