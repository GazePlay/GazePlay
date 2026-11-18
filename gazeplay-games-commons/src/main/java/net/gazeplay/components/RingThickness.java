package net.gazeplay.components;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Shape;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;

/**
 * The thickness of the rings of the fixations chosen in the settings (menu Esc and settings of the main menu; maintainer's
 * request of 18/11/2026): it changes the width of the line of every ring, round or around a card, and never its
 * diameter, which stays the one chosen by the game. A change is seen at once, even on a ring being filled.
 */
public final class RingThickness {

    private RingThickness() {
    }

    /**
     * @return the thickness of the settings, as a factor of the width chosen by the game (1 by default)
     */
    public static double factor() {
        return ActiveConfigurationContext.getInstance().getRingThickness() / 100.0;
    }

    /**
     * @param width the width of the line of a ring chosen by the game, for the thickness by default
     * @return that width times the thickness of the settings, for a ring sized again at each frame
     */
    public static double width(final double width) {
        return width * factor();
    }

    /**
     * Gives a ring the width chosen by the game times the thickness of the settings, followed when the setting changes.
     * It can be called again on the same ring (a ring sized again with its game element).
     *
     * @param ring  the shape drawing the ring (its stroke)
     * @param width the width of the line of the ring chosen by the game, for the thickness by default
     */
    public static void apply(final Shape ring, final double width) {
        ring.strokeWidthProperty().unbind();
        ring.strokeWidthProperty().bind(widthOf(width));
    }

    /**
     * Same as {@link #apply(Shape, double)} for a round ring whose line stays inside a disc: its radius follows the
     * thickness, so that the outer edge of the line stays on the disc.
     *
     * @param ring        the arc of the ring
     * @param outerRadius the radius of the disc
     * @param width       the width of the line of the ring chosen by the game, for the thickness by default
     */
    public static void applyInside(final Arc ring, final double outerRadius, final double width) {
        apply(ring, width);
        ring.radiusXProperty().unbind();
        ring.radiusYProperty().unbind();
        ring.radiusXProperty().bind(Bindings.createDoubleBinding(
            () -> Math.max(1, outerRadius - ring.getStrokeWidth() / 2), ring.strokeWidthProperty()));
        ring.radiusYProperty().bind(ring.radiusXProperty());
    }

    /**
     * @return the width chosen by the game times the thickness of the settings, followed when the setting changes
     */
    public static DoubleBinding widthOf(final double width) {
        final Configuration config = ActiveConfigurationContext.getInstance();
        final IntegerProperty thickness = config.getRingThicknessProperty();
        // The binding observes the setting through a weak listener: the rings of a game do not stay in memory after it
        return Bindings.createDoubleBinding(() -> width * config.getRingThickness() / 100.0, thickness);
    }
}
