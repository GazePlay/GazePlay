package net.gazeplay.ui.scenes.errorhandlingui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.io.File;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;


public class GameErrorDialog extends Stage {
    public GameErrorDialog(
        final GazePlay gazePlay,
        final GameMenuController gameMenuController,
        final Stage primaryStage,
        final GameSpec gameSpec,
        final Parent root,
        final String whereIsItPromptLabelTextKey,
        final ConfigurationContext configurationContext,
        final IGameVariant finalVariant
    ) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        initStyle(StageStyle.UTILITY);
        setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        FlowPane choicePane = new FlowPane();
        choicePane.setAlignment(Pos.CENTER);
        choicePane.setHgap(10);
        choicePane.setVgap(10);

        ScrollPane choicePanelScroller = new ScrollPane();
        choicePanelScroller.setContent(choicePane);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        choicePanelScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: red;";
        I18NLabel promptLabel = new I18NLabel(gazePlay.getTranslator(), whereIsItPromptLabelTextKey);
        promptLabel.setStyle(labelStyle);

        VBox topPane = new VBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(promptLabel);

        BorderPane sceneContentPane = new BorderPane();
        sceneContentPane.setTop(topPane);
        sceneContentPane.setCenter(choicePanelScroller);

        final Configuration config = ActiveConfigurationContext.getInstance();

        final Translator translator = gazePlay.getTranslator();

        final String whereIsItLabelStyle = "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: white;";
        I18NLabel label = new I18NLabel(translator, "WhereIsItDirectory");
        label.setStyle(whereIsItLabelStyle);
        Button doneButton = new Button(translator.translate("Done"));
        doneButton.getStyleClass().add("gameChooserButton");
        doneButton.getStyleClass().add("gameVariation");
        doneButton.getStyleClass().add("button");
        doneButton.wrapTextProperty().setValue(true);
        doneButton.setAlignment(Pos.CENTER_RIGHT);
        choicePane.getChildren().add(doneButton);
        doneButton.setDisable(true);

        Node input = buildDirectoryChooser(config, configurationContext, translator, ConfigurationContext.DirectoryType.WHERE_IS_IT, doneButton, promptLabel);

        choicePane.getChildren().add(label);
        choicePane.getChildren().add(input);


        EventHandler<Event> event = mouseEvent -> {
            close();
            root.setDisable(false);
            gameMenuController.chooseGame(gazePlay, gameSpec, finalVariant);
        };
        doneButton.addEventHandler(MOUSE_CLICKED, event);

        Scene scene = new Scene(sceneContentPane, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene, gazePlay.getCurrentScreenDimensionSupplier());

        setScene(scene);
        setWidth(primaryStage.getWidth() / 2);
        setHeight(primaryStage.getHeight() / 2);
    }

    Node buildDirectoryChooser(
        Configuration configuration,
        ConfigurationContext configurationContext,
        Translator translator,
        ConfigurationContext.DirectoryType type,
        Button doneButton,
        I18NLabel whereIsItPromptLabel

    ) {
        final HBox pane = new HBox(5);
        final String fileDir;
        Button buttonLoad;

        switch (type) {
            case WHERE_IS_IT:
                fileDir = configuration.getWhereIsItDir();
                break;
            default:
                fileDir = configuration.getFileDir();
        }

        buttonLoad = new Button(fileDir);

        buttonLoad.setOnAction(arg0 -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                final File currentFolder;

                switch (type) {
                    case WHERE_IS_IT:
                        currentFolder = new File(configuration.getWhereIsItDir());
                        break;
                    default:
                        currentFolder = new File(configuration.getFileDir());
                }

                if (currentFolder.isDirectory()) {
                    directoryChooser.setInitialDirectory(currentFolder);
                }
                final GazePlay gazePlay = configurationContext.getGazePlay();
                final Scene scene = gazePlay.getPrimaryScene();
                File file = directoryChooser.showDialog(scene.getWindow());
                if (file == null) {
                    return;
                }

                String newPropertyValue = file.getAbsolutePath();

                if (Utils.isWindows()) {
                    newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
                }

                buttonLoad.textProperty().setValue(newPropertyValue);

                if (newPropertyValue.contains("where-is-it")) {
                    doneButton.setDisable(false);
                    switch (type) {
                        case WHERE_IS_IT:
                            configuration.getWhereIsItDirProperty().setValue(newPropertyValue);
                            break;
                        default:
                            configuration.getFiledirProperty().setValue(newPropertyValue);
                    }
                } else {
                    final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: red;";
                    whereIsItPromptLabel.setText(translator.translate("You picked the wrong directory"));
                    whereIsItPromptLabel.setStyle(labelStyle);
                }
            }
        );
        
        final I18NButton resetButton = new I18NButton(translator, "reset");

        switch (type) {
            case WHERE_IS_IT:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = Configuration.DEFAULT_VALUE_WHEREISIT_DIR;
                        configuration.getWhereIsItDirProperty()
                            .setValue(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
                break;
            default:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath();
                        configuration.getFiledirProperty().setValue(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
        }

        pane.getChildren().addAll(buttonLoad, resetButton);

        return pane;
    }

}
