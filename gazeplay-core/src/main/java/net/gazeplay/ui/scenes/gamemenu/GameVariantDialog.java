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
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
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

    private int easymode = 0;

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

            if ((variant instanceof DimensionDifficultyGameVariant) || (variant.toString().contains("DYNAMIC"))) {
                if (!choicePanes.containsKey(1)) {
                    choicePanes.put(1, createFlowPane());
                }
                choicePanes.get(1).getChildren().add(button);
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

            if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor") || gameSpec.getGameSummary().getNameCode().equals("Ninja") || gameSpec.getGameSummary().getNameCode().contains("Memory")) {
                if (variant instanceof DimensionGameVariant) {
                    variant = new DimensionDifficultyGameVariant(((DimensionGameVariant) variant).getWidth(), ((DimensionGameVariant) variant).getHeight(), "normal");
                }

                if (group.getToggles().size() < 2) {
                    RadioButton category1, category2;
                    if (gameSpec.getGameSummary().getNameCode().equals("WhereIsTheColor")) {
                        category1 = new RadioButton("normal");
                        category2 = new RadioButton("easy");
                    } else if (gameSpec.getGameSummary().getNameCode().equals("Ninja") || gameSpec.getGameSummary().getNameCode().contains("Memory")) {
                        category1 = new RadioButton("Static");
                        category2 = new RadioButton("Dynamic");
                    } else {
                        category1 = new RadioButton("Classic");
                        category2 = new RadioButton("High-Contrasts");
                    }

                    category1.setToggleGroup(group);
                    category1.setSelected(true);
                    category2.setToggleGroup(group);

                    bottom.getChildren().add(category2);
                    bottom.getChildren().add(category1);
                    sceneContentPane.setBottom(bottom);
                    category2.setOnAction(actionEvent -> {
                        if (easymode != 1) {
                            easymode = 1;
                            choicePanelScroller.setContent(choicePanes.get(1));
                        }
                    });
                    category1.setOnAction(actionEvent -> {
                        if (easymode != 0) {
                            easymode = 0;
                            choicePanelScroller.setContent(choicePanes.get(0));
                        }
                    });
                }
            }

            IGameVariant finalVariant = variant;
            EventHandler<Event> event = mouseEvent -> {
                close();
                root.setDisable(false);
                if (config.getWhereIsItDir().equals("") && gameSpec.getGameSummary().getNameCode().equals("WhereIsIt")) {
                    whereIsItErrorHandling(gazePlay, gameMenuController, gameSpec, root, finalVariant);
                } else {
                    gameMenuController.chooseAndStartNewGameProcess(gazePlay, gameSpec, finalVariant);
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
