package net.gazeplay.commons.configuration;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RatioChangeListener implements ChangeListener<Number> {

    private final DoubleProperty doubleProperty;

    @Override
    public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
        if (newValue.doubleValue() > 1d) {
            log.warn("Invalid value set : {}. 1 set instead", newValue);
            doubleProperty.setValue(1);
        }
        if (newValue.doubleValue() < 0d) {
            log.warn("Invalid value set : {}. 0 set instead", newValue);
            doubleProperty.setValue(0);
        }
    }
}
