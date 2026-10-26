package net.gazeplay.commons.utils.screen;

import javafx.stage.Screen;

import java.util.function.Supplier;

/**
 * Gives the primary screen of the system.
 */
public class PrimaryScreenSupplier implements Supplier<Screen> {
    @Override
    public Screen get() {
        return Screen.getPrimary();
    }
}
