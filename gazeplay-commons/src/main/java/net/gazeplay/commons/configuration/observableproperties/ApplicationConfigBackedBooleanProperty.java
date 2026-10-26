package net.gazeplay.commons.configuration.observableproperties;


import net.gazeplay.commons.configuration.ApplicationConfig;

import java.beans.PropertyChangeListener;

/**
 * A boolean setting, stored under a key of GazePlay.properties.
 */
public class ApplicationConfigBackedBooleanProperty extends PropertiesBooleanProperty {

    private final ApplicationConfig properties;

    public ApplicationConfigBackedBooleanProperty(final ApplicationConfig properties, final String propertyName, final boolean defaultValue, final PropertyChangeListener propertyChangeListener) {
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
