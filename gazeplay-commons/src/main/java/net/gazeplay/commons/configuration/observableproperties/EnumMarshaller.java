package net.gazeplay.commons.configuration.observableproperties;

import java.util.function.Function;

/**
 * Stores an enum value as its name (null stays null).
 */
public class EnumMarshaller<T extends Enum<T>> implements Function<T, String> {

    @Override
    public String apply(final T enumValue) {
        if (enumValue == null) {
            return null;
        }
        return enumValue.name();
    }

}
