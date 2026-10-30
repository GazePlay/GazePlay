package net.gazeplay.ui.menus;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.components.CssUtil;

/**
 * A message or a question of the menus in their look ({@link MenuDialog}), instead of the alerts of the system: the
 * title, the message (scrolled when long, e.g. a list of problems), and one or two buttons. Shown and waited for.
 */
public final class MenuMessageDialog extends Stage {

    @Getter
    private final Label messageLabel;

    @Getter
    private final Button okButton;

    /**
     * The button cancelling a question; null for a message.
     */
    @Getter
    private final Button cancelButton;

    @Getter
    private boolean accepted;

    /**
     * @param error      a message of error (shown in the color "danger" of the theme)
     * @param cancelText the text of the button cancelling a question; null for a message with one button
     */
    public MenuMessageDialog(final Stage owner, final String title, final String message, final boolean error,
                      final String okText, final String cancelText, final String closeText) {
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.WINDOW_MODAL);
        MenuDialog.undecorate(this);
        setTitle(title);

        messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("gp-dialog-message");
        if (error) {
            messageLabel.setStyle("-fx-text-fill: -gp-danger;");
        }
        final ScrollPane scroller = new ScrollPane(messageLabel);
        scroller.setFitToWidth(true);
        scroller.setPrefViewportWidth(560);
        scroller.setPrefViewportHeight(Math.min(360, 40 + 26 * message.split("\n").length));

        okButton = new Button(okText);
        okButton.getStyleClass().addAll("gp-button", "gp-button-primary");
        okButton.setDefaultButton(true);
        okButton.setOnAction(event -> {
            accepted = true;
            close();
        });
        final HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        if (cancelText != null) {
            cancelButton = new Button(cancelText);
            cancelButton.getStyleClass().addAll("gp-button", "gp-button-secondary");
            cancelButton.setCancelButton(true);
            cancelButton.setOnAction(event -> close());
            buttons.getChildren().add(cancelButton);
        } else {
            cancelButton = null;
        }
        buttons.getChildren().add(okButton);

        final VBox content = new VBox(18, scroller, buttons);
        content.setPadding(new Insets(4, 28, 24, 28));

        setScene(MenuDialog.createScene(this, content, closeText, this::close));
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), getScene());
    }

    /**
     * Shows a message and waits until it is closed.
     */
    public static void inform(final Stage owner, final String title, final String message, final boolean error,
                              final String okText, final String closeText) {
        new MenuMessageDialog(owner, title, message, error, okText, null, closeText).showAndWait();
    }

    /**
     * Asks a question and waits for the answer.
     *
     * @return whether the question was accepted (the "OK" button)
     */
    public static boolean confirm(final Stage owner, final String title, final String message, final String okText,
                                  final String cancelText, final String closeText) {
        final MenuMessageDialog dialog = new MenuMessageDialog(owner, title, message, false, okText, cancelText,
            closeText);
        dialog.showAndWait();
        return dialog.isAccepted();
    }
}
