package net.gazeplay.ui.menus;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The windows of the menus (e.g. creating a profile), drawn in the look of the menus instead of the frame of the
 * system: a rounded card with a shadow, the title of the window and a round close button at the top. The window is
 * moved by dragging its title; Escape closes it (maintainer's feedback of 28/10/2026: the red cross of the system frame
 * did not fit the menus).
 */
public final class MenuDialog {

    private MenuDialog() {
    }

    /**
     * Makes a window undecorated: call before showing it, its scene being made by {@link #createScene}.
     */
    public static void undecorate(final Stage dialog) {
        dialog.initStyle(StageStyle.TRANSPARENT);
    }

    /**
     * @param dialog    the window, undecorated ({@link #undecorate}); its title is shown at the top of the card
     * @param content   what the window shows
     * @param closeText the tooltip of the close button
     * @param onClose   what closing the window does (close button, Escape), e.g. close it and remove the blur of the
     *                  page below
     * @return the scene of the window, with the look of the menus; its stylesheets are those of the settings, to be set
     * by the caller (CssUtil)
     */
    public static Scene createScene(final Stage dialog, final Node content, final String closeText,
                                    final Runnable onClose) {
        final Label title = new Label();
        title.textProperty().bind(dialog.titleProperty());
        title.getStyleClass().add("gp-dialog-title");

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final Button closeButton = MenuStyle.iconButton(MenuIcon.CLOSE, closeText, onClose);
        final HBox header = new HBox(title, spacer, closeButton);
        header.getStyleClass().add("gp-dialog-header");
        makeDraggable(dialog, header);

        final VBox card = new VBox(header, content);
        card.getStyleClass().add("gp-dialog");
        VBox.setVgrow(content, Priority.ALWAYS);

        // Transparent, with room around the card for its shadow (menus.css)
        final StackPane root = new StackPane(card);
        root.getStyleClass().add("gp-dialog-root");
        MenuStyle.applyTo(root);

        final Scene scene = new Scene(root, Color.TRANSPARENT);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                onClose.run();
            }
        });
        return scene;
    }

    /**
     * Moves the window when its title bar is dragged.
     */
    private static void makeDraggable(final Stage dialog, final Node handle) {
        final double[] offset = new double[2];
        handle.setOnMousePressed(event -> {
            offset[0] = event.getScreenX() - dialog.getX();
            offset[1] = event.getScreenY() - dialog.getY();
        });
        handle.setOnMouseDragged(event -> {
            dialog.setX(event.getScreenX() - offset[0]);
            dialog.setY(event.getScreenY() - offset[1]);
        });
    }
}
