
package net.gazeplay.ui.scenes.gamemenu;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.*;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.menus.MenuDialog;
import net.gazeplay.ui.scenes.errorhandlingui.GameWhereIsItErrorPathDialog;
import java.util.HashMap;

/**
 * Window of the variants of a game, opened from the menu: one button per variant. For some games the variants are
 * grouped (sizes, difficulties, modes...), switches above the variants choosing the group shown; Train Switches also
 * asks for its number of trains.
 * <p>
 * Look of the menus ({@link MenuDialog}): the name of the game and a close button at the top, no frame of the system.
 */
@Slf4j
public class GameVariantDialog extends Stage {
    private int easyMode = 0;
    private TextField numberField;

    ToggleGroup group = new ToggleGroup();

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
        MenuDialog.undecorate(this);
        final Runnable cancel = () -> {
            close();
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        };
        setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        HashMap<Integer, FlowPane> choicePanes = new HashMap<>();
        choicePanes.put(0, createFlowPane());

        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        ScrollPane choicePanelScroller = new ScrollPane();
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

        I18NLabel chooseVariantPromptLabel = new I18NLabel(gazePlay.getTranslator(), chooseVariantPromptLabelTextKey);
        chooseVariantPromptLabel.getStyleClass().add("gp-dialog-prompt");

        VBox topPane = new VBox(16);
        topPane.setAlignment(Pos.CENTER);
        topPane.setPadding(new Insets(8, 24, 16, 24));
        topPane.getChildren().add(chooseVariantPromptLabel);

        BorderPane sceneContentPane = new BorderPane();
        sceneContentPane.setTop(topPane);
        sceneContentPane.setCenter(centerPane);
        sceneContentPane.setPadding(new Insets(0, 8, 24, 8));

        final Configuration config = ActiveConfigurationContext.getInstance();

        final Translator translator = gazePlay.getTranslator();

        // The groups of variants, if any: switches above the variants
        FlowPane groups = new FlowPane(10, 10);
        groups.getStyleClass().add("gp-variant-groups");
        groups.setAlignment(Pos.CENTER);

        for (IGameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {

            Button button = new Button(variant.getLabel(translator));
            button.getStyleClass().add("gp-variant");

            button.wrapTextProperty().setValue(true);
            button.setTextAlignment(TextAlignment.CENTER);

            button.setMinWidth(primaryStage.getWidth() / 15);
            button.setMinHeight(primaryStage.getHeight() / 15);

            button.setPrefWidth(primaryStage.getWidth() / 10);
            button.setPrefHeight(primaryStage.getHeight() / 10);

            button.setMaxWidth(primaryStage.getWidth() / 8);
            button.setMaxHeight(primaryStage.getHeight() / 8);

            int indexOfTheVariant = 0;
            if ((gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) && variant instanceof IntStringGameVariant eggVariant){
                button.setText(String.valueOf(eggVariant.getNumber()));
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
                button.setTextAlignment(TextAlignment.CENTER);
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
                button.setTextAlignment(TextAlignment.CENTER);
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                indexOfTheVariant = switch(variantString){
                    case "MODE2" -> 1;
                    case "MODE3" -> 2;
                    default -> 0;
                };
            } else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinish")){
                button.setTextAlignment(TextAlignment.CENTER);
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
                button.setTextAlignment(TextAlignment.CENTER);
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
                button.setTextAlignment(TextAlignment.CENTER);
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
                button.setTextAlignment(TextAlignment.CENTER);
                String variantString = ((IntStringGameVariant) variant).getStringValue();
                indexOfTheVariant = switch (variantString) {
                    case "ImageShrink" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots")){
                button.setTextAlignment(TextAlignment.CENTER);
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                indexOfTheVariant = switch (variantString) {
                    case "DIFFICULTY_EASY_AUTO_KEYBOARD", "DIFFICULTY_NORMAL_AUTO_KEYBOARD", "DIFFICULTY_HARD_AUTO_KEYBOARD" -> 1;
                    default -> 0;
                };
            } else if (gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse")){
                button.setTextAlignment(TextAlignment.CENTER);
                String variantString = String.valueOf(((EnumGameVariant<?>) variant).getEnumValue());
                log.debug("variant robots: {}", variantString);
                indexOfTheVariant = switch (variantString) {
                    case "DIFFICULTY_EASY_AUTO_MOUSE", "DIFFICULTY_NORMAL_AUTO_MOUSE", "DIFFICULTY_HARD_AUTO_MOUSE" -> 1;
                    default -> 0;
                };
            } else if (variant instanceof IntStringGameVariant) {
                button.setTextAlignment(TextAlignment.CENTER);
                indexOfTheVariant = ((IntStringGameVariant) variant).getNumber();
            }

            if (!choicePanes.containsKey(indexOfTheVariant)) {
                choicePanes.put(indexOfTheVariant, createFlowPane());
            }
            choicePanes.get(indexOfTheVariant).getChildren().add(button);

            log.debug("nameGame : {}", gameSpec.getGameSummary().getNameCode());

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
                RadioButton[] categories;

                if (gameSpec.getGameSummary().getNameCode().equals("Bottle")) {
                    categories = new RadioButton[6];
                    categories[0] = new RadioButton(translator.translate("TinySizeCategory"));
                    categories[1] = new RadioButton(translator.translate("SmallSizeCategory"));
                    categories[2] = new RadioButton(translator.translate("NormalSizeCategory"));
                    categories[3] = new RadioButton(translator.translate("HighSizeCategory"));
                    categories[4] = new RadioButton(translator.translate("BigSizeCategory"));
                    categories[5] = new RadioButton(translator.translate("InfinityCategory"));
                }else if (gameSpec.getGameSummary().getNameCode().equals("EggGame") || gameSpec.getGameSummary().getNameCode().equals("PersonalizeEggGame")) {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("Classic"));
                    categories[1] = new RadioButton(translator.translate("ImageShrink"));

                } else if(gameSpec.getGameSummary().getNameCode().equals("RushHour")){
                    categories = new RadioButton[6];

                    categories[0] = new RadioButton(translator.translate("Niveau1-5"));
                    categories[1] = new RadioButton(translator.translate("Niveau6-11"));
                    categories[2] = new RadioButton(translator.translate("Niveau12-17"));
                    categories[3] = new RadioButton(translator.translate("Niveau18-23"));
                    categories[4] = new RadioButton(translator.translate("Niveau24-29"));
                    categories[5] = new RadioButton(translator.translate("Niveau30-33"));

                    log.debug("categories length : {}", categories.length);

                }else if (gameSpec.getGameSummary().getNameCode().equals("Simon")){
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton("Classic");
                    categories[1] = new RadioButton("Simon Copy");
                    categories[2] = new RadioButton("Multiplayer");
                }else if(gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobots")){
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton("Normal");
                    categories[1] = new RadioButton("Auto");
                } else if(gameSpec.getGameSummary().getNameCode().equals("SurviveAgainstRobotsMouse")){
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton("Normal");
                    categories[1] = new RadioButton("Auto");
                }else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinish")){
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton(translator.translate("Niveau1-5"));
                    categories[1] = new RadioButton(translator.translate("Niveau6-11"));
                    categories[2] = new RadioButton(translator.translate("Niveau12-17"));
                }else if(gameSpec.getGameSummary().getNameCode().equals("SprintFinishMouse")){
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton(translator.translate("Niveau1-5"));
                    categories[1] = new RadioButton(translator.translate("Niveau6-11"));
                    categories[2] = new RadioButton(translator.translate("Niveau12-17"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("DotToDot") ||
                    gameSpec.getGameSummary().getNameCode().contains("Memory") ||
                    gameSpec.getGameSummary().getNameCode().equals("Ninja")
                ) {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("Static"));
                    categories[1] = new RadioButton(translator.translate("Dynamic"));
                } else if(gameSpec.getGameSummary().getNameCode().equals("Labyrinth")) {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("MouseCategory"));
                    categories[1] = new RadioButton(translator.translate("OtherCategory"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("RockPaperScissors")) {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("Hide"));
                    categories[1] = new RadioButton(translator.translate("Visible"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal")) {
                    categories = new RadioButton[7];
                    categories[0] = new RadioButton(translator.translate("Farm"));
                    categories[1] = new RadioButton(translator.translate("Forest"));
                    categories[2] = new RadioButton(translator.translate("Savanna"));
                    categories[3] = new RadioButton(translator.translate("Birds"));
                    categories[4] = new RadioButton(translator.translate("Maritime"));
                    categories[5] = new RadioButton(translator.translate("AllAnimals"));
                    categories[6] = new RadioButton(translator.translate("Dynamic"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                    gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape")
                ) {
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton(translator.translate("Easy"));
                    categories[1] = new RadioButton(translator.translate("Normal"));
                    categories[2] = new RadioButton(translator.translate("Hard"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag")) {
                    categories = new RadioButton[6];
                    categories[0] = new RadioButton(translator.translate("MostFamous"));
                    categories[1] = new RadioButton(translator.translate("Africa"));
                    categories[2] = new RadioButton(translator.translate("America"));
                    categories[3] = new RadioButton(translator.translate("Asia"));
                    categories[4] = new RadioButton(translator.translate("Europe"));
                    categories[5] = new RadioButton(translator.translate("AllFlags"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter")) {
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton(translator.translate("Vowels"));
                    categories[1] = new RadioButton(translator.translate("Consonants"));
                    categories[2] = new RadioButton(translator.translate("AllLetters"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound")) {
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton(translator.translate("Animals"));
                    categories[1] = new RadioButton(translator.translate("Instruments"));
                    categories[2] = new RadioButton(translator.translate("AllSounds"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")){
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton("3 "+translator.translate("Stations"));
                    categories[1] = new RadioButton("8 "+translator.translate("Stations"));
                    categories[2] = new RadioButton("13 "+translator.translate("Stations"));
                } else {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("Classic"));
                    categories[1] = new RadioButton(translator.translate("HighContrasts"));
                }

                for (int i = 0; i < categories.length; i++) {
                    int index = i;
                    categories[i].setToggleGroup(group);
                    groups.getChildren().add(categories[i]);
                    categories[i].setOnAction(actionEvent -> {
                        if (easyMode != index) {
                            easyMode = index;
                            choicePanelScroller.setContent(choicePanes.get(index));
                        }
                    });
                }

                categories[0].setSelected(true);
                if (!topPane.getChildren().contains(groups)) {
                    topPane.getChildren().add(groups);
                }
            }


            EventHandler<Event> event = mouseEvent -> {
                close();
                root.setDisable(false);
                if (config.getWhereIsItDir().equals("") && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                    whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant);
                } else if (gameSpec.getGameSummary().getNameCode().equals("TrainSwitches")) {
                    IntStringGameVariant v = (IntStringGameVariant) variant;
                    int numberOfTrains = 10;
                    numberOfTrains = Integer.parseInt(numberField.getText());
                    log.debug("number of trains : {}", numberOfTrains);
                    v.setNumber2(numberOfTrains);
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, v);
                } else {
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
                }
            };
            // A click, Enter or Space
            button.setOnAction(event::handle);
        }

        Scene scene = MenuDialog.createScene(this, sceneContentPane, translator.translate("Close"), cancel);

        CssUtil.setPreferredStylesheets(config, scene, gazePlay.getCurrentScreenDimensionSupplier());

        setScene(scene);
        setWidth(primaryStage.getWidth() * 0.6);
        setHeight(primaryStage.getHeight() * 0.65);
    }

    /**
     * Asks for the folder of the custom images of "Where is it?" before starting it.
     */
    private void whereIsItErrorHandling(GazePlay gazePlay, GameMenuController gameMenuController, GameSpec gameSpec, Parent root, IGameVariant finalVariant) {
        String whereIsItPromptLabel = "WhereIsItNot Config Directory";
        GameWhereIsItErrorPathDialog errorDialog = new GameWhereIsItErrorPathDialog(gazePlay, gameMenuController, gazePlay.getPrimaryStage(), gameSpec, root, whereIsItPromptLabel, finalVariant);
        errorDialog.setTitle("error");
        errorDialog.show();
        errorDialog.toFront();
    }

    /**
     * @return a pane laying out the buttons of the variants
     */
    private FlowPane createFlowPane() {
        FlowPane newFlowPane = new FlowPane();
        newFlowPane.setAlignment(Pos.CENTER);
        newFlowPane.setHgap(10);
        newFlowPane.setVgap(10);
        return newFlowPane;
    }

}
