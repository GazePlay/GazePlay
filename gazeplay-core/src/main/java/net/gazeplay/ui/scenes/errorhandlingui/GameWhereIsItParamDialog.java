package net.gazeplay.ui.scenes.errorhandlingui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

@Slf4j
public class GameWhereIsItParamDialog extends Stage {
    private int currentLevelItem = 0;

    public GameWhereIsItParamDialog(
        final GazePlay gazePlay,
        final GameMenuController gameMenuController,
        final Stage primaryStage,
        final GameSpec gameSpec,
        final Parent root,
        final String whereIsItPromptLabelTextKey,
        final ConfigurationContext configurationContext
    ) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        initStyle(StageStyle.UTILITY);
        setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        final Translator translator = gazePlay.getTranslator();

        FlowPane choicePane = new FlowPane();
        choicePane.setAlignment(Pos.CENTER);
        choicePane.setHgap(10);
        choicePane.setVgap(10);

        ScrollPane choicePanelScroller = new ScrollPane();
        choicePanelScroller.setContent(choicePane);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        choicePanelScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 24;";
        I18NLabel promptLabel = new I18NLabel(translator, whereIsItPromptLabelTextKey);
        promptLabel.setStyle(labelStyle);

        VBox topPane = new VBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(promptLabel);

        BorderPane sceneContentPane = new BorderPane();
        sceneContentPane.setTop(topPane);
        sceneContentPane.setCenter(choicePanelScroller);

        final Configuration config = ActiveConfigurationContext.getInstance();

        final String whereIsItLabelStyle = "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: white;";
        I18NLabel label = new I18NLabel(translator, "WhereIsItParamDirectory");
        label.setStyle(whereIsItLabelStyle);
        Button doneButton = new Button(translator.translate("Done"));
        doneButton.getStyleClass().add("gameChooserButton");
        doneButton.getStyleClass().add("gameVariation");
        doneButton.getStyleClass().add("button");
        doneButton.wrapTextProperty().setValue(true);
        doneButton.setAlignment(Pos.CENTER_RIGHT);

        Node input = buildDirectoryChooser(config, configurationContext, translator, ConfigurationContext.DirectoryType.WHERE_IS_IT, doneButton, label);


        choicePane.getChildren().add(label);
        choicePane.getChildren().add(input);


        EventHandler<Event> event = mouseEvent -> {
            close();
            root.setDisable(false);
            gameMenuController.chooseGame(gazePlay, gameSpec, new IntGameVariant(currentLevelItem));
        };
        doneButton.addEventHandler(MOUSE_CLICKED, event);

        Scene scene = new Scene(sceneContentPane, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene, gazePlay.getCurrentScreenDimensionSupplier());

        setScene(scene);
        setWidth(primaryStage.getWidth() / 2);
        setHeight(primaryStage.getHeight() / 2);
    }

    VBox buildDirectoryChooser(
        Configuration configuration,
        ConfigurationContext configurationContext,
        Translator translator,
        ConfigurationContext.DirectoryType type,
        Button doneButton,
        I18NLabel label

    ) {
        final HBox pane = new HBox(5);
        final String fileDir;
        Button buttonLoad;

        switch (type) {
            case WHERE_IS_IT:
                fileDir = configuration.getWhereIsItParamDir();
                break;
            default:
                fileDir = configuration.getFileDir();
        }

        buttonLoad = new Button(fileDir);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.getStyleClass().add("gameChooserButton");
        resetButton.getStyleClass().add("gameVariation");
        resetButton.getStyleClass().add("button");
        resetButton.wrapTextProperty().setValue(true);
        resetButton.setAlignment(Pos.CENTER);

        switch (type) {
            case WHERE_IS_IT:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = Configuration.DEFAULT_VALUE_WHEREISIT_DIR;
                        configuration.getWhereIsItParamDirProperty()
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
        I18NLabel loadLabel = new I18NLabel(translator, "chooseDirectoryToLoad:");
        final String whereIsItLabelStyle = "-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;";
        loadLabel.setStyle(whereIsItLabelStyle);
        pane.getChildren().addAll(buttonLoad, resetButton);


        ChoiceBox<Integer> levelChooser = new ChoiceBox<>();
        levelChooser.setConverter(new StringConverter<Integer>() {

            @Override
            public String toString(Integer object) {
                return "Level " + (object + 1);
            }

            @Override
            public Integer fromString(String string) {
                return Integer.getInteger(string.split(" ")[1]);
            }
        });

        File questionOrderFile = new File(configuration.getWhereIsItParamDir() + "/questionOrder.csv");
        if (questionOrderFile.exists()) {
            updateLevelSelector(questionOrderFile, levelChooser);
        }

        buttonLoad.setOnAction(arg0 -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                final File currentFolder;

                switch (type) {
                    case WHERE_IS_IT:
                        currentFolder = new File(configuration.getWhereIsItParamDir());
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

                File newQuestionOrderFile = new File(newPropertyValue + "/questionOrder.csv");

                if (getNumberOfValideDirectories(newPropertyValue) == 0) {
                    changeTextLabel(label, translator, "You picked the wrong directory", "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: red;");
                    doneButton.setDisable(true);
                } else if (!(new File(newPropertyValue + "/questions.csv")).exists()) {
                    changeTextLabel(label, translator, "questions.csv is missing", "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: red;");
                    doneButton.setDisable(true);
                } else if (!(newQuestionOrderFile).exists()) {
                    changeTextLabel(label, translator, "questionOrder.csv is missing", "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: red;");
                    doneButton.setDisable(true);
                } else {
                    changeTextLabel(label, translator, "WhereIsItParamDirectory", "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: white;");
                    doneButton.setDisable(false);
                    if (newQuestionOrderFile.exists()) {
                        updateLevelSelector(newQuestionOrderFile, levelChooser);
                    }
                    switch (type) {
                        case WHERE_IS_IT:
                            configuration.getWhereIsItParamDirProperty().setValue(newPropertyValue);
                            break;
                        default:
                            configuration.getFiledirProperty().setValue(newPropertyValue);
                    }
                }
            }
        );

        I18NLabel levelChooserLabel = new I18NLabel(translator, "chooseLevel:");
        levelChooserLabel.setStyle(whereIsItLabelStyle);
        HBox levelselector = new HBox(levelChooser);


        HBox doneButtonBox = new HBox(doneButton);
        doneButton.setAlignment(Pos.CENTER);
        doneButtonBox.setAlignment(Pos.CENTER);

        VBox finalPane = new VBox(loadLabel, pane, levelChooserLabel, levelselector, doneButtonBox);
        finalPane.setSpacing(20);
        finalPane.setTranslateY(20);
        return finalPane;
    }

    public void updateLevelSelector(File questionOrderFile, ChoiceBox<Integer> levelChooser) {
        try (
            InputStream fileInputStream = Files.newInputStream(questionOrderFile.toPath());
            BufferedReader b = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))
        ) {
            levelChooser.getItems().clear();
            String readLine;
            int levelIndex = 0;
            while ((readLine = b.readLine()) != null) {
                levelChooser.getItems().add(levelIndex);
                levelIndex++;
            }

            levelChooser.getSelectionModel().select(0);

            final double PREF_WIDTH = 200;
            final double PREF_HEIGHT = 25;
            levelChooser.setPrefWidth(PREF_WIDTH);
            levelChooser.setPrefHeight(PREF_HEIGHT);

            levelChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    levelChooser.getSelectionModel().select(newValue);
                    setCurrentLevel(newValue);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setCurrentLevel(int level) {
        this.currentLevelItem = level;
        log.info(" LEVEL CHOSE : {}", level);
    }

    private int getNumberOfValideDirectories(String selectedPath) {
        final File imagesDirectory = new File(selectedPath + "/images/");
        int validDirectoriesNumber = 0;
        File[] listOfTheFiles = imagesDirectory.listFiles();
        if (listOfTheFiles != null) {
            for (File f : listOfTheFiles) {
                File[] filesInf = f.listFiles();
                if (filesInf != null) {
                    if (f.isDirectory() && filesInf.length > 0) {
                        boolean containsImage = false;
                        int i = 0;
                        while (!containsImage && i < filesInf.length) {
                            File file = filesInf[i];
                            containsImage = fileIsImageFile(file);
                            i++;
                        }
                        if (containsImage) {
                            validDirectoriesNumber++;
                        }
                    }
                }
            }
        }
        return validDirectoriesNumber;
    }

    static boolean fileIsImageFile(File file) {
        try {
            String mimetype = Files.probeContentType(file.toPath());
            if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                return true;
            }
        } catch (IOException ignored) {

        }
        return false;
    }

    private void changeTextLabel(I18NLabel label, Translator translator, String text, String style) {
        label.setText(translator.translate(text));
        label.setStyle(style);
    }

}
