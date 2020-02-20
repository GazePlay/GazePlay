package net.gazeplay.games.mediaPlayer;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import lombok.Getter;

public class ButtonWithProgressIndicator extends StackPane {

    @Getter
    private Button button;

    ButtonWithProgressIndicator() {
        super();
        button = createButton();
    }

    ButtonWithProgressIndicator(String text) {
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
