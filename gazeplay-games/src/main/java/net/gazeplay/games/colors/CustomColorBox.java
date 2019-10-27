package net.gazeplay.games.colors;

import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.gazeplay.IGameContext;

public class CustomColorBox extends ColorBox {

    private final ColorBox representingBox;

    public CustomColorBox(final IGameContext gameContext, final Color color, final Pane root, final ColorToolBox toolBox, final ToggleGroup colorGroup,
                          final ColorBox representingBox) {
        super(gameContext, color, root, toolBox, colorGroup);
        this.representingBox = representingBox;
    }

    @Override
    protected void action() {
        representingBox.setColor(this.getColor());
        representingBox.action();
    }

}
