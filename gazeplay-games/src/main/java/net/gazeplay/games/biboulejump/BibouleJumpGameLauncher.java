package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;

public class BibouleJumpGameLauncher implements GameSpec.GameLauncher<BibouleJumpStats, GameSpec.EnumGameVariant<BibouleJumpVariant>> {

    @Override
    public BibouleJumpStats createNewStats(Scene scene) {
        return new BibouleJumpStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue());
    }
    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats, double gameSeed) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }

}
