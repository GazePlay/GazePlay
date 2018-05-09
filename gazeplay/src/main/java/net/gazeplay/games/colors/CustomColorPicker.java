package net.gazeplay.games.colors;

import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.Getter;

public class CustomColorPicker extends Pane {

    final GridPane colorGrid;

    public static final Color[] COLOR_LIST = { Color.ALICEBLUE, Color.BURLYWOOD, Color.DARKCYAN };

    public static final int NB_COLOR_PER_ROW = ((int) Math.sqrt(COLOR_LIST.length));

    @Getter
    private ColorBox selectedColor;

    private final ColorBox representingBox;

    public CustomColorPicker(final Pane root, final ColorToolBox toolBox, final ColorBox representingBox) {
        super();

        this.colorGrid = new GridPane();
        this.representingBox = representingBox;

        ToggleGroup colorGroup = new ToggleGroup();

        for (int i = 0; i < COLOR_LIST.length; ++i) {

            for (int j = 0; j < NB_COLOR_PER_ROW; ++j) {
                ColorBox colorBox = new CustomColorBox(COLOR_LIST[i], root, toolBox, colorGroup, representingBox);
                if (i == 0 && j == 0) {
                    colorBox.select();
                    selectedColor = colorBox;
                }

                colorGrid.add(colorBox, i, j);
            }
        }

        this.getChildren().add(colorGrid);
    }
}
