package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class BibouleJumpGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<BibouleJumpVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new BibouleJumpStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.EnumGameVariant<BibouleJumpVariant> gameVariant, Stats stats) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue());
    }

}
