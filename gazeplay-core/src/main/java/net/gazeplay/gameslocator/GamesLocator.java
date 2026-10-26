package net.gazeplay.gameslocator;

import net.gazeplay.GameSpec;
import net.gazeplay.commons.ui.Translator;

import java.util.List;

/**
 * Finds the games of GazePlay.
 */
public interface GamesLocator {

    /**
     * @return the games, in the order of the menu (the translator gives the names to sort them)
     */
    List<GameSpec> listGames(Translator translator);

}
