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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class GazeMediaPlayer extends Parent implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;

    private Button[] titre;
    private Button left, playPause, right, fullScreen, addVideo, upArrow, downArrow;
    private BorderPane videoRoot;
    private HBox window, tools;
    private VBox scrollList, videoSide;
    private boolean full = false;
    private boolean play = false;
    private MediaFileReader musicList;

    EventHandler<MouseEvent>[] eventTitre;

    @Override
    public void launch() {
        createHandlers();
        createUpDownHandlers();
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    public GazeMediaPlayer(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        eventTitre = new EventHandler[3];

        musicList = new MediaFileReader();

        window = new HBox();

        scrollList = new VBox();

        titre = new Button[3];

        upArrow = new Button("^");
        upArrow.setPrefWidth(dimension2D.getWidth() / 4);
        upArrow.setPrefHeight(dimension2D.getHeight() / 7);
        // Premier titre a afficher
        titre[0] = new Button("WebView");
        titre[0].setPrefWidth(dimension2D.getWidth() / 4);
        titre[0].setPrefHeight(dimension2D.getHeight() / 7);
        putMusic(0, true);
        // Second titre a afficher
        titre[1] = new Button("MediaPlayer");
        titre[1].setPrefWidth(dimension2D.getWidth() / 4);
        titre[1].setPrefHeight(dimension2D.getHeight() / 7);
        putMusic(1, true);
        // Troisieme titre a afficher
        titre[2] = new Button("MP3Player");
        titre[2].setPrefWidth(dimension2D.getWidth() / 4);
        titre[2].setPrefHeight(dimension2D.getHeight() / 7);
        putMusic(2, true);
        downArrow = new Button("v");
        downArrow.setPrefWidth(dimension2D.getWidth() / 4);
        downArrow.setPrefHeight(dimension2D.getHeight() / 7);

        scrollList.setSpacing(dimension2D.getHeight() / 30);
        scrollList.setAlignment(Pos.CENTER);
        scrollList.getChildren().addAll(upArrow, titre[0], titre[1], titre[2], downArrow);

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
        EventHandler<MouseEvent> eventFull = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {

                Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                if (videoRoot.getCenter() instanceof MediaView) {
                    MediaView mediaView = (MediaView) videoRoot.getCenter();
                    if (!full) {

                        mediaView.setFitWidth(dimension2D.getWidth());
                        if (mediaView.getMediaPlayer().getMedia().getWidth() != 0) {
                            mediaView.setFitHeight((7 * dimension2D.getHeight()) / 8);
                        } else {
                            mediaView.setFitHeight(0);
                        }
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

    private void createUpDownHandlers() {

        EventHandler<MouseEvent> eventDownArrow = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                titre[0].setText(titre[1].getText());
                titre[0].removeEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[0]);
                titre[0].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[1]);
                titre[1].setText(titre[2].getText());
                titre[1].removeEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[1]);
                titre[1].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[2]);
                putMusic(2, true);
            }
        };

        downArrow.addEventFilter(MouseEvent.MOUSE_CLICKED, eventDownArrow);

        EventHandler<MouseEvent> eventUpArrow = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                titre[2].setText(titre[1].getText());
                titre[2].removeEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[2]);
                titre[2].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[1]);
                titre[1].setText(titre[0].getText());
                titre[1].removeEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[1]);
                titre[1].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[0]);
                putMusic(0, false);
            }
        };

        upArrow.addEventFilter(MouseEvent.MOUSE_CLICKED, eventUpArrow);

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

        EventHandler<Event> eventURL;
        eventURL = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                dialog.close();
                primaryStage.getScene().getRoot().setEffect(null);
            }
        };

        EventHandler<Event> eventPath;
        eventPath = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                dialog.close();
                primaryStage.getScene().getRoot().setEffect(null);
            }
        };

        buttonPath.addEventHandler(MouseEvent.MOUSE_CLICKED, eventURL);
        buttonURL.addEventHandler(MouseEvent.MOUSE_CLICKED, eventPath);

        urlSide.setAlignment(Pos.CENTER);
        pathSide.setAlignment(Pos.CENTER);
        choicePane.getChildren().addAll(urlSide, pathSide);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        dialog.setScene(scene);

        return dialog;
    }

    public void putMusic(int i, boolean next) {
        MediaFile mf;
        if (next) {
            mf = musicList.next();
        } else {
            mf = musicList.previous();
        }
        titre[i].setText(mf.getName());

        if (mf.getType().equals("URL")) {

            eventTitre[i] = new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {

                    Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                    String videoUrl = mf.getPath();
                    WebView webview = new WebView();
                    webview.getEngine().load(videoUrl);
                    webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p

                    videoRoot.getChildren().clear();

                    BorderPane.setAlignment(webview, Pos.CENTER);
                    videoRoot.setCenter(webview);
                    play = true;

                }
            };

        } else {

            eventTitre[i] = new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {

                    Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                    File media = new File(mf.getPath());
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
        }

        titre[i].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre[i]);

    }
}
