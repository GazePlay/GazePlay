package net.gazeplay.commons.utils;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import lombok.Getter;

public class ControlPanelConfigurator {

    @Getter
    private static final ControlPanelConfigurator singleton = new ControlPanelConfigurator();

    public void customizeControlePaneLayout(HBox controlPane) {
        controlPane.setPadding(new Insets(15, 12, 15, 12));
        controlPane.setSpacing(10);
    }

}
