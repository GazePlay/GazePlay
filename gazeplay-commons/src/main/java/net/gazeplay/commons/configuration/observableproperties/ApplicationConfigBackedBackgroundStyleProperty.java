package net.gazeplay.commons.configuration.observableproperties;


import net.gazeplay.commons.configuration.ApplicationConfig;
import net.gazeplay.commons.configuration.Configuration;

import java.beans.PropertyChangeListener;

public class ApplicationConfigBackedBackgroundStyleProperty extends PropertiesBackgroundStyleProperty {

    private final ApplicationConfig properties;

    public ApplicationConfigBackedBackgroundStyleProperty(final ApplicationConfig properties, final String propertyName, final Configuration.BackgroundStyle defaultValue, final PropertyChangeListener propertyChangeListener) {
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
