package net.gazeplay.games.mediaPlayer;

import java.io.File;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GazePlay;
import net.gazeplay.User;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.shooter.Point;
import net.gazeplay.games.shooter.Shooter;

@Slf4j
public class mediaPlayer extends Parent implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;

    private Button titre1, titre2, titre3;
    private Button left, playPause, right, fullScreen, addVideo;
    private BorderPane videoRoot;
    private HBox window, tools;
    private VBox scrollList, videoSide;
    private boolean full = false;
    private boolean play = false;

    @Override
    public void launch() {
        createHandlers();

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    public mediaPlayer(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        window = new HBox();

        scrollList = new VBox();

        Button upArrow = new Button("^");
        upArrow.setPrefWidth(dimension2D.getWidth() / 4);
        upArrow.setPrefHeight(dimension2D.getHeight() / 7);
        // Premier titre a afficher
        titre1 = new Button("WebView");
        titre1.setPrefWidth(dimension2D.getWidth() / 4);
        titre1.setPrefHeight(dimension2D.getHeight() / 7);
        // Second titre a afficher
        titre2 = new Button("MediaPlayer");
        titre2.setPrefWidth(dimension2D.getWidth() / 4);
        titre2.setPrefHeight(dimension2D.getHeight() / 7);
        // Troisieme titre a afficher
        titre3 = new Button("MP3Player");
        titre3.setPrefWidth(dimension2D.getWidth() / 4);
        titre3.setPrefHeight(dimension2D.getHeight() / 7);
        Button downArrow = new Button("v");
        downArrow.setPrefWidth(dimension2D.getWidth() / 4);
        downArrow.setPrefHeight(dimension2D.getHeight() / 7);

        scrollList.setSpacing(dimension2D.getHeight() / 30);
        scrollList.setAlignment(Pos.CENTER);
        scrollList.getChildren().addAll(upArrow, titre1, titre2, titre3, downArrow);

        videoSide = new VBox();

        addVideo = new Button("+");
        addVideo.setPrefWidth(dimension2D.getWidth() / 6);
        addVideo.setPrefHeight(dimension2D.getHeight() / 8);

        videoRoot = new BorderPane();

        WebView video = new WebView();

        BorderPane.setAlignment(video, Pos.CENTER);
        videoRoot.setCenter(video);

        tools = new HBox();

        left = new Button("<<");
        left.setPrefWidth(dimension2D.getWidth() / 12);
        left.setPrefHeight(dimension2D.getHeight() / 8);
        playPause = new Button("||>");
        playPause.setPrefWidth(dimension2D.getWidth() / 12);
        playPause.setPrefHeight(dimension2D.getHeight() / 8);
        right = new Button(">>");
        right.setPrefWidth(dimension2D.getWidth() / 12);
        right.setPrefHeight(dimension2D.getHeight() / 8);
        fullScreen = new Button("full");
        fullScreen.setPrefWidth(dimension2D.getWidth() / 12);
        fullScreen.setPrefHeight(dimension2D.getHeight() / 8);

        tools.setSpacing(dimension2D.getWidth() / 20);
        tools.setAlignment(Pos.CENTER);
        tools.getChildren().addAll(left, playPause, right, fullScreen);

        videoSide.setSpacing(dimension2D.getHeight() / 30);
        videoSide.setAlignment(Pos.CENTER);
        videoSide.getChildren().addAll(addVideo, videoRoot, tools);

        window.setSpacing(dimension2D.getWidth() / 15);
        window.setAlignment(Pos.CENTER);
        window.getChildren().addAll(scrollList, videoSide);

        window.setLayoutX(dimension2D.getWidth() / 8);
        window.setLayoutY(dimension2D.getHeight() / 12);

        this.gameContext.getChildren().add(window);

    }

    public void createHandlers() {
        EventHandler<MouseEvent> eventTitre1 = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {

                Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                String videoUrl = "http://www.youtube.com/embed/YE7VzlLtp-4?autoplay=1";
                WebView webview = new WebView();
                webview.getEngine().load(videoUrl);
                webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p

                videoRoot.getChildren().clear();

                BorderPane.setAlignment(webview, Pos.CENTER);
                videoRoot.setCenter(webview);
                play = true;

            }
        };
        titre1.addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre1);

        EventHandler<MouseEvent> eventTitre2 = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {

                Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                File media = new File("C:/Users/super/Downloads/oow2010-2.flv");
                MediaPlayer player = new MediaPlayer(new Media(media.toURI().toString()));
                MediaView mediaView = new MediaView(player);
                mediaView.setFitHeight(dimension2D.getHeight() / 2);
                mediaView.setFitWidth(dimension2D.getWidth() / 3);

                videoRoot.getChildren().clear();

                BorderPane.setAlignment(mediaView, Pos.CENTER);
                videoRoot.setCenter(mediaView);
                player.play();
                play = true;

            }
        };
        titre2.addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre2);

        EventHandler<MouseEvent> eventTitre3 = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {

                Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                File media = new File("D:/Musique/La cour des grands (Deluxe)/01 - La cour des grands.mp3");
                MediaPlayer player = new MediaPlayer(new Media(media.toURI().toString()));
                MediaView mediaView = new MediaView(player);
                mediaView.setFitHeight(dimension2D.getHeight() / 2);
                mediaView.setFitWidth(dimension2D.getWidth() / 3);

                videoRoot.getChildren().clear();

                BorderPane.setAlignment(mediaView, Pos.CENTER);
                videoRoot.setCenter(mediaView);
                player.play();
                play = true;

            }
        };
        titre3.addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre3);

        EventHandler<MouseEvent> eventFull = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {

                Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                if (videoRoot.getCenter() instanceof MediaView) {
                    MediaView mediaView = (MediaView) videoRoot.getCenter();
                    if (!full) {
                        mediaView.setFitWidth(dimension2D.getWidth());
                        mediaView.setFitHeight((7 * dimension2D.getHeight()) / 8);
                        gameContext.getChildren().clear();
                        videoSide.setSpacing(0);
                        videoSide.getChildren().remove(addVideo);
                        BorderPane bp = new BorderPane();
                        bp.setCenter(videoSide);
                        double x = dimension2D.getHeight() - mediaView.getFitHeight() + left.getHeight();
                        bp.setLayoutY(x / 2);
                        gameContext.getChildren().add(bp);
                    } else {
                        mediaView.setFitHeight(dimension2D.getHeight() / 2);
                        mediaView.setFitWidth(dimension2D.getWidth() / 3);
                        gameContext.getChildren().clear();
                        videoSide.getChildren().setAll(addVideo, videoRoot, tools);
                        videoSide.setSpacing(dimension2D.getHeight() / 30);
                        window.getChildren().clear();
                        window.getChildren().addAll(scrollList, videoSide);
                        gameContext.getChildren().add(window);
                    }
                    full = !full;

                } else if (videoRoot.getCenter() instanceof WebView) {
                    WebView webview = (WebView) videoRoot.getCenter();
                    if (!full) {
                        webview.setPrefSize(dimension2D.getWidth(), (7 * dimension2D.getHeight()) / 8); // 360p
                        gameContext.getChildren().clear();
                        videoSide.setSpacing(0);
                        videoSide.getChildren().remove(addVideo);
                        BorderPane bp = new BorderPane();
                        bp.setCenter(videoSide);
                        gameContext.getChildren().add(bp);
                    } else {
                        webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p
                        gameContext.getChildren().clear();
                        videoSide.setSpacing(dimension2D.getHeight() / 30);
                        window.getChildren().clear();
                        videoSide.getChildren().setAll(addVideo, videoRoot, tools);
                        window.getChildren().addAll(scrollList, videoSide);
                        gameContext.getChildren().add(window);
                    }
                    full = !full;

                }

            }
        };
        fullScreen.addEventFilter(MouseEvent.MOUSE_CLICKED, eventFull);

        EventHandler<MouseEvent> eventPlayPause = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (videoRoot.getCenter() instanceof MediaView) {
                    MediaView mediaView = (MediaView) videoRoot.getCenter();
                    if (play) {
                        mediaView.getMediaPlayer().pause();
                    } else {
                        mediaView.getMediaPlayer().play();
                    }
                    play = !play;
                }
            }
        };

        playPause.addEventFilter(MouseEvent.MOUSE_CLICKED, eventPlayPause);

        EventHandler<MouseEvent> eventAddVideo = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Stage dialog = createDialog(gameContext.getGazePlay().getPrimaryStage());
                dialog.setTitle("new Title");
                dialog.show();

                dialog.toFront();
                dialog.setAlwaysOnTop(true);
            }
        };

        addVideo.addEventFilter(MouseEvent.MOUSE_CLICKED, eventAddVideo);

    }

    private Stage createDialog(Stage primaryStage) {
        // initialize the confirmation dialog
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
        });

        HBox choicePane = new HBox();
        choicePane.setSpacing(20);
        choicePane.setAlignment(Pos.CENTER);

        ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        // URL BLOCK ___
        VBox urlSide = new VBox();

        HBox urlField = new HBox();
        urlField.setAlignment(Pos.CENTER);
        Text t = new Text("URL: ");
        TextField tf = new TextField();
        tf.setPromptText("enter a web URL");
        tf.setMaxWidth(primaryStage.getWidth() / 10);
        urlField.getChildren().addAll(t, tf);

        Button buttonURL = new Button("Ok");
        buttonURL.getStyleClass().add("gameChooserButton");
        buttonURL.getStyleClass().add("gameVariation");
        buttonURL.getStyleClass().add("button");
        buttonURL.setMinHeight(primaryStage.getHeight() / 10);
        buttonURL.setMinWidth(primaryStage.getWidth() / 10);

        urlSide.getChildren().addAll(urlField, buttonURL);
        // ___ URL BLOCK

        // PATH BLOCK ___
        VBox pathSide = new VBox();

        Button pathField = new Button("new media");
        pathField.getStyleClass().add("gameChooserButton");
        pathField.getStyleClass().add("gameVariation");
        pathField.getStyleClass().add("button");
        pathField.setMinHeight(primaryStage.getHeight() / 10);
        pathField.setMinWidth(primaryStage.getWidth() / 10);

        Button buttonPath = new Button("Ok");
        buttonPath.getStyleClass().add("gameChooserButton");
        buttonPath.getStyleClass().add("gameVariation");
        buttonPath.getStyleClass().add("button");
        buttonPath.setMinHeight(primaryStage.getHeight() / 10);
        buttonPath.setMinWidth(primaryStage.getWidth() / 10);

        pathSide.getChildren().addAll(pathField, buttonPath);
        // ___ PATH BLOCK

        EventHandler<Event> event;
        event = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                dialog.close();
                primaryStage.getScene().getRoot().setEffect(null);
            }
        };
        buttonPath.addEventHandler(MouseEvent.MOUSE_CLICKED, event);
        buttonURL.addEventHandler(MouseEvent.MOUSE_CLICKED, event);

        urlSide.setAlignment(Pos.CENTER);
        pathSide.setAlignment(Pos.CENTER);
        choicePane.getChildren().addAll(urlSide, pathSide);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        dialog.setScene(scene);

        return dialog;
    }
}
