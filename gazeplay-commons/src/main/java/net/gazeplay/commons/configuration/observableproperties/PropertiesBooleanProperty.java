package net.gazeplay.commons.configuration.observableproperties;

import javafx.beans.property.BooleanPropertyBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * JavaFX boolean property whose value is not held in memory but read from, and written to, a key/value store at each
 * access (implemented by the ApplicationConfigBacked... subclass). The default value is given while the key is absent.
 * Each change is also reported to a {@link PropertyChangeListener} (the configuration, which saves its file).
 */
@RequiredArgsConstructor
@Slf4j
public abstract class PropertiesBooleanProperty extends BooleanPropertyBase {

    private final String propertyName;

    private final boolean defaultValue;

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
    public boolean get() {
        final String propertyValue = getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(propertyValue);
    }

    @Override
    public void set(final boolean value) {
        log.info("Set property {} to {}", propertyName, value);
        setProperty(propertyName, Boolean.toString(value));
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, propertyName, null, value));
        fireValueChangedEvent();
    }

}
