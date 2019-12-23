package net.gazeplay.commons.configuration.observableproperties;


import net.gazeplay.commons.configuration.ApplicationConfig;

import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class ApplicationConfigBackedStringSetProperty extends PropertiesStringSetProperty {

    private final ApplicationConfig properties;

    public ApplicationConfigBackedStringSetProperty(
        ApplicationConfig properties,
        String propertyName,
        Set<String> defaultValue,
        PropertyChangeListener propertyChangeListener
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
        ApplicationConfig properties,
        String propertyName,
        Set<String> defaultValue,
        PropertyChangeListener propertyChangeListener,
        String delimiter,
        Predicate<String> itemUnmarshallingFilter,
        Function<String, String> itemUnmarshallingMapper,
        Function<String, String> itemMarshallingMapper
    ) {
        super(propertyName, defaultValue, propertyChangeListener, delimiter, itemUnmarshallingFilter, itemUnmarshallingMapper, itemMarshallingMapper);
        this.properties = properties;
    }

    @Override
    protected void setProperty(String propertyName, String propertyValue) {
        properties.setProperty(propertyName, propertyValue);
    }

    @Override
    protected String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

}
