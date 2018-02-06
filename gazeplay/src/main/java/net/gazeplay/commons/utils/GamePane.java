package net.gazeplay.commons.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * This is a pane to represent a game choice at the home menu.
 * 
 * @author thomas MEDARD
 */
public class GamePane extends BorderPane {

    @Getter
    private final Label gameLabel;

    /**
     * The constructor. It will also add the css class.
     * 
     * @param gameName
     *            The name of the game corresponding to this GamePane.
     */
    public GamePane(String gameName) {
        // The bottom of the main pane
        VBox bottomBox = new VBox();
        bottomBox.setAlignment(Pos.CENTER);

        // Add the game title
        this.gameLabel = new Label(gameName);

        StackPane bottomPane = new StackPane();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().add(gameLabel);

        bottomBox.getChildren().add(bottomPane);

        this.setBottom(bottomBox);

        this.getStyleClass().add("gamePane");
    }
}
