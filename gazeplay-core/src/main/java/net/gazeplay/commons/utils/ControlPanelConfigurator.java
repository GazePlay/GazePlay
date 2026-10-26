package net.gazeplay.commons.utils;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import lombok.Getter;

/**
 * Common layout of the control panels (padding and spacing).
 */
public class ControlPanelConfigurator {

    @Getter
    private static final ControlPanelConfigurator singleton = new ControlPanelConfigurator();

    /**
     * Applies the common padding and spacing to a control panel.
     */
    public void customizeControlPaneLayout(HBox controlPane) {
        controlPane.setPadding(new Insets(15, 12, 15, 12));
        controlPane.setSpacing(10);
    }

}
