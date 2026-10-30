
package net.gazeplay.ui.scenes.gamemenu;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
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
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;
import net.gazeplay.ui.scenes.configuration.UserImagesDialog;
import net.gazeplay.ui.scenes.errorhandlingui.GameWhereIsItErrorPathDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Window of the variants of a game, opened from the menu: one button per variant. For some games the variants are
 * grouped (sizes, difficulties, modes...), switches choosing the group shown; Train Switches also asks for its number
 * of trains. For a reviewed game (it has a help, 30/10/2026): the switches are all at the bottom, under the variants;
 * a variant button only chooses the variant (framed), and the "Start" button at the bottom starts the game, next to
 * the "i" of the help. For the other games, a variant button starts the game at once, and the switches are at the
 * top.
 * <p>
 * Look of the menus ({@link MenuDialog}): the name of the game and a close button at the top, no frame of the system.
 */
@Slf4j
public class GameVariantDialog extends Stage {
    private int easyMode = 0;
    private TextField numberField;

    ToggleGroup group = new ToggleGroup();

    /**
     * Height of the row of the help button (a round button of the menus and its margin).
     */
    private static final double HELP_ROW_HEIGHT = 90;

    /**
     * Space between two rows of switches (e.g. what the cards of Memory show, then hidden or visible cards).
     */
    static final double SWITCH_ROWS_GAP = 28;

    /**
     * Height of a row of switches.
     */
    private static final double SWITCH_ROW_HEIGHT = 50;

    /**
     * Height added to the window for its rows of switches beyond the first one.
     */
    private double extraHeight;

    /**
     * The button opening the help of the game, null when the game has none.
     */
    @Getter
    private Button helpButton;

    /**
     * Whether the game has been reviewed (it has a help, see {@link GameHelp}): its variants are chosen, then started
     * by the "Start" button at the bottom, and all its switches are at the bottom, above it (maintainer's request of
     * 30/10/2026: the other games start at once with the variant clicked, their switches at the top).
     */
    private final boolean reviewed;

    /**
     * What starts the game with each variant button.
     */
    private final Map<Button, Runnable> launches = new HashMap<>();

    /**
     * The variant chosen (reviewed games), framed in the main color.
     */
    @Getter
    private Button selectedButton;

    /**
     * The button starting the game with the variant chosen (reviewed games), null for the other games.
     */
    @Getter
    private Button startButton;

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
        final Runnable cancel = this::close;
        final String nameCode = gameSpec.getGameSummary().getNameCode();
        this.reviewed = GameHelp.exists(nameCode);
        // The menu comes back, without its blur, when the window closes, whatever closes it: a variant chosen used to
        // leave the menu blurred, seen when coming back from the game (the menu is kept, not built again)
        setOnHidden(windowEvent -> {
            root.setEffect(null);
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

        // Variants in two groups (GroupedGameVariant, e.g. Memory): switches at the top and at the bottom
        final List<GroupedGameVariant> groupedVariants = groupedVariants(gameSpec);
        if (groupedVariants != null) {
            buildGroupedChoice(groupedVariants, gazePlay, gameMenuController, primaryStage, gameSpec, root,
                translator, topPane, centerPane, choicePanelScroller);
        }

        for (IGameVariant variant : groupedVariants != null ? List.<IGameVariant>of()
            : gameSpec.getGameVariantGenerator().getVariants()) {

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
            } else if (gameSpec.getGameSummary().getNameCode().equals("Horses")) {
                // The normal board, or the simplified one (HorsesVariant SIMPLIFIED_...), chosen at the bottom
                indexOfTheVariant = variant.toString().contains(":SIMPLIFIED_") ? 1 : 0;
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
                gameSpec.getGameSummary().getNameCode().equals("Horses") ||
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
                } else if (gameSpec.getGameSummary().getNameCode().equals("Horses")) {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("HorsesBoardNormal"));
                    categories[1] = new RadioButton(translator.translate("HorsesBoardSimplified"));
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
                if (reviewed) {
                    // The switches under the variants, above the "Start" button (maintainer's request of 30/10/2026)
                    if (!centerPane.getChildren().contains(groups)) {
                        groups.setPadding(new Insets(16, 0, 0, 0));
                        centerPane.getChildren().add(groups);
                    }
                } else if (!topPane.getChildren().contains(groups)) {
                    topPane.getChildren().add(groups);
                }
            }


            EventHandler<Event> event = mouseEvent -> {
                close();
                final String userImages = UserImagesVariant.folderOf(variant);
                if (config.getWhereIsItDir().equals("") && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                    whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant);
                } else if (userImages != null && UserImagesVariant.images(userImages).isEmpty()) {
                    userImagesErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant, userImages);
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
            // A click, Enter or Space: starts the game, or chooses the variant (reviewed games)
            register(button, () -> event.handle(null));
        }

        if (reviewed) {
            // At the bottom: "Start" in the middle, the help of the game (a round "i") at the right
            startButton = new Button(translator.translate("VariantStart"));
            startButton.getStyleClass().addAll("gp-button", "gp-button-primary");
            startButton.setDefaultButton(true);
            startButton.setOnAction(e -> {
                if (selectedButton != null) {
                    launches.get(selectedButton).run();
                }
            });
            helpButton = MenuStyle.roundButton(MenuIcon.INFO, "teal", translator.translate("GameHelp"),
                () -> GameHelp.dialog(this, translator, nameCode, getTitle(), null).show());
            final BorderPane bottom = new BorderPane();
            bottom.setCenter(startButton);
            bottom.setRight(helpButton);
            // Room on the left as wide as the help button, so that "Start" is in the middle
            final Region balance = new Region();
            balance.prefWidthProperty().bind(helpButton.widthProperty());
            bottom.setLeft(balance);
            bottom.setPadding(new Insets(8, 16, 0, 16));
            sceneContentPane.setBottom(bottom);

            // The variant at the same place stays chosen when a switch shows other variants
            choicePanelScroller.contentProperty().addListener((observable, oldPane, newPane) -> {
                int index = 0;
                if (oldPane instanceof FlowPane old && selectedButton != null) {
                    index = Math.max(0, old.getChildren().indexOf(selectedButton));
                }
                selectIn(newPane, index);
            });
            selectIn(choicePanelScroller.getContent(), 0);
        }

        Scene scene = MenuDialog.createScene(this, sceneContentPane, translator.translate("Close"), cancel);

        CssUtil.setPreferredStylesheets(config, scene);

        setScene(scene);
        setWidth(primaryStage.getWidth() * 0.6);
        // Taller by the row of the help button, so that the variants keep their room
        setHeight(primaryStage.getHeight() * 0.65 + (helpButton == null ? 0 : HELP_ROW_HEIGHT) + extraHeight);
    }

    /**
     * A variant button: it starts the game at once, or, for a reviewed game, it is chosen (the "Start" button starts
     * the game).
     */
    private void register(final Button button, final Runnable launch) {
        launches.put(button, launch);
        button.setOnAction(e -> {
            if (reviewed) {
                select(button);
            } else {
                launch.run();
            }
        });
    }

    /**
     * Chooses a variant: it is framed, the one chosen before no longer.
     */
    void select(final Button button) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("gp-variant-selected");
        }
        selectedButton = button;
        button.getStyleClass().add("gp-variant-selected");
    }

    /**
     * Chooses the variant at a place of a list of variants (the last one if there are fewer).
     */
    private void selectIn(final javafx.scene.Node pane, final int index) {
        if (pane instanceof FlowPane flowPane && !flowPane.getChildren().isEmpty()) {
            final int place = Math.min(index, flowPane.getChildren().size() - 1);
            if (flowPane.getChildren().get(place) instanceof Button button) {
                select(button);
            }
        }
    }

    /**
     * @return the variants of the game when all of them are in two groups, else null
     */
    static List<GroupedGameVariant> groupedVariants(final GameSpec gameSpec) {
        final List<GroupedGameVariant> grouped = new ArrayList<>();
        for (final IGameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {
            if (!(variant instanceof GroupedGameVariant groupedVariant)) {
                return null;
            }
            grouped.add(groupedVariant);
        }
        return grouped.isEmpty() ? null : grouped;
    }

    /**
     * Switches at the top for the groups, at the bottom for the sub-groups, and the buttons of the variants of the
     * chosen group and sub-group (the first ones at the start).
     */
    private void buildGroupedChoice(final List<GroupedGameVariant> variants, final GazePlay gazePlay,
                                    final GameMenuController gameMenuController, final Stage primaryStage,
                                    final GameSpec gameSpec, final Parent root, final Translator translator,
                                    final VBox topPane, final VBox centerPane, final ScrollPane scroller) {
        final Map<String, FlowPane> panes = new LinkedHashMap<>();
        final Set<String> groups = new LinkedHashSet<>();
        final Set<String> subGroups = new LinkedHashSet<>();
        for (final GroupedGameVariant variant : variants) {
            groups.add(variant.getGroup());
            subGroups.add(variant.getSubGroup());
            final Button button = variantButton(variant.getLabel(translator), primaryStage);
            register(button, () -> {
                close();
                final String userImages = UserImagesVariant.folderOf(variant);
                if (userImages != null && UserImagesVariant.images(userImages).isEmpty()) {
                    userImagesErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant, userImages);
                } else {
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
                }
            });
            panes.computeIfAbsent(variant.getGroup() + "|" + variant.getSubGroup(), key -> createFlowPane())
                .getChildren().add(button);
        }
        final String[] chosen = {groups.iterator().next(), subGroups.iterator().next()};
        final Runnable show = () -> scroller.setContent(panes.getOrDefault(chosen[0] + "|" + chosen[1],
            createFlowPane()));
        // Under the variants: the groups, then the sub-groups just under them (maintainer's request of 30/10/2026: all
        // the switches together at the bottom)
        final FlowPane groupRow = switches(groups, translator, value -> {
            chosen[0] = value;
            show.run();
        });
        groupRow.setPadding(new Insets(16, 0, 0, 0));
        final FlowPane subGroupRow = switches(subGroups, translator, value -> {
            chosen[1] = value;
            show.run();
        });
        // A clear space between the two rows of switches (maintainer's request of 30/10/2026)
        subGroupRow.setPadding(new Insets(SWITCH_ROWS_GAP, 0, 0, 0));
        centerPane.getChildren().addAll(groupRow, subGroupRow);
        // The window is taller by the second row and the space above it, so that the variants keep their room
        extraHeight += SWITCH_ROWS_GAP + SWITCH_ROW_HEIGHT;
        show.run();
    }

    /**
     * @return a row of switches, one per value (key of translation), the first one selected
     */
    private static FlowPane switches(final Set<String> values, final Translator translator,
                                     final java.util.function.Consumer<String> onChosen) {
        final FlowPane row = new FlowPane(10, 10);
        row.getStyleClass().add("gp-variant-groups");
        row.setAlignment(Pos.CENTER);
        final ToggleGroup toggles = new ToggleGroup();
        for (final String value : values) {
            final RadioButton radio = new RadioButton(translator.translate(value));
            radio.setToggleGroup(toggles);
            radio.setOnAction(e -> onChosen.accept(value));
            row.getChildren().add(radio);
        }
        ((RadioButton) row.getChildren().get(0)).setSelected(true);
        return row;
    }

    /**
     * @return the button of a variant, sized for the window
     */
    private static Button variantButton(final String text, final Stage primaryStage) {
        final Button button = new Button(text);
        button.getStyleClass().add("gp-variant");
        button.wrapTextProperty().setValue(true);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setMinWidth(primaryStage.getWidth() / 15);
        button.setMinHeight(primaryStage.getHeight() / 15);
        button.setPrefWidth(primaryStage.getWidth() / 10);
        button.setPrefHeight(primaryStage.getHeight() / 10);
        button.setMaxWidth(primaryStage.getWidth() / 8);
        button.setMaxHeight(primaryStage.getHeight() / 8);
        return button;
    }

    /**
     * Asks for the folder of the custom images of "Where is it?" before starting it.
     */
    private void whereIsItErrorHandling(GazePlay gazePlay, GameMenuController gameMenuController, GameSpec gameSpec, Parent root, IGameVariant finalVariant) {
        // The menu stays blurred and disabled behind this window, until it closes
        root.setEffect(new BoxBlur());
        root.setDisable(true);
        // "WhereIsItNot Config Directory" used to be asked: a key missing from the translations, shown as it was
        String whereIsItPromptLabel = "WhereIsItDirNotConfig";
        GameWhereIsItErrorPathDialog errorDialog = new GameWhereIsItErrorPathDialog(gazePlay, gameMenuController, gazePlay.getPrimaryStage(), gameSpec, root, whereIsItPromptLabel, finalVariant);
        errorDialog.setTitle(gazePlay.getTranslator().translate("WhereIsIt"));
        errorDialog.show();
        errorDialog.toFront();
    }

    /**
     * Asks for the images of the user of a variant playing with them, when it has none, before starting it.
     */
    private void userImagesErrorHandling(GazePlay gazePlay, GameMenuController gameMenuController, GameSpec gameSpec,
                                         Parent root, IGameVariant variant, String folder) {
        // The menu stays blurred and disabled behind this window, until it closes
        root.setEffect(new BoxBlur());
        root.setDisable(true);
        final UserImagesDialog dialog = new UserImagesDialog(gazePlay.getPrimaryStage(), root, gazePlay.getTranslator(),
            folder, gazePlay.getTranslator().translate(gameSpec.getGameSummary().getNameCode()),
            () -> gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant));
        dialog.show();
        dialog.toFront();
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
