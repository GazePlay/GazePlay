
package net.gazeplay.ui.scenes.gamemenu;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.scenes.errorhandlingui.GameWhereIsItErrorPathDialog;

import java.util.HashMap;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

@Slf4j
public class GameVariantDialog extends Stage {
    private int easyMode = 0;

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
        initStyle(StageStyle.UTILITY);
        setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        HashMap<Integer, FlowPane> choicePanes = new HashMap<>();
        choicePanes.put(0, createFlowPane());

        ScrollPane choicePanelScroller = new ScrollPane();
        choicePanelScroller.setContent(choicePanes.get(0));
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        choicePanelScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        final String labelStyle = "-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: black;";
        I18NLabel chooseVariantPromptLabel = new I18NLabel(gazePlay.getTranslator(), chooseVariantPromptLabelTextKey);
        chooseVariantPromptLabel.setStyle(labelStyle);

        VBox topPane = new VBox();
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(chooseVariantPromptLabel);

        BorderPane sceneContentPane = new BorderPane();
        sceneContentPane.setTop(topPane);
        sceneContentPane.setCenter(choicePanelScroller);

        final Configuration config = ActiveConfigurationContext.getInstance();

        final Translator translator = gazePlay.getTranslator();

        HBox bottom = new HBox();
        bottom.prefWidthProperty().bind(sceneContentPane.widthProperty());
        bottom.setAlignment(Pos.CENTER);
        bottom.setSpacing(50);

        for (IGameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {
            Button button = new Button(variant.getLabel(translator));
            button.getStyleClass().add("gameChooserButton");
            button.getStyleClass().add("gameVariation");
            button.getStyleClass().add("button");

            button.wrapTextProperty().setValue(true);

            button.setMinWidth(primaryStage.getWidth() / 15);
            button.setMinHeight(primaryStage.getHeight() / 15);

            button.setPrefWidth(primaryStage.getWidth() / 10);
            button.setPrefHeight(primaryStage.getHeight() / 10);

            button.setMaxWidth(primaryStage.getWidth() / 8);
            button.setMaxHeight(primaryStage.getHeight() / 8);

            if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound")
            ) {
                String difficultyString = ((DimensionDifficultyGameVariant) variant).getVariant();
                int indexOfTheVariant = switch(difficultyString) {
                    case "Easy", "Vowels", "Farm", "Animals", "MostFamous" -> 0;
                    case "Normal", "Consonants", "Forest", "Instruments", "Africa" -> 1;
                    case "Hard", "AllLetters", "Savanna", "AllSounds", "America" -> 2;
                    case "Birds", "Asia" -> 3;
                    case "Maritime", "Europe" -> 4;
                    case "AllAnimals", "AllFlags" -> 5;
                    case "Dynamic" -> 6;
                    default -> -1;
                };

                if (!choicePanes.containsKey(indexOfTheVariant)) {
                    choicePanes.put(indexOfTheVariant, createFlowPane());
                }
                choicePanes.get(indexOfTheVariant).getChildren().add(button);
            } else if (gameSpec.getGameSummary().getNameCode().equals("bottle")) {
                button.setTextAlignment(TextAlignment.CENTER);
                String variantString = ((IntStringGameVariant) variant).getStringValue();
                int indexOfTheVariant = switch (variantString) {
                    case "InfinityB" -> 5;
                    case "BigB" -> 4;
                    case "HighB" -> 3;
                    case "NormalB" -> 2;
                    case "SmallB" -> 1;
                    default -> 0;
                };

                if (!choicePanes.containsKey(indexOfTheVariant)) {
                    choicePanes.put(indexOfTheVariant, createFlowPane());
                }
                choicePanes.get(indexOfTheVariant).getChildren().add(button);
            } else if (variant instanceof IntStringGameVariant) {
                button.setTextAlignment(TextAlignment.CENTER);
                int number = ((IntStringGameVariant) variant).getNumber();

                if (!choicePanes.containsKey(number)) {
                    choicePanes.put(number, createFlowPane());
                }
                choicePanes.get(number).getChildren().add(button);
            } else {
                choicePanes.get(0).getChildren().add(button);
            }

            if ((gameSpec.getGameSummary().getNameCode().equals("bottle") ||
                gameSpec.getGameSummary().getNameCode().equals("DotToDot") ||
                gameSpec.getGameSummary().getNameCode().equals("Labyrinth") ||
                gameSpec.getGameSummary().getNameCode().contains("Memory") ||
                gameSpec.getGameSummary().getNameCode().equals("Ninja") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheFlag") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheLetter") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape") ||
                gameSpec.getGameSummary().getNameCode().equals("WhereIsTheSound")) &&
                group.getToggles().size() < 2
            ) {
                RadioButton[] categories;

                if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") ||
                    gameSpec.getGameSummary().getNameCode().equals("WhereIsTheShape")
                ) {
                    categories = new RadioButton[3];
                    categories[0] = new RadioButton(translator.translate("Easy"));
                    categories[1] = new RadioButton(translator.translate("Normal"));
                    categories[2] = new RadioButton(translator.translate("Hard"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheAnimal")) {
                    categories = new RadioButton[7];
                    categories[0] = new RadioButton(translator.translate("Farm"));
                    categories[1] = new RadioButton(translator.translate("Forest"));
                    categories[2] = new RadioButton(translator.translate("Savanna"));
                    categories[3] = new RadioButton(translator.translate("Birds"));
                    categories[4] = new RadioButton(translator.translate("Maritime"));
                    categories[5] = new RadioButton(translator.translate("AllAnimals"));
                    categories[6] = new RadioButton(translator.translate("Dynamic"));
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
                } else if (gameSpec.getGameSummary().getNameCode().equals("DotToDot") ||
                    gameSpec.getGameSummary().getNameCode().contains("Memory") ||
                    gameSpec.getGameSummary().getNameCode().equals("Ninja")
                ) {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("Static"));
                    categories[1] = new RadioButton(translator.translate("Dynamic"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("bottle")) {
                    categories = new RadioButton[6];
                    categories[0] = new RadioButton(translator.translate("TinyF"));
                    categories[1] = new RadioButton(translator.translate("SmallF"));
                    categories[2] = new RadioButton(translator.translate("NormalF"));
                    categories[3] = new RadioButton(translator.translate("HighF"));
                    categories[4] = new RadioButton(translator.translate("BigF"));
                    categories[5] = new RadioButton(translator.translate("InfinityF"));
                } else if (gameSpec.getGameSummary().getNameCode().equals("Labyrinth")) {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("MouseC"));
                    categories[1] = new RadioButton(translator.translate("OtherC"));
                } else {
                    categories = new RadioButton[2];
                    categories[0] = new RadioButton(translator.translate("Classic"));
                    categories[1] = new RadioButton(translator.translate("HighContrasts"));
                }

                for (int i = 0; i < categories.length; i++) {
                    int index = i;
                    categories[i].setToggleGroup(group);
                    bottom.getChildren().add(categories[i]);
                    categories[i].setOnAction(actionEvent -> {
                        if (easyMode != index) {
                            easyMode = index;
                            choicePanelScroller.setContent(choicePanes.get(index));
                        }
                    });
                }

                categories[0].setSelected(true);
                sceneContentPane.setBottom(bottom);
            }

            EventHandler<Event> event = mouseEvent -> {
                close();
                root.setDisable(false);
                if (config.getWhereIsItDir().equals("") && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                    whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, variant);
                } else {
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
                }
            };
            button.addEventHandler(MOUSE_CLICKED, event);
        }

        Scene scene = new Scene(sceneContentPane, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene, gazePlay.getCurrentScreenDimensionSupplier());

        setScene(scene);
        setWidth(primaryStage.getWidth() / 2);
        setHeight(primaryStage.getHeight() / 2);
    }

    private void whereIsItErrorHandling(GazePlay gazePlay, GameMenuController gameMenuController, GameSpec gameSpec, Parent root, IGameVariant finalVariant) {
        String whereIsItPromptLabel = "WhereIsItNotConfigDirectory";
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

}
