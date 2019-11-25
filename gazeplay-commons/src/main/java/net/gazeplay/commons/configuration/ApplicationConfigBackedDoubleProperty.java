package net.gazeplay.commons.configuration;


import java.beans.PropertyChangeListener;

public class ApplicationConfigBackedDoubleProperty extends PropertiesDoubleProperty {

    private final ApplicationConfig properties;

    public ApplicationConfigBackedDoubleProperty(ApplicationConfig properties, String propertyName, double defaultValue, PropertyChangeListener propertyChangeListener) {
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
