package net.gazeplay.games.colors;

import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.gazeplay.IGameContext;

public class CustomColorBox extends ColorBox {

    private final ColorBox representingBox;
    private final ColorToolBox toolBox;

    public CustomColorBox(final IGameContext gameContext, final Color color, final Pane root, final ColorToolBox toolBox, final ToggleGroup colorGroup,
                          final ColorBox representingBox, final double colorizeButtonsSizePx) {
        super(gameContext, color, root, toolBox, colorGroup, colorizeButtonsSizePx);
        this.representingBox = representingBox;
        this.toolBox = toolBox;
    }

    @Override
    protected void action() {
        if (toolBox.isDialogOpen()) {
            representingBox.setColor(this.getColor());
            representingBox.action();
        }
    }

}
