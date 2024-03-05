package net.gazeplay.ui.scenes.configuration;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ThumbnailImage;
import net.gazeplay.ui.scenes.gamemenu.GameButtonPane;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CustomGamesChooser extends Stage {

    private Configuration configuration;
    private ConfigurationContext configurationContext;
    private Translator translator;
    private GazePlay gazePlay;
    public List<GameSpec> games;
    private ThumbnailImage thumbnailImage;
    CustomGamesChooser(Configuration configuration,
                       ConfigurationContext configurationContext,
                       Translator translator,
                       GazePlay gazePlay){
        super();
        this.configuration = configuration;
        this.configurationContext = configurationContext;
        this.translator = translator;
        this.gazePlay = gazePlay;
        this.games = gazePlay.getGamesLocator().listGames(gazePlay.getTranslator());
        this.thumbnailImage = new ThumbnailImage();

        this.setMinWidth(500);
        this.setMinHeight(500);

        final Stage primaryStage = gazePlay.getPrimaryStage();
        this.initOwner(primaryStage);
        this.initModality(Modality.WINDOW_MODAL);
        this.initStyle(StageStyle.UTILITY);
        this.setOnCloseRequest(
            windowEvent -> primaryStage.getScene().getRoot().setEffect(null));
        this.setTitle(translator.translate("customGamesChooser"));
        this.toFront();

        this.createCardGames();

    }
    public void createCardGames(){

        ScrollPane scrollPane = new ScrollPane();
        GridPane gridpane = new GridPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPrefSize(800, 800);
        scrollPane.setContent(gridpane);
        Scene scene = new Scene(scrollPane);
        this.setScene(scene);


        gridpane.setAlignment(Pos.CENTER);
        gridpane.setPadding(new Insets(50, 50, 50, 50));

        gridpane.setHgap(50);
        gridpane.setVgap(50);


        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setFillWidth(true);
        columnConstraints.setHgrow(Priority.ALWAYS);
        gridpane.getColumnConstraints().add(columnConstraints);

        int imageCol = 0;
        int imageRow = 0;

        for (GameSpec gameSpec : games) {
            final GameSummary gameSummary = gameSpec.getGameSummary();
            final I18NText gameTitleText = new I18NText(translator, gameSummary.getNameCode());
            String path = thumbnailImage.getPathThumbnailImg(gameSummary.getGameThumbnail());
            Image image = new Image(path);

            ImageView pic = new ImageView();
            pic.setFitWidth(130);
            pic.setFitHeight(130);

            CheckBox chooseGame = new CheckBox("Sélectionner");

            pic.setImage(image);
            VBox vb = new VBox();
            vb.setAlignment(Pos.CENTER);
            vb.getChildren().addAll(gameTitleText, pic, chooseGame);

            gridpane.add(vb, imageCol, imageRow);
            GridPane.setMargin(pic, new Insets(2,2,2,2));
            imageCol++;

            if (imageCol > 2) {
                imageCol = 0;
                imageRow++;

            }
        }
    }
}
