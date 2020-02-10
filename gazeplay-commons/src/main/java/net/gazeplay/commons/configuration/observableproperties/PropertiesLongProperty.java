package net.gazeplay.commons.configuration.observableproperties;

import javafx.beans.property.LongPropertyBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@RequiredArgsConstructor
@Slf4j
public abstract class PropertiesLongProperty extends LongPropertyBase {

    private final String propertyName;

    private final long defaultValue;

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
    public long get() {
        final String propertyValue = getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return Long.parseLong(propertyValue);
    }

    @Override
    public void set(final long value) {
        log.info("Set property {} to {}", propertyName, value);
        setProperty(propertyName, Long.toString(value));
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, propertyName, null, value));
    }

}
