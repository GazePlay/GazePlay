package net.gazeplay.commons.configuration.observableproperties;

import javafx.beans.property.ObjectPropertyBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@RequiredArgsConstructor
@Slf4j
public abstract class PropertiesObjectProperty<T> extends ObjectPropertyBase<T> {

    private final String propertyName;

    private final T defaultValue;

    private final PropertyChangeListener propertyChangeListener;

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    protected abstract void setProperty(String propertyName, T propertyValue);

    protected abstract T getProperty(String propertyName);

    @Override
    public T get() {
        final T propertyValue = getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return propertyValue;
    }

    @Override
    public void set(final T value) {
        log.info("Set property {} to {}", propertyName, value);
        setProperty(propertyName, value);
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, propertyName, null, value));
        fireValueChangedEvent();
    }

}
