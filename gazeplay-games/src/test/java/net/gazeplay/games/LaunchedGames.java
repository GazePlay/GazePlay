package net.gazeplay.games;

import javafx.application.Platform;
import net.gazeplay.GameLifeCycle;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The games launched by a test, closed after it ({@link GameLifeCycle#dispose()}, on the JavaFX thread): a game left
 * running keeps its timers (a "Lost!" pause, a countdown, a loop), which may fire after the end of its class, on mocks
 * already freed (REVUE_DES_JEUX.md, "Mémoire des tests"; maintainer's request of 18/11/2026: every test closes its
 * games). A test class registers it with
 * <pre>{@code @RegisterExtension final LaunchedGames games = new LaunchedGames();}</pre>
 * and gives it each game it launches ({@link #add}). A game closed by the test itself is closed again: the games accept
 * it.
 */
public final class LaunchedGames implements AfterEachCallback {

    private final List<GameLifeCycle> games = new ArrayList<>();

    /**
     * @return the game, closed after the test
     */
    public synchronized <T extends GameLifeCycle> T add(final T game) {
        games.add(game);
        return game;
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final List<GameLifeCycle> launched;
        synchronized (this) {
            launched = new ArrayList<>(games);
            games.clear();
        }
        if (launched.isEmpty()) {
            return;
        }
        final Runnable close = () -> launched.forEach(GameLifeCycle::dispose);
        if (Platform.isFxApplicationThread()) {
            close.run();
        } else {
            WaitForAsyncUtils.asyncFx(close).get();
        }
    }
}
