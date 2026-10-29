package net.gazeplay.ui.scenes.errorhandlingui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.games.WhereIsItVaildator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.menus.MenuDialog;
import net.gazeplay.ui.menus.MenuStyle;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.io.File;
import java.util.LinkedList;

/**
 * Window shown when "Where is it?" is started with custom images but without a valid folder: it asks for the folder
 * (see {@code WhereIsItVaildator}), then starts the game.
 * <p>
 * Look of the menus ({@link MenuDialog}): the name of the game and a close button at the top, rounded buttons.
 */
public class GameWhereIsItErrorPathDialog extends Stage {

    /**
     * Inline style of the message when the folder chosen holds no valid folder of images.
     */
    private static final String ERROR_STYLE = "-fx-text-fill: -gp-danger;";

    @Getter
    private final Label promptLabel;

    @Getter
    private final Button folderButton;

    @Getter
    private final Button resetButton;

    @Getter
    private final Button doneButton;

    /**
     * The message shown when the window opens, shown again once a valid folder is chosen.
     */
    private final String initialMessage;

    public GameWhereIsItErrorPathDialog(
        final GazePlay gazePlay,
        final GameMenuController gameMenuController,
        final Stage primaryStage,
        final GameSpec gameSpec,
        final Parent root,
        final String whereIsItPromptLabelTextKey,
        final IGameVariant finalVariant
    ) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        MenuDialog.undecorate(this);
        // The menu comes back, without its blur, when the window closes, whatever closes it (the game started too)
        setOnHidden(windowEvent -> {
            root.setEffect(null);
            root.setDisable(false);
        });

        final Configuration config = ActiveConfigurationContext.getInstance();
        final Translator translator = gazePlay.getTranslator();

        initialMessage = translator.translate(whereIsItPromptLabelTextKey);
        promptLabel = new Label(initialMessage);
        promptLabel.getStyleClass().add("gp-dialog-prompt");

        final Label folderLabel = new Label(translator.translate("WhereIsItDirectory"));
        folderLabel.getStyleClass().add("gp-setting-name");

        folderButton = new Button();
        folderButton.getStyleClass().addAll("gp-button", "gp-button-secondary");
        // A long path keeps its end (the folder) and is whole in the tooltip
        folderButton.setMaxWidth(420);
        folderButton.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
        final Tooltip pathTooltip = MenuStyle.tooltip("");
        pathTooltip.textProperty().bind(folderButton.textProperty());
        folderButton.setTooltip(pathTooltip);
        showFolder(config.getWhereIsItDir(), translator);

        resetButton = new Button(translator.translate("Reset"));
        resetButton.getStyleClass().addAll("gp-button", "gp-button-secondary");

        doneButton = new Button(translator.translate("Done"));
        doneButton.getStyleClass().addAll("gp-button", "gp-button-primary");
        doneButton.setDisable(true);

        folderButton.setOnAction(event -> {
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            final File currentFolder = new File(config.getWhereIsItDir());
            if (currentFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(currentFolder);
            }
            final File dirFile = directoryChooser.showDialog(this);
            if (dirFile != null) {
                chooseFolder(dirFile.getAbsolutePath(), config, translator);
            }
        });

        resetButton.setOnAction(event -> {
            config.setWhereIsItDir(Configuration.DEFAULT_VALUE_WHERE_IS_IT_DIR);
            showFolder(Configuration.DEFAULT_VALUE_WHERE_IS_IT_DIR, translator);
            showMessage(initialMessage, false);
            doneButton.setDisable(true);
        });

        // A click, Enter or Space
        doneButton.setOnAction(event -> {
            close();
            gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, finalVariant);
        });

        final HBox folderRow = new HBox(10, folderButton, resetButton);
        folderRow.setAlignment(Pos.CENTER);

        final VBox content = new VBox(18, promptLabel, folderLabel, folderRow, doneButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(8, 28, 28, 28));

        final Scene scene = MenuDialog.createScene(this, content, translator.translate("Close"), this::close);
        CssUtil.setPreferredStylesheets(config, scene);
        setScene(scene);
        setWidth(Math.max(560, primaryStage.getWidth() * 0.5));
        setHeight(Math.max(360, primaryStage.getHeight() * 0.45));
    }

    /**
     * Keeps a folder chosen if it holds at least one valid folder of images (then the game can start), otherwise says
     * that it is the wrong folder.
     */
    void chooseFolder(final String path, final Configuration config, final Translator translator) {
        final String folder = Utils.isWindows() ? Utils.convertWindowsPath(path) : path;
        showFolder(folder, translator);
        if (WhereIsItVaildator.getNumberOfValidDirectories(folder, new LinkedList<>()) != 0) {
            config.setWhereIsItDir(folder);
            showMessage(initialMessage, false);
            doneButton.setDisable(false);
        } else {
            showMessage(translator.translate("PickedWrongDir"), true);
            doneButton.setDisable(true);
        }
    }

    /**
     * Shows a folder on its button; "Select" when there is none.
     */
    private void showFolder(final String folder, final Translator translator) {
        folderButton.setText(folder == null || folder.isBlank() ? translator.translate("Select") : folder);
    }

    /**
     * Shows the message at the top of the window, in the color "danger" of the theme for an error.
     */
    private void showMessage(final String message, final boolean error) {
        promptLabel.setText(message);
        promptLabel.setStyle(error ? ERROR_STYLE : "");
    }
}
