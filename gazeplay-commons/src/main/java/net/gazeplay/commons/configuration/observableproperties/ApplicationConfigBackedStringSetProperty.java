package net.gazeplay.commons.configuration.observableproperties;


import net.gazeplay.commons.configuration.ApplicationConfig;

import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class ApplicationConfigBackedStringSetProperty extends PropertiesStringSetProperty {

    private final ApplicationConfig properties;

    public ApplicationConfigBackedStringSetProperty(
        final ApplicationConfig properties,
        final String propertyName,
        final Set<String> defaultValue,
        final PropertyChangeListener propertyChangeListener
    ) {
        this(properties, propertyName, defaultValue, propertyChangeListener,
            ",",
            s -> {
                if (s == null) {
                    return false;
                }
                return !s.trim().equals("");
            },
            String::trim,
            s -> s
        );
    }

    public ApplicationConfigBackedStringSetProperty(
        final ApplicationConfig properties,
        final String propertyName,
        final Set<String> defaultValue,
        final PropertyChangeListener propertyChangeListener,
        final String delimiter,
        final Predicate<String> itemUnmarshallingFilter,
        final Function<String, String> itemUnmarshallingMapper,
        final Function<String, String> itemMarshallingMapper
    ) {
        super(propertyName, defaultValue, propertyChangeListener, delimiter, itemUnmarshallingFilter, itemUnmarshallingMapper, itemMarshallingMapper);
        this.properties = properties;
    }

    @Override
    protected void setProperty(final String propertyName, final String propertyValue) {
        properties.setProperty(propertyName, propertyValue);
    }

    @Override
    protected String getProperty(final String propertyName) {
        return properties.getProperty(propertyName);
    }

}
