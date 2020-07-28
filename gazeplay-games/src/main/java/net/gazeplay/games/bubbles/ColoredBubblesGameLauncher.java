package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class ColoredBubblesGameLauncher implements IGameLauncher<Stats, EnumGameVariant<BubblesGameVariant>> {
    @Override
    public Stats createNewStats(final Scene scene) {
        return new BubblesGamesStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final EnumGameVariant<BubblesGameVariant> gameVariant, final Stats stats) {
        return new Bubble(gameContext, BubbleType.COLOR, stats, gameVariant.getEnumValue());
    }
}
