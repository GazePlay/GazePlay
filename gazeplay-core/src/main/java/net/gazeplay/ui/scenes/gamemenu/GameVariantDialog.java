
package net.gazeplay.ui.scenes.gamemenu;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.*;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.gaze.devicemanager.TobiiGazeDeviceManager;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.components.ProgressButton;
import net.gazeplay.ui.scenes.errorhandlingui.GameWhereIsItErrorPathDialog;

import javafx.util.Duration;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
public class GameVariantDialog extends Stage {
    private int easyMode = 0;
    private TextField numberField;
    private final Configuration config;

    ToggleGroup group = new ToggleGroup();
    ProgressButton[] categories;
    ScrollPane choicePanelScroller;
    HashMap<Integer, FlowPane> choicePanes;

    public GameVariantDialog(
        final GazePlay gazePlay,
        final GameMenuController gameMenuController,
        final Stage primaryStage,
        final GameSpec gameSpec,
        final Parent root,
        final String chooseVariantPromptLabelTextKey
    ) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        initStyle(StageStyle.UNDECORATED);
        setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        this.config = ActiveConfigurationContext.getInstance();
        choicePanes = new HashMap<>();
        choicePanes.put(0, createFlowPane());

        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        choicePanelScroller = new ScrollPane();
        choicePanelScroller.setContent(choicePanes.get(0));
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        choicePanelScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        centerPane.getChildren().add(choicePanelScroller);

        if(gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")){
            HBox numberBox = new HBox();
            numberBox.setSpacing(20);
            numberBox.setAlignment(Pos.CENTER);
            centerPane.getChildren().add(numberBox);

            I18NLabel numberLabel = new I18NLabel(gazePlay.getTranslator(), "NumberOfTrains");
            numberBox.getChildren().add(numberLabel);

            numberField = new TextField();
            numberField.setText("10");
            numberBox.getChildren().add(numberField);
        }

        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 36; -fx-text-fill: black; -fx-padding: 60 0 0 0";

        I18NLabel chooseVariantPromptLabel = new I18NLabel(gazePlay.getTranslator(), chooseVariantPromptLabelTextKey);
        Label titleVariant = new Label(chooseVariantPromptLabel.getText() + " " + gameSpec.getGameSummary().getNameCode());
        titleVariant.setStyle(labelStyle);

        GridPane gridPane = new GridPane();
        GridPane.setHgrow(titleVariant, Priority.ALWAYS);
        GridPane.setHalignment(titleVariant, HPos.CENTER);
        gridPane.add(this.createReturnButton(primaryStage, root), 1, 0);
        gridPane.add(titleVariant, 0, 0);

        VBox topPane = new VBox();
        topPane.setAlignment(Pos.TOP_RIGHT);
        topPane.getChildren().add(gridPane);

        BorderPane sceneContentPane = new BorderPane();
        sceneContentPane.setTop(topPane);
        sceneContentPane.setCenter(centerPane);

        final Translator translator = gazePlay.getTranslator();

        HBox bottom = new HBox();
        bottom.prefWidthProperty().bind(sceneContentPane.widthProperty());
        bottom.setAlignment(Pos.CENTER);
        bottom.setSpacing(50);
        bottom.setPadding(new Insets(0, 0, 50, 0));

        for (IGameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {

            GameButtonPane button = new GameButtonPane(gameSpec);
            button.getStyleClass().add("gameChooserButton");
            button.getStyleClass().add("gameVariation");
            button.getStyleClass().add("button");
            button.setCenter(new Label(variant.getLabel(translator)));

            button.setMinWidth(primaryStage.getWidth() / 15);
            button.setMinHeight(primaryStage.getHeight() / 15);

            button.setPrefWidth(primaryStage.getWidth() / 10);
            button.setPrefHeight(primaryStage.getHeight() / 10);

            button.setMaxWidth(primaryStage.getWidth() / 8);
            button.setMaxHeight(primaryStage.getHeight() / 8);

            int indexOfTheVariant = 0;
            if ((gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) && variant instanceof IntStringGameVariant eggVariant){
                button.setCenter(new Label(String.valueOf(eggVariant.getNumber())));
            }

            if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound")
            ) {
                String variantString = ((DimensionDifficultyGameVariant) variant).getVariant();
                indexOfTheVariant = switch (variantString) {
                    case "Easy", "Vowels", "Farm", "Animals", "MostFamous" -> 0;
                    case "Normal", "Consonants", "Forest", "Instruments", "Africa" -> 1;
                    case "Hard", "AllLetters", "Savanna", "AllSounds", "America" -> 2;
                    case "Birds", "Asia" -> 3;
                    case "Maritime", "Europe" -> 4;
                    case "AllAnimals", "AllFlags" -> 5;
                    case "Dynamic" -> 6;
                    default -> -1;
                };
            } else if (variant.toString().contains("DYNAMIC") || variant.toString().contains("Dynamic")){
                indexOfTheVariant = 1;
            } else if(gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")){
                int variantNumber = ((IntStringGameVariant) variant).getNumber();
                indexOfTheVariant = switch (variantNumber){
                    case 3 -> 0;
                    case 8 -> 1;
                    case 13 -> 2;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("RockPaperScissors")) {
                indexOfTheVariant = variant.toString().toLowerCase().contains("hide") ? 0 : 1;
            } else if (gameSpec.getGameSummary().getNameCode().equals("Labyrinth")) {
                indexOfTheVariant = variant.toString().toLowerCase().contains("other") ? 1 : 0;
            } else if(gameSpec.getGameSummary().getNameCode().equals("RushHour")){
                int variantString = ((IntGameVariant) variant).getNumber();
                indexOfTheVariant = switch(variantString){
                    case 30,31,32,33-> 5;
                    case 24,25,26,27,28,29 -> 4;
                    case 18,19,20,21,22,23 -> 3;
                    case 12,13,14,15,16,17 -> 2;
                    case 6,7,8,9,10,11 -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("Simon")){
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                indexOfTheVariant = switch(variantString){
                    case "MODE2" -> 1;
                    case "MODE3" -> 2;
                    default -> 0;
                };
            } else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinish")){
                int variantString = ((IntGameVariant) variant).getNumber();
                indexOfTheVariant = switch(variantString){
                    case 30,31,32,33-> 5;
                    case 24,25,26,27,28,29 -> 4;
                    case 18,19,20,21,22,23 -> 3;
                    case 12,13,14,15,16,17 -> 2;
                    case 6,7,8,9,10,11 -> 1;
                    default -> 0;
                };
            } else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinishMouse")){
                int variantString = ((IntGameVariant) variant).getNumber();
                indexOfTheVariant = switch(variantString){
                    case 30,31,32,33-> 5;
                    case 24,25,26,27,28,29 -> 4;
                    case 18,19,20,21,22,23 -> 3;
                    case 12,13,14,15,16,17 -> 2;
                    case 6,7,8,9,10,11 -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("Bottle")) {
                String variantString = ((IntStringGameVariant) variant).getStringValue();
                indexOfTheVariant = switch (variantString) {
                    case "InfinityBottles" -> 5;
                    case "BigBottles" -> 4;
                    case "HighBottles" -> 3;
                    case "NormalBottles" -> 2;
                    case "SmallBottles" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) {
                String variantString = ((IntStringGameVariant) variant).getStringValue();
                indexOfTheVariant = switch (variantString) {
                    case "ImageShrink" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots")){
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                indexOfTheVariant = switch (variantString) {
                    case "DIFFICULTY_EASY_AUTO_KEYBOARD", "DIFFICULTY_NORMAL_AUTO_KEYBOARD", "DIFFICULTY_HARD_AUTO_KEYBOARD" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse")){
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                System.out.println("variant robots: " + variantString);
                indexOfTheVariant = switch (variantString) {
                    case "DIFFICULTY_EASY_AUTO_MOUSE", "DIFFICULTY_NORMAL_AUTO_MOUSE", "DIFFICULTY_HARD_AUTO_MOUSE" -> 1;
                    default -> 0;
                };
            } else if (variant instanceof IntStringGameVariant) {
                indexOfTheVariant = ((IntStringGameVariant) variant).getNumber();
            }

            if (!choicePanes.containsKey(indexOfTheVariant)) {
                choicePanes.put(indexOfTheVariant, createFlowPane());
            }
            choicePanes.get(indexOfTheVariant).getChildren().add(button);

            System.out.println("nameGame : "+ gameSpec.getGameSummary().getNameCode());

            if ((gameSpec.getGameSummary().getNameCode().equals("Bottle") ||
                gameSpec.getGameSummary().getNameCode().equals("EggGame") ||
                gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame") ||
                gameSpec.getGameSummary().getNameCode().equals("RushHour") ||
                gameSpec.getGameSummary().getNameCode().equals("SprintFinish") ||
                gameSpec.getGameSummary().getNameCode().equals("SprintFinishMouse") ||
                gameSpec.getGameSummary().getNameCode().equals("DotToDot") ||
                gameSpec.getGameSummary().getNameCode().equals("Labyrinth") ||
                gameSpec.getGameSummary().getNameCode().contains("Memory") ||
                gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots") ||
                gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse") ||
                gameSpec.getGameSummary().getNameCode().equals("Ninja") ||
                gameSpec.getGameSummary().getNameCode().equals("Simon") ||
                gameSpec.getGameSummary().getNameCode().equals("RockPaperScissors") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound") ||
                gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")) &&
                group.getToggles().size() < 2
            ) {

                if (gameSpec.getGameSummary().getNameCode().equals("Bottle")) {
                    categories = new ProgressButton[6];
                    categories[0] = this.createCategoriesButton(translator.translate("TinySizeCategory"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("SmallSizeCategory"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("NormalSizeCategory"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("HighSizeCategory"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("BigSizeCategory"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("InfinityCategory"), 5);
                }else if (gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Classic"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("ImageShrink"), 1);
                } else if(gameSpec.getGameSummary().getNameCode().equals("RushHour")){
                    categories = new ProgressButton[6];
                    categories[0] = this.createCategoriesButton(translator.translate("Niveau1-5"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Niveau6-11"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Niveau12-17"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("Niveau18-23"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("Niveau24-29"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("Niveau30-33"), 5);
                }else if (gameSpec.getGameSummary().getNameCode().equals("Simon")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton("Classic", 0);
                    categories[1] = this.createCategoriesButton("Simon Copy", 1);
                    categories[2] = this.createCategoriesButton("Multiplayer", 2);
                }else if(gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots")){
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton("Normal", 0);
                    categories[1] = this.createCategoriesButton("Auto", 1);
                } else if(gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse")){
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton("Normal", 0);
                    categories[1] = this.createCategoriesButton("Auto", 1);
                }else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinish")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Niveau1-5"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Niveau6-11"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Niveau12-17"), 2);
                }else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinishMouse")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Niveau1-5"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Niveau6-11"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Niveau12-17"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("DotToDot") ||
                    gameSpec.getGameSummary().getNameCode().contains("Memory") ||
                    gameSpec.getGameSummary().getNameCode().equals("Ninja")
                ) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Static"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Dynamic"), 1);
                } else if(gameSpec.getGameSummary().getNameCode().equals("Labyrinth")) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("MouseCategory"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("OtherCategory"), 1);
                } else if (gameSpec.getGameSummary().getNameCode().equals("RockPaperScissors")) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Hide"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Visible"), 1);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal")) {
                    categories = new ProgressButton[7];
                    categories[0] = this.createCategoriesButton(translator.translate("Farm"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Forest"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Savanna"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("Birds"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("Maritime"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("AllAnimals"), 5);
                    categories[6] = this.createCategoriesButton(translator.translate("Dynamic"), 6);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                    gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape")
                ) {
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Easy"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Normal"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Hard"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag")) {
                    categories = new ProgressButton[6];
                    categories[0] = this.createCategoriesButton(translator.translate("MostFamous"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Africa"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("America"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("Asia"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("Europe"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("AllFlags"), 5);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter")) {
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Vowels"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Consonants"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("AllLetters"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound")) {
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Animals"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Instruments"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("AllSounds"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton("3 "+translator.translate("Stations"), 0);
                    categories[1] = this.createCategoriesButton("8 "+translator.translate("Stations"), 1);
                    categories[2] = this.createCategoriesButton("13 "+translator.translate("Stations"), 2);
                } else {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Classic"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("HighContrasts"), 1);
                }

                HBox categoriesBox = new HBox();
                categoriesBox.setAlignment(Pos.CENTER);
                final ImageView iv = new ImageView(new Image("data/common/images/selected.png"));
                iv.maxHeight(40);
                iv.maxWidth(40);

                for (ProgressButton category : categories) {
                    category.getButton().setRadius(40);
                    categoriesBox.getChildren().addAll(category, new Label(category.getName()), new Label("     "));
                }

                categories[0].setImage(iv);
                sceneContentPane.setBottom(categoriesBox);
            }

            ProgressIndicator progressIndicator = new ProgressIndicator(0);
            progressIndicator.setMinWidth(80);
            progressIndicator.setMinHeight(80);
            progressIndicator.setStyle(" -fx-progress-color: " + config.getProgressBarColor());

            Timeline timelineProgressBar = new Timeline();

            EventHandler<Event> eventClicked = MouseEvent -> {
                close();
                root.setDisable(false);
                if (config.getWhereIsItDir().isEmpty() && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                    whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant);
                } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")) {
                    IntStringGameVariant v = (IntStringGameVariant) variant;
                    int numberOfTrains = Integer.parseInt(numberField.getText());
                    System.out.println("Nombre de trains"+numberOfTrains);
                    v.setNumber2(numberOfTrains);
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, v);
                } else {
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
                }
            };

            EventHandler<Event> eventEntered = GazeEvent -> {
                button.setTop(new Label(variant.getLabel(translator)));
                button.setCenter(progressIndicator);
                progressIndicator.setProgress(0.0);
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(2000),
                    new KeyValue(progressIndicator.progressProperty(), 1)));
                timelineProgressBar.setOnFinished(actionEvent -> {
                    timelineProgressBar.stop();
                    close();
                    root.setDisable(false);
                    if (config.getWhereIsItDir().isEmpty() && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                        whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant);
                    } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")) {
                        IntStringGameVariant v = (IntStringGameVariant) variant;
                        int numberOfTrains = Integer.parseInt(numberField.getText());
                        System.out.println("Nombre de trains"+numberOfTrains);
                        v.setNumber2(numberOfTrains);
                        gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, v);
                    } else {
                        gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
                    }
                });
                timelineProgressBar.play();
            };

            EventHandler<Event> eventExited = GazeEvent -> {
                timelineProgressBar.stop();
                button.getChildren().clear();
                button.setCenter(new Label(variant.getLabel(translator)));
                if ((gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) && variant instanceof IntStringGameVariant eggVariant){
                    button.setCenter(new Label(String.valueOf(eggVariant.getNumber())));
                }
            };

            button.addEventHandler(MouseEvent.MOUSE_CLICKED, eventClicked);
            button.addEventHandler(GazeEvent.GAZE_ENTERED, eventEntered);
            button.addEventHandler(GazeEvent.GAZE_EXITED, eventExited);
        }

        Scene scene = new Scene(sceneContentPane, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene, gazePlay.getCurrentScreenDimensionSupplier());

        setScene(scene);
        setWidth(primaryStage.getWidth() / 2);
        setHeight(primaryStage.getHeight() / 2);
    }

    public GameVariantDialog(
        final GazePlay gazePlay,
        final GameMenuController gameMenuController,
        final Stage primaryStage,
        final GameSpec gameSpec,
        final Parent root,
        final String chooseVariantPromptLabelTextKey,
        TobiiGazeDeviceManager tobiiGazeDeviceManager,
        GameMenuFactory gameMenuFactory
    ) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);
        initStyle(StageStyle.UTILITY);
        setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        this.config = ActiveConfigurationContext.getInstance();
        choicePanes = new HashMap<>();
        choicePanes.put(0, createFlowPane());

        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        choicePanelScroller = new ScrollPane();
        choicePanelScroller.setContent(choicePanes.get(0));
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        choicePanelScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        centerPane.getChildren().add(choicePanelScroller);

        if(gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")){
            HBox numberBox = new HBox();
            numberBox.setSpacing(20);
            numberBox.setAlignment(Pos.CENTER);
            centerPane.getChildren().add(numberBox);

            I18NLabel numberLabel = new I18NLabel(gazePlay.getTranslator(), "NumberOfTrains");
            numberBox.getChildren().add(numberLabel);

            numberField = new TextField();
            numberField.setText("10");
            numberBox.getChildren().add(numberField);
        }

        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: black;";

        I18NLabel chooseVariantPromptLabel = new I18NLabel(gazePlay.getTranslator(), chooseVariantPromptLabelTextKey);
        Label titleVariant = new Label(chooseVariantPromptLabel.getText() + " " + gameSpec.getGameSummary().getNameCode());
        titleVariant.setStyle(labelStyle);
        ProgressButton returnHome = this.createReturnButton(primaryStage, root, gameMenuFactory);
        tobiiGazeDeviceManager.addEventFilter(returnHome);

        GridPane gridPane = new GridPane();
        GridPane.setHgrow(titleVariant, Priority.ALWAYS);
        GridPane.setHalignment(titleVariant, HPos.CENTER);
        gridPane.add(returnHome, 1, 0);
        gridPane.add(titleVariant, 0, 0);

        VBox topPane = new VBox();
        topPane.setAlignment(Pos.TOP_RIGHT);
        topPane.getChildren().add(gridPane);

        BorderPane sceneContentPane = new BorderPane();
        sceneContentPane.setTop(topPane);
        sceneContentPane.setCenter(centerPane);

        final Translator translator = gazePlay.getTranslator();

        HBox bottom = new HBox();
        bottom.prefWidthProperty().bind(sceneContentPane.widthProperty());
        bottom.setAlignment(Pos.CENTER);
        bottom.setSpacing(50);

        for (IGameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {

            GameButtonPane button = new GameButtonPane(gameSpec);
            button.getStyleClass().add("gameChooserButton");
            button.getStyleClass().add("gameVariation");
            button.getStyleClass().add("button");
            button.setCenter(new Label(variant.getLabel(translator)));

            button.setMinWidth(primaryStage.getWidth() / 15);
            button.setMinHeight(primaryStage.getHeight() / 15);

            button.setPrefWidth(primaryStage.getWidth() / 10);
            button.setPrefHeight(primaryStage.getHeight() / 10);

            button.setMaxWidth(primaryStage.getWidth() / 8);
            button.setMaxHeight(primaryStage.getHeight() / 8);

            int indexOfTheVariant = 0;
            if ((gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) && variant instanceof IntStringGameVariant eggVariant){
                button.setCenter(new Label(String.valueOf(eggVariant.getNumber())));
            }

            if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound")
            ) {
                String variantString = ((DimensionDifficultyGameVariant) variant).getVariant();
                indexOfTheVariant = switch (variantString) {
                    case "Easy", "Vowels", "Farm", "Animals", "MostFamous" -> 0;
                    case "Normal", "Consonants", "Forest", "Instruments", "Africa" -> 1;
                    case "Hard", "AllLetters", "Savanna", "AllSounds", "America" -> 2;
                    case "Birds", "Asia" -> 3;
                    case "Maritime", "Europe" -> 4;
                    case "AllAnimals", "AllFlags" -> 5;
                    case "Dynamic" -> 6;
                    default -> -1;
                };
            } else if (variant.toString().contains("DYNAMIC") || variant.toString().contains("Dynamic")){
                indexOfTheVariant = 1;
            } else if(gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")){
                int variantNumber = ((IntStringGameVariant) variant).getNumber();
                indexOfTheVariant = switch (variantNumber){
                    case 3 -> 0;
                    case 8 -> 1;
                    case 13 -> 2;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("RockPaperScissors")) {
                indexOfTheVariant = variant.toString().toLowerCase().contains("hide") ? 0 : 1;
            } else if (gameSpec.getGameSummary().getNameCode().equals("Labyrinth")) {
                indexOfTheVariant = variant.toString().toLowerCase().contains("other") ? 1 : 0;
            } else if(gameSpec.getGameSummary().getNameCode().equals("RushHour")){
                int variantString = ((IntGameVariant) variant).getNumber();
                indexOfTheVariant = switch(variantString){
                    case 30,31,32,33-> 5;
                    case 24,25,26,27,28,29 -> 4;
                    case 18,19,20,21,22,23 -> 3;
                    case 12,13,14,15,16,17 -> 2;
                    case 6,7,8,9,10,11 -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("Simon")){
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                indexOfTheVariant = switch(variantString){
                    case "MODE2" -> 1;
                    case "MODE3" -> 2;
                    default -> 0;
                };
            } else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinish")){
                int variantString = ((IntGameVariant) variant).getNumber();
                indexOfTheVariant = switch(variantString){
                    case 30,31,32,33-> 5;
                    case 24,25,26,27,28,29 -> 4;
                    case 18,19,20,21,22,23 -> 3;
                    case 12,13,14,15,16,17 -> 2;
                    case 6,7,8,9,10,11 -> 1;
                    default -> 0;
                };
            } else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinishMouse")){
                int variantString = ((IntGameVariant) variant).getNumber();
                indexOfTheVariant = switch(variantString){
                    case 30,31,32,33-> 5;
                    case 24,25,26,27,28,29 -> 4;
                    case 18,19,20,21,22,23 -> 3;
                    case 12,13,14,15,16,17 -> 2;
                    case 6,7,8,9,10,11 -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("Bottle")) {
                String variantString = ((IntStringGameVariant) variant).getStringValue();
                indexOfTheVariant = switch (variantString) {
                    case "InfinityBottles" -> 5;
                    case "BigBottles" -> 4;
                    case "HighBottles" -> 3;
                    case "NormalBottles" -> 2;
                    case "SmallBottles" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) {
                String variantString = ((IntStringGameVariant) variant).getStringValue();
                indexOfTheVariant = switch (variantString) {
                    case "ImageShrink" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots")){
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                indexOfTheVariant = switch (variantString) {
                    case "DIFFICULTY_EASY_AUTO_KEYBOARD", "DIFFICULTY_NORMAL_AUTO_KEYBOARD", "DIFFICULTY_HARD_AUTO_KEYBOARD" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse")){
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                System.out.println("variant robots: " + variantString);
                indexOfTheVariant = switch (variantString) {
                    case "DIFFICULTY_EASY_AUTO_MOUSE", "DIFFICULTY_NORMAL_AUTO_MOUSE", "DIFFICULTY_HARD_AUTO_MOUSE" -> 1;
                    default -> 0;
                };
            } else if (variant instanceof IntStringGameVariant) {
                indexOfTheVariant = ((IntStringGameVariant) variant).getNumber();
            }

            if (!choicePanes.containsKey(indexOfTheVariant)) {
                choicePanes.put(indexOfTheVariant, createFlowPane());
            }
            choicePanes.get(indexOfTheVariant).getChildren().add(button);

            System.out.println("nameGame : "+ gameSpec.getGameSummary().getNameCode());

            if ((gameSpec.getGameSummary().getNameCode().equals("Bottle") ||
                gameSpec.getGameSummary().getNameCode().equals("EggGame") ||
                gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame") ||
                gameSpec.getGameSummary().getNameCode().equals("RushHour") ||
                gameSpec.getGameSummary().getNameCode().equals("SprintFinish") ||
                gameSpec.getGameSummary().getNameCode().equals("SprintFinishMouse") ||
                gameSpec.getGameSummary().getNameCode().equals("DotToDot") ||
                gameSpec.getGameSummary().getNameCode().equals("Labyrinth") ||
                gameSpec.getGameSummary().getNameCode().contains("Memory") ||
                gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots") ||
                gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse") ||
                gameSpec.getGameSummary().getNameCode().equals("Ninja") ||
                gameSpec.getGameSummary().getNameCode().equals("Simon") ||
                gameSpec.getGameSummary().getNameCode().equals("RockPaperScissors") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound") ||
                gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")) &&
                group.getToggles().size() < 2
            ) {

                if (gameSpec.getGameSummary().getNameCode().equals("Bottle")) {
                    categories = new ProgressButton[6];
                    categories[0] = this.createCategoriesButton(translator.translate("TinySizeCategory"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("SmallSizeCategory"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("NormalSizeCategory"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("HighSizeCategory"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("BigSizeCategory"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("InfinityCategory"), 5);
                }else if (gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Classic"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("ImageShrink"), 1);
                } else if(gameSpec.getGameSummary().getNameCode().equals("RushHour")){
                    categories = new ProgressButton[6];
                    categories[0] = this.createCategoriesButton(translator.translate("Niveau1-5"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Niveau6-11"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Niveau12-17"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("Niveau18-23"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("Niveau24-29"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("Niveau30-33"), 5);
                }else if (gameSpec.getGameSummary().getNameCode().equals("Simon")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton("Classic", 0);
                    categories[1] = this.createCategoriesButton("Simon Copy", 1);
                    categories[2] = this.createCategoriesButton("Multiplayer", 2);
                }else if(gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots")){
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton("Normal", 0);
                    categories[1] = this.createCategoriesButton("Auto", 1);
                } else if(gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse")){
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton("Normal", 0);
                    categories[1] = this.createCategoriesButton("Auto", 1);
                }else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinish")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Niveau1-5"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Niveau6-11"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Niveau12-17"), 2);
                }else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinishMouse")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Niveau1-5"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Niveau6-11"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Niveau12-17"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("DotToDot") ||
                    gameSpec.getGameSummary().getNameCode().contains("Memory") ||
                    gameSpec.getGameSummary().getNameCode().equals("Ninja")
                ) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Static"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Dynamic"), 1);
                } else if(gameSpec.getGameSummary().getNameCode().equals("Labyrinth")) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("MouseCategory"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("OtherCategory"), 1);
                } else if (gameSpec.getGameSummary().getNameCode().equals("RockPaperScissors")) {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Hide"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Visible"), 1);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal")) {
                    categories = new ProgressButton[7];
                    categories[0] = this.createCategoriesButton(translator.translate("Farm"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Forest"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Savanna"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("Birds"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("Maritime"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("AllAnimals"), 5);
                    categories[6] = this.createCategoriesButton(translator.translate("Dynamic"), 6);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                    gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape")
                ) {
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Easy"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Normal"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("Hard"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag")) {
                    categories = new ProgressButton[6];
                    categories[0] = this.createCategoriesButton(translator.translate("MostFamous"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Africa"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("America"), 2);
                    categories[3] = this.createCategoriesButton(translator.translate("Asia"), 3);
                    categories[4] = this.createCategoriesButton(translator.translate("Europe"), 4);
                    categories[5] = this.createCategoriesButton(translator.translate("AllFlags"), 5);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter")) {
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Vowels"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Consonants"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("AllLetters"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound")) {
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton(translator.translate("Animals"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("Instruments"), 1);
                    categories[2] = this.createCategoriesButton(translator.translate("AllSounds"), 2);
                } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")){
                    categories = new ProgressButton[3];
                    categories[0] = this.createCategoriesButton("3 "+translator.translate("Stations"), 0);
                    categories[1] = this.createCategoriesButton("8 "+translator.translate("Stations"), 1);
                    categories[2] = this.createCategoriesButton("13 "+translator.translate("Stations"), 2);
                } else {
                    categories = new ProgressButton[2];
                    categories[0] = this.createCategoriesButton(translator.translate("Classic"), 0);
                    categories[1] = this.createCategoriesButton(translator.translate("HighContrasts"), 1);
                }

                HBox categoriesBox = new HBox();
                categoriesBox.setAlignment(Pos.CENTER);
                final ImageView iv = new ImageView(new Image("data/common/images/selected.png"));
                iv.maxHeight(40);
                iv.maxWidth(40);

                for (ProgressButton category : categories) {
                    category.getButton().setRadius(40);
                    categoriesBox.getChildren().addAll(category, new Label(category.getName()), new Label("     "));
                    tobiiGazeDeviceManager.addEventFilter(category);
                }

                categories[0].setImage(iv);
                sceneContentPane.setBottom(categoriesBox);
            }

            ProgressIndicator progressIndicator = new ProgressIndicator(0);
            progressIndicator.setMinWidth(80);
            progressIndicator.setMinHeight(80);
            progressIndicator.setStyle(" -fx-progress-color: " + config.getProgressBarColor());

            Timeline timelineProgressBar = new Timeline();

            EventHandler<Event> eventClicked = MouseEvent -> {
                close();
                root.setDisable(false);
                if (config.getWhereIsItDir().isEmpty() && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                    whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant);
                } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")) {
                    IntStringGameVariant v = (IntStringGameVariant) variant;
                    int numberOfTrains = Integer.parseInt(numberField.getText());
                    System.out.println("Nombred de trains"+numberOfTrains);
                    v.setNumber2(numberOfTrains);
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, v);
                } else {
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
                }
            };

            EventHandler<Event> eventEntered = GazeEvent -> {
                button.setTop(new Label(variant.getLabel(translator)));
                button.setCenter(progressIndicator);
                progressIndicator.setProgress(0.0);
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(2000),
                    new KeyValue(progressIndicator.progressProperty(), 1)));
                timelineProgressBar.setOnFinished(actionEvent -> {
                    timelineProgressBar.stop();
                    close();
                    root.setDisable(false);
                    if (config.getWhereIsItDir().isEmpty() && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                        whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant);
                    } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")) {
                        IntStringGameVariant v = (IntStringGameVariant) variant;
                        int numberOfTrains = Integer.parseInt(numberField.getText());
                        System.out.println("Nombred de trains"+numberOfTrains);
                        v.setNumber2(numberOfTrains);
                        gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, v);
                    } else {
                        gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
                    }
                    gameMenuFactory.inGameVariant = false;
                });
                timelineProgressBar.play();
            };

            EventHandler<Event> eventExited = GazeEvent -> {
                timelineProgressBar.stop();
                button.getChildren().clear();
                button.setCenter(new Label(variant.getLabel(translator)));
                if ((gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) && variant instanceof IntStringGameVariant eggVariant){
                    button.setCenter(new Label(String.valueOf(eggVariant.getNumber())));
                }
            };
            tobiiGazeDeviceManager.addEventFilter(button);
            button.addEventHandler(MouseEvent.MOUSE_CLICKED, eventClicked);
            button.addEventHandler(GazeEvent.GAZE_ENTERED, eventEntered);
            button.addEventHandler(GazeEvent.GAZE_EXITED, eventExited);
        }

        Scene scene = new Scene(sceneContentPane, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene, gazePlay.getCurrentScreenDimensionSupplier());

        setScene(scene);
        setWidth(primaryStage.getWidth() / 2);
        setHeight(primaryStage.getHeight() / 2);
    }

    private void whereIsItErrorHandling(GazePlay gazePlay, GameMenuController gameMenuController, GameSpec gameSpec, Parent root, IGameVariant finalVariant) {
        String whereIsItPromptLabel = "WhereIsItNot Config Directory";
        GameWhereIsItErrorPathDialog errorDialog = new GameWhereIsItErrorPathDialog(gazePlay, gameMenuController, gazePlay.getPrimaryStage(), gameSpec, root, whereIsItPromptLabel, finalVariant);
        errorDialog.setTitle("error");
        errorDialog.show();
        errorDialog.toFront();
    }

    private FlowPane createFlowPane() {
        FlowPane newFlowPane = new FlowPane();
        newFlowPane.setAlignment(Pos.CENTER);
        newFlowPane.setHgap(10);
        newFlowPane.setVgap(10);
        return newFlowPane;
    }

    private ProgressButton createReturnButton(Stage primaryStage, Parent root){
        final EventHandler<Event> buttonHandler;
        final ProgressButton bt = new ProgressButton();
        final ImageView iv = new ImageView(new Image("data/common/images/return.png"));
        iv.maxHeight(40);
        iv.maxWidth(40);
        bt.setImage(iv);
        bt.getButton().setRadius(50);
        buttonHandler = e -> {
            close();
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        };
        bt.addEventHandler(MouseEvent.MOUSE_CLICKED, buttonHandler);
        bt.assignIndicatorUpdatable(buttonHandler, this.config);
        if (!Objects.equals(config.getEyeTracker(), "mouse_control")){
            bt.active();
        }
        return bt;
    }
    private ProgressButton createReturnButton(Stage primaryStage, Parent root, GameMenuFactory gameMenuFactory){
        final EventHandler<Event> buttonHandler;
        final ProgressButton bt = new ProgressButton();
        final ImageView iv = new ImageView(new Image("data/common/images/return.png"));
        iv.maxHeight(40);
        iv.maxWidth(40);
        bt.setImage(iv);
        bt.getButton().setRadius(50);
        buttonHandler = e -> {
            gameMenuFactory.inGameVariant = false;
            close();
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        };
        bt.addEventHandler(MouseEvent.MOUSE_CLICKED, buttonHandler);
        bt.assignIndicatorUpdatable(buttonHandler, this.config);
        if (!Objects.equals(config.getEyeTracker(), "mouse_control")){
            bt.active();
        }
        return bt;
    }

    private ProgressButton createCategoriesButton(String name, int index){
        final EventHandler<Event> buttonHandler;
        final ProgressButton bt = new ProgressButton(name);
        bt.getButton().setRadius(40);

        final ImageView iv = new ImageView(new Image("data/common/images/selected.png"));
        iv.maxHeight(40);
        iv.maxWidth(40);

        buttonHandler = e -> {
            for (ProgressButton category : categories){
                category.setImage(new ImageView());
            }
            bt.setImage(iv);
            if (easyMode != index) {
                easyMode = index;
            }
            choicePanelScroller.setContent(choicePanes.get(index));
        };
        bt.addEventHandler(MouseEvent.MOUSE_CLICKED, buttonHandler);
        bt.assignIndicatorUpdatable(buttonHandler, this.config);
        if (!Objects.equals(config.getEyeTracker(), "mouse_control")){
            bt.active();
        }
        return bt;
    }
}
