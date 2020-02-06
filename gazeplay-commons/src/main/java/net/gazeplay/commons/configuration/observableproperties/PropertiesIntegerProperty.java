package net.gazeplay.commons.configuration.observableproperties;

import javafx.beans.property.IntegerPropertyBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@RequiredArgsConstructor
@Slf4j
public abstract class PropertiesIntegerProperty extends IntegerPropertyBase {

    private final String propertyName;

    private final int defaultValue;

    private final PropertyChangeListener propertyChangeListener;

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    protected abstract void setProperty(String propertyName, String propertyValue);

    protected abstract String getProperty(String propertyName);

    @Override
    public int get() {
        final String propertyValue = getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return Integer.parseInt(propertyValue);
    }

    @Override
    public void set(final int value) {
        log.info("Set property {} to {}", propertyName, value);
        setProperty(propertyName, Integer.toString(value));
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, propertyName, null, value));
    }

}
