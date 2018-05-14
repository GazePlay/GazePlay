package net.gazeplay.games.colors;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;

public class CustomColorPicker extends Pane {

    final GridPane colorGrid;

    public static final Color[] COLOR_LIST = { Color.ALICEBLUE, Color.BURLYWOOD, Color.DARKCYAN };

    public static final int NB_COLOR_PER_ROW = ((int) Math.sqrt(COLOR_LIST.length));

    @Getter
    private ColorBox selectedColor;

    private final ColorBox representingBox;
    
    private final AbstractGazeIndicator progressIndicator;
    
    private final Stage dialog;

    public CustomColorPicker(final Pane root, final ColorToolBox toolBox, final ColorBox representingBox, final Stage dialog) {
        super();

        final VBox mainNode = new VBox();
        mainNode.setSpacing(5);
        mainNode.setAlignment(Pos.CENTER);
        this.getChildren().add(mainNode);
        
        this.colorGrid = new GridPane();
        this.representingBox = representingBox;
        this.dialog = dialog;

        ToggleGroup colorGroup = new ToggleGroup();
        
        progressIndicator = new GazeFollowerIndicator(this);

        for (int i = 0; i < COLOR_LIST.length; ++i) {

            for (int j = 0; j < NB_COLOR_PER_ROW; ++j) {
                ColorBox colorBox = new CustomColorBox(COLOR_LIST[i], root, toolBox, colorGroup, representingBox);
                colorBox.setProgressIndicator(progressIndicator);
                if (i == 0 && j == 0) {
                    colorBox.select();
                    selectedColor = colorBox;
                }

                colorGrid.add(colorBox, i, j);
            }
        }

        mainNode.getChildren().add(colorGrid);
        
        // Close button
        final Button closeButton = new Button("Close");
        mainNode.getChildren().add(closeButton);
        
        this.getChildren().add(progressIndicator);
        progressIndicator.toFront();
    }
}
