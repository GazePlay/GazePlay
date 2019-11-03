package net.gazeplay.games.math101;

import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

@RequiredArgsConstructor
public final class MathGameLauncher implements GameSpec.GameLauncher<Stats, MathGameVariant> {
    
    private final Math101GameType math101GameType;
    
    @Override
    public Stats createNewStats(Scene scene) {
        return new MathGamesStats(scene);
    }// Need to make customized stats

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, MathGameVariant gameVariant, Stats stats) {
        return new Math101(math101GameType, gameContext, gameVariant, stats);
    }
}
