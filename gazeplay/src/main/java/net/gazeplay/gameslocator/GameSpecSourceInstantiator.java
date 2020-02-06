package net.gazeplay.gameslocator;

import net.gazeplay.GameSpecSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class GameSpecSourceInstantiator {

    static class GameSpecInstantiationException extends RuntimeException {
        GameSpecInstantiationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public List<GameSpecSource> instantiateGameSpecSources(List<Class> gamesClasses) {
        return gamesClasses
            .stream()
            .map(gameClass -> {
                try {
                    Constructor<GameSpecSource> defaultConstructor = gameClass.getConstructor();
                    return defaultConstructor.newInstance();
                } catch (
                    NoSuchMethodException |
                        IllegalAccessException |
                        InstantiationException |
                        InvocationTargetException |
                        ClassCastException e) {
                    throw new GameSpecInstantiationException("Failed to create new instance of class " + gameClass, e);
                }
            })
            .collect(Collectors.toList());
    }

}
