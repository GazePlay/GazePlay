package net.gazeplay.games.math101;

import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

@RequiredArgsConstructor
public final class MathGameLauncher implements GameSpec.GameLauncher<Stats, MathGameVariant> {

    private final MathGameType mathGameType;

    @Override
    public Stats createNewStats(final Scene scene) {
        return new MathGamesStats(scene);
    }// Need to make customized stats

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final MathGameVariant gameVariant, final Stats stats) {
        return new Math101(mathGameType, gameContext, gameVariant, stats);
    }
}
