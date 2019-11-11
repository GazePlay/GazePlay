package net.gazeplay.gameslocator;

import net.gazeplay.GameSpec;
import net.gazeplay.commons.ui.Translator;

import java.util.List;

public interface GamesLocator {

    List<GameSpec> listGames(Translator translator);

}
