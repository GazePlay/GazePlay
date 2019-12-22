package net.gazeplay.gameslocator;

import net.gazeplay.GameSpecSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class GameSpecSourceInstanciator {

    public List<GameSpecSource> instanciateGameSpecSource(List<Class> gamesClasses) {
        return gamesClasses
            .stream()
            .map(gameClass -> {
                try {
                    Constructor<GameSpecSource> defaultConstructor = gameClass.getConstructor();
                    return defaultConstructor.newInstance();
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create new instance of class " + gameClass, e);
                }
            })
            .collect(Collectors.toList());
    }

}
