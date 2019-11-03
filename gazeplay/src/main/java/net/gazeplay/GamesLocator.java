package net.gazeplay;

import net.gazeplay.commons.ui.Translator;

import java.util.List;

public interface GamesLocator {

    List<GameSpec> listGames(Translator translator);

}
