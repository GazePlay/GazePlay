package net.gazeplay.games.colors;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ColorBox extends ToggleButton {

    @Getter
    private final Color color;

    private Boolean selected;

    private final ColorToolBox toolBox;

    public static final double COLOR_BOX_WIDTH_PX = 200;

    public ColorBox(final Color color, final Pane root, final ColorToolBox toolBox) {
        super("");

        this.toolBox = toolBox;

        final Rectangle graphic = new Rectangle(COLOR_BOX_WIDTH_PX, computeHeight(root), color);
        this.setGraphic(graphic);

        this.color = color;

        root.heightProperty().addListener((observable) -> {

            double newHeight = this.computeHeight(root);
            log.info("new Height = {}", newHeight);
            graphic.setHeight(newHeight);
        });
    }

    private double computeHeight(final Pane root) {

        double freeSpace = root.getHeight() * ColorToolBox.HEIGHT_POURCENT - ColorToolBox.MAIN_INSETS.getTop()
                + ColorToolBox.MAIN_INSETS.getBottom() + ColorToolBox.SPACING_PX;
        return freeSpace / ColorToolBox.NB_COLORS_DISPLAYED;
    }

    public void select(Rectangle test) {

        test.setFill(color);
    }

    public void select() {

        this.setSelected(true);
    }

    public void unselect() {

        this.setSelected(false);
    }
}
