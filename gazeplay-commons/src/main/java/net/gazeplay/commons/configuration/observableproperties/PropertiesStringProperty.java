package net.gazeplay.commons.configuration.observableproperties;

import javafx.beans.property.StringPropertyBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@RequiredArgsConstructor
@Slf4j
public abstract class PropertiesStringProperty extends StringPropertyBase {

    private final String propertyName;

    private final String defaultValue;

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
    public String get() {
        final String propertyValue = getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return propertyValue;
    }

    @Override
    public void set(final String value) {
        log.info("Set property {} to {}", propertyName, value);
        setProperty(propertyName, value);
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, propertyName, null, value));
    }

}
