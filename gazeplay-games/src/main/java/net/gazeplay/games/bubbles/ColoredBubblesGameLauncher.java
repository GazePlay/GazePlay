package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class ColoredBubblesGameLauncher implements GameSpec.GameLauncher<Stats, EnumGameVariant<BubblesGameVariant>> {
    @Override
    public Stats createNewStats(final Scene scene) {
        return new BubblesGamesStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final EnumGameVariant<BubblesGameVariant> gameVariant, final Stats stats) {
        return new Bubble(gameContext, BubbleType.COLOR, stats, true, gameVariant.getEnumValue());
    }
}
