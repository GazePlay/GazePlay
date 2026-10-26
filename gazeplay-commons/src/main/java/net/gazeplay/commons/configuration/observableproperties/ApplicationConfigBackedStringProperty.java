package net.gazeplay.commons.configuration.observableproperties;


import net.gazeplay.commons.configuration.ApplicationConfig;

import java.beans.PropertyChangeListener;

/**
 * A text setting, stored under a key of GazePlay.properties.
 */
public class ApplicationConfigBackedStringProperty extends PropertiesStringProperty {

    private final ApplicationConfig properties;

    public ApplicationConfigBackedStringProperty(final ApplicationConfig properties, final String propertyName, final String defaultValue, final PropertyChangeListener propertyChangeListener) {
        super(propertyName, defaultValue, propertyChangeListener);
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
