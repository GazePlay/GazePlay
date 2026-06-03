package net.gazeplay.games.gazeplayEval;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.NonNull;
import net.gazeplay.IGameContext;
import org.kordamp.ikonli.javafx.FontIcon;

public class EvalOptionsCard {

    String evalOptionValue;
    IGameContext gameContext;
    GazeplayEval gameInstance;

    ProgressIndicator progressIndicator;

    Button restartButton;
    Button continueButton;

    EvalOptionsCard(String evalOptionValue, @NonNull IGameContext gameContext, GazeplayEval gameInstance) {
        this.evalOptionValue = evalOptionValue;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;

        this.createButtons();

        switch (evalOptionValue) {
            case "Autonomie_Soutient_Oral":
                this.manualOption();
                break;
        }
        this.progressIndicator = buildProgressIndicator();
    }

    public void createButtons(){
        Label restartText = new Label("Recommencer");
        restartText.setStyle("-fx-font-size: 24px;");

        FontIcon restartIcon = new FontIcon("fas-redo-alt");
        restartIcon.setIconSize(80);

        VBox restartContent = new VBox(15);
        restartContent.setAlignment(Pos.CENTER);
        restartContent.getChildren().addAll(restartText, restartIcon);

        restartButton = new Button();
        restartButton.setGraphic(restartContent);

        restartButton.setPrefSize(260, 260);

        Label continueText = new Label("Continuer");
        continueText.setStyle("-fx-font-size: 24px;");

        FontIcon continueIcon = new FontIcon("fas-arrow-right");
        continueIcon.setIconSize(80);

        VBox continueContent = new VBox(15);
        continueContent.setAlignment(Pos.CENTER);
        continueContent.getChildren().addAll(continueText, continueIcon);

        continueButton = new Button();
        continueButton.setGraphic(continueContent);

        continueButton.setPrefSize(260, 260);

        HBox buttonsBox = new HBox(40);
        buttonsBox.setAlignment(Pos.CENTER);

        buttonsBox.getChildren().addAll(restartButton, continueButton);

        StackPane root = new StackPane();
        root.getChildren().add(buttonsBox);
        StackPane.setAlignment(buttonsBox, Pos.CENTER);

        root.setPrefSize(
            gameContext.getGamePanelDimensionProvider().getDimension2D().getWidth(),
            gameContext.getGamePanelDimensionProvider().getDimension2D().getHeight()
        );

        gameContext.getChildren().add(root);
    }

    public void manualOption(){
        this.continueButton.setOnMouseClicked(event -> {
            gameInstance.generateScreen();
        });

        this.restartButton.setOnMouseClicked(event -> {
            gameInstance.decreaseIndex();
            gameInstance.generateScreen();
        });
    }

    private ProgressIndicator buildProgressIndicator() {
        // progressIndicator 2cm de diamètre
        double minWidth = 75;
        double minHeight = 75;

        final Region root = gameContext.getRoot();

        ProgressIndicator result = new ProgressIndicator(0);
        result.setTranslateX((root.getWidth()/2) - (minWidth/2));
        result.setTranslateY((root.getHeight()/2) - (minHeight/2));
        result.setMinWidth(minWidth);
        result.setMinHeight(minHeight);
        result.setOpacity(0.5);
        result.setVisible(false);

        return result;
    }
}
