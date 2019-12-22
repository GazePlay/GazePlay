package net.gazeplay.gameslocator;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummaryComparator;
import net.gazeplay.commons.ui.Translator;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public abstract class AbstractGamesLocator implements GamesLocator {

    private final GameSpecSourceInstantiator gameSpecSourceInstantiator = new GameSpecSourceInstantiator();

    @Override
    public List<GameSpec> listGames(Translator translator) {
        LinkedList<GameSpec> gameList = new LinkedList<>();
        List<Class> gamesClasses = findGameSpecSourceClasses();
        for (GameSpecSource source : gameSpecSourceInstantiator.instantiateGameSpecSources(gamesClasses)) {
            gameList.add(source.getGameSpec());
        }
        log.info("Games found : {}", gameList.size());

        Comparator<GameSpec> gameSpecComparator = Comparator
            .comparing(GameSpec::getGameSummary, new GameSummaryComparator(translator));

        gameList.sort(gameSpecComparator);

        log.info("Here is the list of games : ");
        for (GameSpec gameSpec : gameList) {
            log.info("   Game {} : {}", gameSpec.getGameSummary().getNameCode(), gameSpec.getGameSummary().getCategories());
        }

        return gameList;
    }

    protected abstract List<Class> findGameSpecSourceClasses();

}
