package net.gazeplay.commons.utils.screen;

import javafx.stage.Screen;

import java.util.function.Supplier;

public class PrimaryScreenSupplier implements Supplier<Screen> {
    @Override
    public Screen get() {
        return Screen.getPrimary();
    }
}
