package net.gazeplay.commons.configuration.observableproperties;

import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.beans.property.SetPropertyBase;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class PropertiesStringSetProperty extends SetPropertyBase<String> {

    private final String propertyName;

    private final Set<String> defaultValue;

    private final PropertyChangeListener propertyChangeListener;

    private final String delimiter;

    private final Predicate<String> itemUnmarshallingFilter;

    private final Function<String, String> itemUnmarshallingMapper;

    private final Function<String, String> itemMarshallingMapper;

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
    public ObservableSet<String> get() {
        final SetChangeListener<String> observer = new SetChangeListener<>() {
            @Override
            public void onChanged(final Change<? extends String> change) {
                PropertiesStringSetProperty.this.set((ObservableSet<String>) change.getSet());
            }
        };
        //
        final String propertyValue = getProperty(propertyName);
        if (propertyValue == null) {
            final ObservableSetWrapper<String> result = new ObservableSetWrapper<>(new LinkedHashSet<>(defaultValue));
            result.addListener(observer);
            return result;
        }
        final Set<String> unmarshalledValue = Arrays.stream(propertyValue.split(delimiter)).filter(itemUnmarshallingFilter).map(itemUnmarshallingMapper).collect(Collectors.toCollection(LinkedHashSet::new));
        final ObservableSetWrapper<String> result = new ObservableSetWrapper<>(unmarshalledValue);
        result.addListener(observer);
        return result;
    }

    @Override
    public void set(final ObservableSet<String> value) {
        final String marshalledValue = value.stream().map(itemMarshallingMapper).collect(Collectors.joining(delimiter));
        log.info("Set property {} to {}", propertyName, marshalledValue);
        setProperty(propertyName, marshalledValue);
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, propertyName, null, value));
    }

}
