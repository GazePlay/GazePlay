package net.gazeplay.gameslocator;

import lombok.AllArgsConstructor;
import net.gazeplay.GameSpec;
import net.gazeplay.GamesLocator;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
public class CachingGamesLocator implements GamesLocator {

	private final GamesLocator delegate;

	private final AtomicReference<List<GameSpec>> cachedResult = new AtomicReference<>();

	@Override
	public List<GameSpec> listGames() {
		List<GameSpec> result = cachedResult.get();
		if (result == null) {
			result = delegate.listGames();
			cachedResult.set(result);
		}
		return result;
	}

}
