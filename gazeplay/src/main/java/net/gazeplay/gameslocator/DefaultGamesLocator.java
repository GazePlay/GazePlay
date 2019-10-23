package net.gazeplay.gameslocator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GamesLocator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    private final List<GameSpecSource> sources;

    public DefaultGamesLocator() {
        sources = new ArrayList<>();

        String pkg = "net.gazeplay.games";
        Class<GameSpecSource> searchedInterface = GameSpecSource.class;
        try (ScanResult scanResult =
                 new ClassGraph()
                     .whitelistPackages(pkg)      // Scan com.xyz and subpackages (omit to scan all packages)
                     .scan()) {                   // Start the scan
            for (ClassInfo routeClassInfo : scanResult.getClassesImplementing(searchedInterface.getName())) {
                Class<GameSpecSource> gameClass = routeClassInfo.loadClass(searchedInterface);
                log.info("Found {} class : {}", searchedInterface.getSimpleName(), gameClass);
                try {
                    Constructor<GameSpecSource> defaultConstructor = gameClass.getConstructor();
                    GameSpecSource gameSpecSourceInstance = defaultConstructor.newInstance();
                    sources.add(gameSpecSourceInstance);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create new instance of class " + gameClass, e);
                }
            }
        }
    }

    @Override
    public List<GameSpec> listGames() {
        LinkedList<GameSpec> gameList = new LinkedList<>();
        for (GameSpecSource source : sources) {
            gameList.add(source.getGameSpec());
        }
        log.info("Games found : {}", gameList.size());

        Comparator<GameSpec> gameSpecComparator = Comparator
            .comparing(GameSpec::getGameSummary);

        gameList.sort(gameSpecComparator);

        log.info("Here is the list of games : ");
        for (GameSpec gameSpec : gameList) {
            log.info("   Game {} : {}", gameSpec.getGameSummary().getNameCode(), gameSpec.getGameSummary().getCategories());
        }

        return gameList;
    }

}
