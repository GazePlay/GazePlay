package net.gazeplay.commons.configuration.observableproperties;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * Reads an enum value from its name (null stays null; an unknown name throws an IllegalArgumentException).
 */
@RequiredArgsConstructor
public class EnumUnmarshaller<T extends Enum<T>> implements Function<String, T> {

    private final Class<T> enumClass;

    @Override
    public T apply(final String name) {
        if (name == null) {
            return null;
        }
        return Enum.valueOf(enumClass, name);
    }
}
