package net.gazeplay.commons.configuration.observableproperties;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class EnumUnmarshaller<T extends Enum<T>> implements Function<String, T> {

    private final Class<T> enumClass;

    @Override
    public T apply(final String name) {
        return Enum.valueOf(enumClass, name);
    }
}
