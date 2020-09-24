package net.gazeplay.ui.scenes.errorhandlingui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

@Slf4j
public class GameWhereIsItConfigurableDialog extends Stage {
    private int currentLevelItem = 0;

    public GameWhereIsItConfigurableDialog(
        final GazePlay gazePlay,
        final GameMenuController gameMenuController,
        final Stage primaryStage,
        final GameSpec gameSpec,
        final Parent root,
        final String whereIsItPromptLabelTextKey
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

        final String whereIsItLabelStyle = "-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: white;";
        Label label = new Label( "");
        label.setStyle(whereIsItLabelStyle);
        label.setAlignment(Pos.CENTER);

        Button doneButton = new Button(translator.translate("Done"));
        doneButton.getStyleClass().add("gameChooserButton");
        doneButton.getStyleClass().add("gameVariation");
        doneButton.getStyleClass().add("button");
        doneButton.wrapTextProperty().setValue(true);
        doneButton.setAlignment(Pos.CENTER_RIGHT);

        Node input = buildDirectoryChooser(config, translator, doneButton, label, gazePlay);

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
        Translator translator,
        Button doneButton,
        Label label,
        GazePlay gazePlay

    ) {
        final HBox pane = new HBox(5);
        final String fileDir;
        Button buttonLoad;

        fileDir = configuration.getWhereIsItConfigurableDir();

        buttonLoad = new Button(fileDir);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.getStyleClass().add("gameChooserButton");
        resetButton.getStyleClass().add("gameVariation");
        resetButton.getStyleClass().add("button");
        resetButton.wrapTextProperty().setValue(true);
        resetButton.setAlignment(Pos.CENTER);

        I18NLabel loadLabel = new I18NLabel(translator, "chooseDirectoryToLoad:");
        final String whereIsItLabelStyle = "-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: white;";
        loadLabel.setStyle(whereIsItLabelStyle);
        buttonLoad.minWidthProperty().bind(loadLabel.widthProperty());
        pane.getChildren().addAll(buttonLoad, resetButton);


        ChoiceBox<Integer> levelChooser = new ChoiceBox<>();
        levelChooser.minWidthProperty().bind(loadLabel.widthProperty());
        levelChooser.disableProperty().bind(doneButton.disabledProperty());
        levelChooser.setConverter(new StringConverter<Integer>() {

            @Override
            public String toString(Integer object) {
                return "Trial " + (object + 1);
            }

            @Override
            public Integer fromString(String string) {
                return Integer.getInteger(string.split(" ")[1]);
            }
        });

        resetButton.setOnAction(
            e -> {
                String defaultValue = Configuration.DEFAULT_VALUE_WHEREISIT_DIR;
                configuration.getWhereIsItConfigurableDirProperty()
                    .setValue(defaultValue);
                buttonLoad.textProperty().setValue(defaultValue);

                File newQuestionOrderFile = new File("");
                updateErrorMessage("", newQuestionOrderFile, levelChooser, configuration, translator, label, doneButton);
            });

        buttonLoad.setOnAction(arg0 -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                final File currentFolder;

                currentFolder = new File(configuration.getWhereIsItConfigurableDir());

                if (currentFolder.isDirectory()) {
                    directoryChooser.setInitialDirectory(currentFolder);
                }
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
                updateErrorMessage(newPropertyValue, newQuestionOrderFile, levelChooser, configuration, translator, label, doneButton);
            }
        );

        File questionOrderFile = new File(configuration.getWhereIsItConfigurableDir() + "/questionOrder.csv");
        updateErrorMessage(configuration.getWhereIsItConfigurableDir(), questionOrderFile, levelChooser, configuration, translator, label, doneButton);


        I18NLabel levelChooserLabel = new I18NLabel(translator, "chooseLevel:");
        levelChooserLabel.setStyle(whereIsItLabelStyle);
        HBox levelselector = new HBox(levelChooser);


        HBox doneButtonBox = new HBox(doneButton);
        doneButton.setAlignment(Pos.CENTER);
        doneButtonBox.setAlignment(Pos.CENTER);

        VBox finalPane = new VBox(label, loadLabel, pane, levelChooserLabel, levelselector, doneButtonBox);
        finalPane.setSpacing(20);
        finalPane.setTranslateY(20);
        return finalPane;
    }

    private void updateErrorMessage(String newPropertyValue, File newQuestionOrderFile, ChoiceBox<Integer> levelChooser, Configuration configuration, Translator translator, Label label, Button doneButton) {
        String errorMessage = "";//getNumberOfValideDirectories(newPropertyValue, configuration, translator);
        if(newPropertyValue.equals("")){
            changeTextLabel(label, "", "-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: white;");
            doneButton.setDisable(true);
        } else if (!(new File(newPropertyValue + "/questions.csv")).exists()) {
            changeTextLabel(label, "questions.csv " + translator.translate("ismissing"), "-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: red;");
            doneButton.setDisable(true);
        } else if (!(newQuestionOrderFile).exists()) {
            changeTextLabel(label, "questionOrder.csv " + translator.translate("ismissing"), "-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: red;");
            doneButton.setDisable(true);
        } else if (!(errorMessage).equals("")) {
            changeTextLabel(label, errorMessage, "-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: red;");
            doneButton.setDisable(true);
        } else {
            changeTextLabel(label, "", "-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: white;");
            doneButton.setDisable(false);
            if (newQuestionOrderFile.exists()) {
                updateLevelSelector(newQuestionOrderFile, levelChooser);
            }
            configuration.getWhereIsItConfigurableDirProperty().setValue(newPropertyValue);
        }
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

            levelChooser.setPrefWidth(200);
            levelChooser.setPrefHeight(25);

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

    private String getNumberOfValideDirectories(String selectedPath, Configuration configuration, Translator translator) {
        final File imagesDirectory = new File(selectedPath + "/images/");
        int validDirectoriesNumber = 0;
        List<String> imagesFolders = new LinkedList<>();
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
                            imagesFolders.add(f.getName());
                        }
                    }
                }
            }
        }

        if (validDirectoriesNumber == 0) {
            return translator.translate("NoImageFound");
        }

        File questionOrderFile = new File(configuration.getWhereIsItConfigurableDir() + "/questionOrder.csv");
        try (
            InputStream fileInputStream = Files.newInputStream(questionOrderFile.toPath());
            BufferedReader b = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))
        ) {
            String readLine;
            int line = 1;
            while ((readLine = b.readLine()) != null) {
                String[] split = readLine.split(",");
                if (split.length <= 3) {
                    return "'questionOrder.csv' " + translator.translate("Line") + " " + line + ": " + translator.translate("LineIsEmpty");
                }
                String answer = split[0];
                try {
                    int numberOfImages = Integer.parseInt(split[ 1]) * Integer.parseInt( split[2]);
                    if (numberOfImages <= split.length - 5) {
                        return "'questionOrder.csv' " + translator.translate("Line") + " " + line + ": " + getFinalSentence(translator, "LineNeedEltButGot", numberOfImages, (split.length - 3));
                    }
                    boolean correctImageFound = false;
                    for (int i = 2; i < split.length && i < numberOfImages + 3; i++) {
                        if (split[i].equals(split[0])) {
                            correctImageFound = true;
                        }
                        if (!(split[i] == null) && !split[i].equals("") && split[i].toString().length()!=0 ) {
                            int j = 0;
                            while (j < imagesFolders.size() && !imagesFolders.get(j).equals(split[i])) {
                                j++;
                            }
                            if (j >= imagesFolders.size()) {
                                return "'questionOrder.csv' " + translator.translate("Line") + " " + line + ": " + getFinalSentence(translator, "ImageDirectoryIsMissing", split[i]);
                            }
                        }
                    }
                    if (!correctImageFound) {
                        return "'questionOrder.csv' " + translator.translate("Line") + " " + line + ": " + getFinalSentence(translator, "CorrectAnswerIsMissingInList", split[0]);
                    }
                } catch (NumberFormatException e) {
                    return "'questionOrder.csv' " + translator.translate("Line") + " " + line + ": " + translator.translate("TheLastElementsShouldBeLineAndCol");
                }
                line++;
            }
        } catch (IOException e) {
            return "";
        }

        return "";
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

    private void changeTextLabel(Label label, String text, String style) {
        label.setText(text);
        label.setStyle(style);
    }


    private String getFinalSentence(Translator translator, String var1, Object... var2) {
        String tempVar1 = translator.translate(var1);
        for (int i = 0; i < var2.length; i++) {
            tempVar1 = tempVar1.replaceFirst("\\{}", var2[i].toString());
        }
        return tempVar1;
    }

}
