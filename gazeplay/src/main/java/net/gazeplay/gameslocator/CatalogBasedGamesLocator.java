package net.gazeplay.gameslocator;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CatalogBasedGamesLocator extends AbstractGamesLocator {

    private static final String catalogResourceLocation = "games-catalog.txt";

    @Override
    protected List<Class> findGameSpecSourceClasses() {

        Stream<String> linesStream = Stream.generate(new Supplier<>() {

            final Scanner scanner;

            {
                InputStream resourceAsStream;
                resourceAsStream = getClass().getClassLoader().getResourceAsStream(catalogResourceLocation);
                scanner = new Scanner(Objects.requireNonNull(resourceAsStream), StandardCharsets.UTF_8);
            }

            @Override
            public String get() {
                if (scanner.hasNextLine()) {
                    return scanner.nextLine();
                } else {
                    return null;
                }
            }

        });

        return linesStream
            .takeWhile(Objects::nonNull)
            .map(className -> {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    log.warn("Failed to load class {}", className, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    }

}
