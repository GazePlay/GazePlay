package net.gazeplay.commons.configuration.observableproperties;


import net.gazeplay.commons.configuration.ApplicationConfig;

import java.beans.PropertyChangeListener;
import java.util.function.Function;

public class ApplicationConfigBackedObjectProperty<T> extends PropertiesObjectProperty<T> {

    private final ApplicationConfig properties;

    private final Function<T, String> marshaller;

    private final Function<String, T> unmarshaller;

    public ApplicationConfigBackedObjectProperty(
        final ApplicationConfig properties,
        final String propertyName,
        final T defaultValue,
        final PropertyChangeListener propertyChangeListener,
        final Function<T, String> marshaller,
        final Function<String, T> unmarshaller
    ) {
        super(propertyName, defaultValue, propertyChangeListener);
        this.properties = properties;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    @Override
    protected void setProperty(final String propertyName, final T propertyValue) {
        properties.setProperty(propertyName, marshaller.apply(propertyValue));
    }

    @Override
    protected T getProperty(final String propertyName) {
        return unmarshaller.apply(properties.getProperty(propertyName));
    }

}
