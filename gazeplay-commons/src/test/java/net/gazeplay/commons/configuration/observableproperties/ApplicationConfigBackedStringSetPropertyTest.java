package net.gazeplay.commons.configuration.observableproperties;

import javafx.collections.ObservableSet;
import net.gazeplay.commons.configuration.ApplicationConfig;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Persisted format of the favorite games and hidden categories (GazePlay.properties): items separated by commas, in
 * the order they were added. The set used to be an internal class of JavaFX, replaced by FXCollections.observableSet.
 */
class ApplicationConfigBackedStringSetPropertyTest {

    private final ApplicationConfig config = new ApplicationConfig(new Properties());

    private final PropertyChangeListener listener = mock(PropertyChangeListener.class);

    private final ApplicationConfigBackedStringSetProperty property =
        new ApplicationConfigBackedStringSetProperty(config, "FAVORITE_GAMES", Set.of(), listener);

    @Test
    void shouldReadTheSavedItemsInTheirOrder() {
        config.setProperty("FAVORITE_GAMES", "Ninja, ,WhereIsTheShape,Creampie");

        assertEquals(List.of("Ninja", "WhereIsTheShape", "Creampie"), List.copyOf(property.get()));
    }

    @Test
    void shouldSaveAnItemAddedToTheSet() {
        config.setProperty("FAVORITE_GAMES", "Ninja");

        ObservableSet<String> favorites = property.get();
        favorites.add("WhereIsTheShape");

        assertEquals("Ninja,WhereIsTheShape", config.getProperty("FAVORITE_GAMES"));
        verify(listener).propertyChange(any());
    }

    @Test
    void shouldSaveAnItemRemovedFromTheSet() {
        config.setProperty("FAVORITE_GAMES", "Ninja,WhereIsTheShape");

        property.get().remove("Ninja");

        assertEquals("WhereIsTheShape", config.getProperty("FAVORITE_GAMES"));
    }

    @Test
    void shouldStartFromTheDefaultValueWhenNothingIsSaved() {
        ObservableSet<String> favorites = property.get();
        favorites.add("Ninja");

        assertEquals("Ninja", config.getProperty("FAVORITE_GAMES"));
    }
}
