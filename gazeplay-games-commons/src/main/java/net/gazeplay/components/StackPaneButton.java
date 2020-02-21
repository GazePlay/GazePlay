package net.gazeplay.components;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import lombok.Getter;

public class StackPaneButton extends StackPane {

    @Getter
    private Button button;

    public StackPaneButton() {
        super();
        button = createButton();
    }

    public StackPaneButton(String text) {
        super();
        button = createButton();
        button.setText(text);
    }

    private Button createButton() {
        button = new Button();
        this.minWidthProperty().bind(button.minWidthProperty());
        this.prefWidthProperty().bind(button.minWidthProperty());
        this.minHeightProperty().bind(button.minHeightProperty());
        this.prefHeightProperty().bind(button.prefHeightProperty());
        this.getChildren().add(button);
        return button;
    }

}
