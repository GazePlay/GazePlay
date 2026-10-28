package net.gazeplay.ui.menus;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.MenuUtils;
import net.gazeplay.components.CssUtil;

/**
 * Buttons shared by the menus of the games and of the evaluations.
 */
public final class MenuButtons {

    private MenuButtons() {
    }

    /**
     * @return the round button switching the full screen (violet), its icon and tooltip following the state of the
     * window
     */
    public static Button fullScreenButton(final GazePlay gazePlay, final Translator translator) {
        final Button button = MenuStyle.roundButton(MenuIcon.FULLSCREEN, "violet", "", gazePlay::toggleFullScreen);
        final Runnable update = () -> {
            final boolean fullScreen = gazePlay.isFullScreen();
            button.setGraphic((fullScreen ? MenuIcon.FULLSCREEN_EXIT : MenuIcon.FULLSCREEN).create(40));
            final String text = translator.translate(fullScreen ? "Exit FullScreen" : "Enter FullScreen");
            button.setText(text);
            button.getTooltip().setText(text);
        };
        update.run();
        gazePlay.getFullScreenProperty().addListener((observable, oldValue, newValue) -> update.run());
        return button;
    }

    /**
     * @return the version of GazePlay Learning, as a link opening its licence in a window of the menus
     */
    public static Button versionButton(final GazePlay gazePlay, final Translator translator) {
        final Button versionButton = new Button(MenuUtils.versionText(), MenuIcon.INFO.create(20));
        versionButton.getStyleClass().add("gp-link");
        versionButton.setTooltip(MenuStyle.tooltip(translator.translate("License")));
        versionButton.setOnAction(e -> showLicense(gazePlay, translator));
        return versionButton;
    }

    /**
     * @return the name of the current profile, as shown to the user ("Default user" for the default profile)
     */
    public static String currentProfileName(final Translator translator) {
        final String userName = ActiveConfigurationContext.getInstance().getUserName();
        return userName == null || userName.isEmpty() ? translator.translate("DefaultUser") : userName;
    }

    /**
     * Shows the licence of GazePlay Learning in a window of the menus.
     */
    private static void showLicense(final GazePlay gazePlay, final Translator translator) {
        final TextArea text = new TextArea(MenuUtils.licenseText());
        text.setEditable(false);
        text.setWrapText(true);
        text.setPrefSize(720, 480);
        text.getStyleClass().add("gp-license");
        final VBox content = new VBox(text);
        content.setPadding(new Insets(16, 24, 24, 24));

        final Stage dialog = new Stage();
        dialog.initOwner(gazePlay.getPrimaryStage());
        MenuDialog.undecorate(dialog);
        dialog.setTitle(translator.translate("License"));
        dialog.setScene(MenuDialog.createScene(dialog, content, translator.translate("Close"), dialog::close));
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), dialog.getScene(),
            gazePlay.getCurrentScreenDimensionSupplier());
        dialog.show();
    }
}
