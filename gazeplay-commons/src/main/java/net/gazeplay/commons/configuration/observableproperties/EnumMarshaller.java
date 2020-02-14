package net.gazeplay.commons.configuration.observableproperties;

import java.util.function.Function;

public class EnumMarshaller<T extends Enum<T>> implements Function<T, String> {

    @Override
    public String apply(final T enumValue) {
        if (enumValue == null) {
            return null;
        }
        return enumValue.name();
    }

}
