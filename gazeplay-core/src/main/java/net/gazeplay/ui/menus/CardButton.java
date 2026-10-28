package net.gazeplay.ui.menus;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Region;

/**
 * A clickable card of the menus (style class {@code gp-card}): a button showing only its content, whose text stays its
 * name for the screen readers.
 * <p>
 * Its height is measured at its own width. A button measured without width (as FlowPane and HBox do) passes a negative
 * width to its content: a text on several lines is then measured one word per line, and the card becomes several times
 * too high.
 */
public class CardButton extends Button {

    public CardButton(final String name, final Node content) {
        super(name, content);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        getStyleClass().add("gp-card");
        setMinWidth(Region.USE_PREF_SIZE);
        setMinHeight(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
    }

    @Override
    protected double computePrefHeight(final double width) {
        return super.computePrefHeight(width < 0 ? prefWidth(-1) : width);
    }
}
