package net.gazeplay.ui.menus;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 * The shared look of the menus (REFONTE_GRAPHIQUE.md): their styles are in data/stylesheets/menus.css, under the
 * style class {@code gp-menu} put on the root of each menu screen and dialog. The games never carry this class: the
 * styles of the menus do not change them.
 */
public final class MenuStyle {

    /**
     * Style class of the root of a menu screen or dialog.
     */
    public static final String MENU = "gp-menu";

    private MenuStyle() {
    }

    /**
     * Gives a screen or a dialog the look of the menus (background, fonts, controls), and loads the fonts if needed.
     */
    public static void applyTo(final Parent root) {
        MenuFonts.load();
        if (!root.getStyleClass().contains(MENU)) {
            root.getStyleClass().add(MENU);
        }
    }

    /**
     * @return the large title of a screen
     */
    public static Label title(final String text) {
        final Label title = new Label(text);
        title.getStyleClass().add("gp-title");
        return title;
    }

    /**
     * @param tooltip the text shown on hover, which also names the button for the screen readers
     * @return a round button showing only an icon
     */
    public static Button iconButton(final MenuIcon icon, final String tooltip, final Runnable action) {
        return iconButton(icon, 32, tooltip, action);
    }

    /**
     * @return the button quitting GazePlay: larger than the other icon buttons, red and filled, so that it is seen at
     * once (maintainer's feedback of 28/10/2026)
     */
    public static Button quitButton(final String tooltip, final Runnable action) {
        final Button button = iconButton(MenuIcon.POWER, 40, tooltip, action);
        button.getStyleClass().add("gp-quit");
        return button;
    }

    private static Button iconButton(final MenuIcon icon, final double size, final String tooltip,
                                     final Runnable action) {
        final Button button = new Button(tooltip, icon.create(size));
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("gp-icon-button");
        button.setOnAction(e -> action.run());
        return button;
    }
}
