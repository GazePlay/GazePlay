package net.gazeplay.commons.configuration.observableproperties;


import net.gazeplay.commons.configuration.ApplicationConfig;

import java.beans.PropertyChangeListener;

public class ApplicationConfigBackedBooleanProperty extends PropertiesBooleanProperty {

    private final ApplicationConfig properties;

    public ApplicationConfigBackedBooleanProperty(ApplicationConfig properties, String propertyName, boolean defaultValue, PropertyChangeListener propertyChangeListener) {
        super(propertyName, defaultValue, propertyChangeListener);
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
