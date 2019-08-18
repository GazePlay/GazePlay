package net.gazeplay;

import java.util.List;


public interface GamesLocator {

    List<GameSpec> listGames();

    List<GameSpec> listGames(GameCategories.Category category);

}
