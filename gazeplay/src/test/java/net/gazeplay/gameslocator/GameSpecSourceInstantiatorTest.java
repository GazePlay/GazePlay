package net.gazeplay.gameslocator;

import net.gazeplay.GameSpecSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSpecSourceInstantiatorTest {

    @Test
    void shouldInstantiateFromListOfSources() throws ClassNotFoundException {
        List<Class> classList = new ArrayList<>(List.of(
            Class.forName("net.gazeplay.games.videogrid.VideoGridGameSpecSource"),
            Class.forName("net.gazeplay.games.order.OrderGameSpecSource"),
            Class.forName("net.gazeplay.games.mediaPlayer.MediaPlayerGameSpecSource")
        ));

        GameSpecSourceInstantiator instantiator = new GameSpecSourceInstantiator();
        List<GameSpecSource> result = instantiator.instantiateGameSpecSources(classList);

        assertEquals(3, result.size());
    }

    @Test
    void shouldInstantiateEmptyListFromEmptyListOfSources() {
        List<Class> classList = new ArrayList<>();

        GameSpecSourceInstantiator instantiator = new GameSpecSourceInstantiator();
        List<GameSpecSource> result = instantiator.instantiateGameSpecSources(classList);

        assertEquals(0, result.size());
    }

    @Test
    void shouldThrowExceptionForIncorrectClass() {
        assertThrows(GameSpecSourceInstantiator.GameSpecInstantiationException.class, () -> {
            List<Class> classList = new ArrayList<>(List.of(
                Class.forName("net.gazeplay.commons.utils.FileUtils")
            ));

            GameSpecSourceInstantiator instantiator = new GameSpecSourceInstantiator();
            instantiator.instantiateGameSpecSources(classList);
        });
    }
}
