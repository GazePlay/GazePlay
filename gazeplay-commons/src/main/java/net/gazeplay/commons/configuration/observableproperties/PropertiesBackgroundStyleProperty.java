package net.gazeplay.commons.configuration.observableproperties;

import javafx.beans.property.ObjectPropertyBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@RequiredArgsConstructor
@Slf4j
public abstract class PropertiesBackgroundStyleProperty extends ObjectPropertyBase<Configuration.BackgroundStyle> {

    private final String propertyName;

    private final Configuration.BackgroundStyle defaultValue;

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
    public Configuration.BackgroundStyle get() {
        final String propertyValue = getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return Configuration.BackgroundStyle.valueOf(propertyValue);
    }

    @Override
    public void set(final Configuration.BackgroundStyle value) {
        log.info("Set property {} to {}", propertyName, value);
        setProperty(propertyName, value.name());
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, propertyName, null, value));
    }

}
