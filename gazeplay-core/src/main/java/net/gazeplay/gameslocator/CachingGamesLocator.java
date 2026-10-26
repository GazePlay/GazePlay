package net.gazeplay.gameslocator;

import lombok.AllArgsConstructor;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.ui.Translator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Keeps the list of the games of another locator after its first call.
 */
@AllArgsConstructor
public class CachingGamesLocator implements GamesLocator {

    private final GamesLocator delegate;

    private final AtomicReference<List<GameSpec>> cachedResult = new AtomicReference<>();

    @Override
    public List<GameSpec> listGames(Translator translator) {
        List<GameSpec> result = cachedResult.get();
        if (result == null) {
            result = Collections.unmodifiableList(delegate.listGames(translator));
            cachedResult.set(result);
        }
        return result;
    }

}
