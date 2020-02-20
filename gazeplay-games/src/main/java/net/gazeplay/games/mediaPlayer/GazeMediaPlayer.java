package net.gazeplay.games.mediaPlayer;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Slf4j
public class GazeMediaPlayer extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;


    private final MediaButton[] mediaButtons;
    private final Button left;
    private final Button playPause;
    private final Button right;
    private final Button fullScreen;
    private final Button addVideo;
    private final Button upArrow;
    private final Button downArrow;
    private final BorderPane videoRoot;
    private final HBox window;
    private final HBox tools;
    private final VBox scrollList;
    private final VBox videoSide;
    private final Text musicTitle;
    private final MediaFileReader musicList;
    private boolean full = false;
    private boolean play = false;

    GazeMediaPlayer(final IGameContext gameContext) {
        this.gameContext = gameContext;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        musicList = new MediaFileReader(gameContext);

        window = new HBox();
        gameContext.getGazeDeviceManager().addEventFilter(gameContext.getRoot());

        scrollList = new VBox();

        mediaButtons = new MediaButton[3];


        upArrow = createButton("^", dimension2D.getWidth() / 5, dimension2D.getHeight() / 7);
        upArrow.setStyle("-fx-background-radius: 5em; ");

        for (int i = 0; i < 3; i++) {
            mediaButtons[i] = new MediaButton(dimension2D.getWidth() / 4, dimension2D.getHeight() / 7);
        }

        downArrow = createButton("v", dimension2D.getWidth() / 5, dimension2D.getHeight() / 7);
        downArrow.setStyle("-fx-background-radius: 5em; ");

        scrollList.setSpacing(dimension2D.getHeight() / 30);
        scrollList.setAlignment(Pos.CENTER);
        scrollList.getChildren().addAll(upArrow, mediaButtons[0], mediaButtons[1], mediaButtons[2], downArrow);

        videoSide = new VBox();

        addVideo = createButton("+", dimension2D.getWidth() / 6, dimension2D.getHeight() / 8);

        videoRoot = new BorderPane();

        final ImageView video = new ImageView();
        video.resize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p

        final StackPane videoStack = new StackPane();

        final Rectangle r = new Rectangle(0, 0, dimension2D.getWidth() / 3, dimension2D.getHeight() / 2);
        r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));

        videoStack.getChildren().addAll(r, video);
        video.toFront();

        BorderPane.setAlignment(videoStack, Pos.CENTER);
        videoRoot.setCenter(videoStack);

        musicTitle = new Text();
        musicTitle.setFill(Color.WHITE);

        tools = new HBox();

        left = createButtonWithGraphic(dimension2D.getWidth() / 12, dimension2D.getHeight() / 8, "data/gazeMediaPlayer/prev.png");
        playPause = createButtonWithGraphic(dimension2D.getWidth() / 12, dimension2D.getHeight() / 8, "data/gazeMediaPlayer/playPause.png");
        right = createButtonWithGraphic(dimension2D.getWidth() / 12, dimension2D.getHeight() / 8, "data/gazeMediaPlayer/next.png");
        fullScreen = createButtonWithGraphic(dimension2D.getWidth() / 12, dimension2D.getHeight() / 8, "data/gazeMediaPlayer/fullon.png");

        tools.setSpacing(dimension2D.getWidth() / 20);
        tools.setAlignment(Pos.CENTER);
        tools.getChildren().addAll(left, playPause, right, fullScreen);

        videoSide.setSpacing(dimension2D.getHeight() / 30);
        videoSide.setAlignment(Pos.CENTER);
        videoSide.getChildren().addAll(addVideo, videoRoot, musicTitle, tools);

        window.setSpacing(dimension2D.getWidth() / 15);
        window.setAlignment(Pos.CENTER);
        window.getChildren().addAll(scrollList, videoSide);

        window.setLayoutX(dimension2D.getWidth() / 8);
        window.setLayoutY(dimension2D.getHeight() / 12);

        this.gameContext.getChildren().add(window);
    }

    private Button createButtonWithGraphic(double width, double height, String imageURL) {
        Button button = createButton(width, height);
        final ImageView imageButton = new ImageView(new Image(imageURL));
        imageButton.setPreserveRatio(true);
        imageButton.setFitHeight((90 * button.getHeight()) / 100);
        button.setGraphic(imageButton);
        return button;
    }

    private Button createButton(double width, double height) {
        Button button = new Button();
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        return button;
    }

    private Button createButton(String text, double width, double height) {
        Button button = new Button(text);
        button.setMinWidth(width);
        button.setMinHeight(height);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        return button;
    }

    @Override
    public void launch() {
        createHandlers();
    }

    @Override
    public void dispose() {
        this.stopMedia();
    }

    private void createFullScreenHandler() {
        fullScreen.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> fullScreenCheck());
        fullScreen.addEventFilter(GazeEvent.GAZE_ENTERED, e -> fullScreenCheck());
    }

    private void createPlayPauseHandler() {
        final EventHandler<Event> eventPlayPause = e -> {
            if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
                final MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
                if (play) {
                    mediaView.getMediaPlayer().pause();
                } else {
                    mediaView.getMediaPlayer().play();
                }
                play = !play;
            }
        };
        playPause.addEventFilter(MouseEvent.MOUSE_CLICKED, eventPlayPause);
        playPause.addEventFilter(GazeEvent.GAZE_ENTERED, eventPlayPause);
    }

    private void createAddVideoHandler() {
        final EventHandler<Event> eventAddVideo = e -> {
            final Stage dialog = createDialog(gameContext.getPrimaryStage());
            dialog.setTitle("new Title");
            dialog.show();
            dialog.toFront();
            dialog.setAlwaysOnTop(true);
        };
        addVideo.addEventFilter(MouseEvent.MOUSE_CLICKED, eventAddVideo);
    }

    private void createHandlers() {
        createFullScreenHandler();
        createPlayPauseHandler();
        createAddVideoHandler();
        createUpDownHandlers();
        createLeftRightHandlers();
        setMusicListeListener();
    }

    private void stopMedia() {
        if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
            final MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            mediaView.getMediaPlayer().stop();
        }
        ((StackPane) videoRoot.getCenter()).getChildren().set(1, new ImageView());
    }

    private void createLeftRightHandlers() {
        final EventHandler<Event> eventLeft = e -> {
            stopMedia();
            playMusic(true);

        };
        left.addEventFilter(MouseEvent.MOUSE_CLICKED, eventLeft);
        left.addEventFilter(GazeEvent.GAZE_ENTERED, eventLeft);

        final EventHandler<Event> eventRight = e -> {
            stopMedia();
            playMusic(false);

        };
        right.addEventFilter(MouseEvent.MOUSE_CLICKED, eventRight);
        right.addEventFilter(GazeEvent.GAZE_ENTERED, eventRight);
    }

    private void playMusic(final boolean next) {
        final MediaFile mf;
        if (next) {
            mf = musicList.mediaToPlayNext();
        } else {
            mf = musicList.mediaToPlayPrevious();
        }

        if (mf != null && mf.getType().equals("URL")) {

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            final String videoUrl = mf.getPath();
            final WebView webview = new WebView();
            webview.getEngine().load(videoUrl);
            play = true;

            if (full) {
                final BorderPane bp = (BorderPane) videoSide.getParent();
                bp.setLayoutY(0);
                webview.setPrefSize(dimension2D.getWidth(), (7 * dimension2D.getHeight()) / 8); // 360p
            } else {
                webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p
            }

            BorderPane.setAlignment(webview, Pos.CENTER);
            ((StackPane) videoRoot.getCenter()).getChildren().set(1, webview);

        } else if (mf != null && mf.getType().equals("MEDIA")) {
            stopMedia();

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            final File file = new File(mf.getPath());
            final Media media = new Media(file.toURI().toString());
            final MediaPlayer player = new MediaPlayer(media);
            final MediaView mediaView = new MediaView(player);

            if (full) {
                mediaView.setFitWidth(dimension2D.getWidth());
                mediaView.setFitHeight((7 * dimension2D.getHeight()) / 8);

                final Rectangle r = new Rectangle(0, 0, (7 * dimension2D.getHeight()) / 8, (7 * dimension2D.getHeight()) / 8);
                r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
                ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);

                gameContext.getChildren().clear();
                videoSide.setSpacing(0);
                videoSide.getChildren().remove(addVideo);
                final BorderPane bp = new BorderPane();
                bp.setCenter(videoSide);
                bp.setLayoutY(0);
                gameContext.getChildren().add(bp);
            } else {
                mediaView.setFitHeight(dimension2D.getHeight() / 2);
                mediaView.setFitWidth(dimension2D.getWidth() / 3);
                final Rectangle r = new Rectangle(0, 0, dimension2D.getWidth() / 3, dimension2D.getHeight() / 2);
                r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
                ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);
            }

            BorderPane.setAlignment(mediaView, Pos.CENTER);

            ((StackPane) videoRoot.getCenter()).getChildren().set(1, mediaView);
            player.play();

        }

        if (mf != null) {
            musicTitle.setText(mf.getName());
        }

    }

    private void setMusicListeListener() {
        musicList.getFirstMediaDisplayedIndex().addListener((o, oldValue, newValue) -> {

            if (oldValue.intValue() < newValue.intValue()) {
                putMusic(false);

            } else {
                putMusic(true);
            }

        });
    }

    private void createUpDownHandlers() {
        downArrow.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            musicList.next();
        });
        downArrow.addEventFilter(GazeEvent.GAZE_ENTERED, e -> {
            musicList.next();
        });

        upArrow.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            musicList.previous();
        });
        upArrow.addEventFilter(GazeEvent.GAZE_ENTERED, e -> {
            musicList.previous();
        });
    }

    private Stage createDialog(final Stage primaryStage) {
        // initialize the confirmation dialog
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> primaryStage.getScene().getRoot().setEffect(null));

        final VBox choicePane = new VBox();
        choicePane.setSpacing(50);
        choicePane.setAlignment(Pos.CENTER);

        final ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        final HBox sides = new HBox();
        sides.setSpacing(50);
        sides.setAlignment(Pos.CENTER);

        final Text titleText = new Text("Title");

        final TextField title = new TextField();
        title.setPromptText("enter the title of the media");
        title.setMaxWidth(primaryStage.getWidth() / 5);

        final Button tfi = createMediaButton(gameContext.getTranslator().translate("ChooseImage"), primaryStage.getWidth() / 10, primaryStage.getHeight() / 10);
        tfi.setMinHeight(primaryStage.getHeight() / 20);
        tfi.setMinWidth(primaryStage.getWidth() / 10);

        final EventHandler<Event> chooseImageHandler = event -> {
            final String s = getImage(tfi, dialog);
            if (s != null) {
                tfi.setText(s);
            }
        };

        tfi.addEventFilter(MouseEvent.MOUSE_CLICKED, chooseImageHandler);

        // URL BLOCK ___
        final VBox urlSide = new VBox();
        urlSide.setSpacing(10);

        final HBox urlField = new HBox();
        urlField.setAlignment(Pos.CENTER);
        final TextField tf = new TextField();
        tf.setPromptText("enter a web URL");
        tf.setMaxWidth(primaryStage.getWidth() / 10);
        urlField.getChildren().add(tf);

        final Button buttonURL = createMediaButton("Ok", primaryStage.getWidth() / 10, primaryStage.getHeight() / 10);

        urlSide.getChildren().addAll(urlField, buttonURL);
        // ___ URL BLOCK

        // PATH BLOCK ___
        final VBox pathSide = new VBox();
        pathSide.setSpacing(10);

        final Button pathField = createMediaButton("new Media", primaryStage.getWidth() / 10, primaryStage.getHeight() / 10);

        final EventHandler<Event> eventNew;
        eventNew = mouseEvent -> {
            final String s = getPath(primaryStage);
            pathField.setText(s);
        };

        pathField.addEventHandler(MouseEvent.MOUSE_CLICKED, eventNew);

        final Button buttonPath = createMediaButton("Ok", primaryStage.getWidth() / 10, primaryStage.getHeight() / 10);
        pathSide.getChildren().addAll(pathField, buttonPath);
        // ___ PATH BLOCK

        final Text t = new Text();

        final EventHandler<Event> eventURL;
        eventURL = mouseEvent -> {
            if (tf.getText() != null && !tf.getText().equals("")) {
                dialog.close();
                String name = title.getText();
                if (name == null || name.equals("")) {
                    name = "media" + musicList.getMediaList().size();
                }

                final MediaFile mf;
                if (tfi.getText().equals(gameContext.getTranslator().translate("ChooseImage"))) {
                    mf = new MediaFile("URL", tf.getText(), name, null);
                } else {
                    mf = new MediaFile("URL", tf.getText(), name, tfi.getText());
                }

                musicList.addMedia(mf);
                primaryStage.getScene().getRoot().setEffect(null);
            } else {
                t.setText("Invalid URL !");
                t.setFill(Color.RED);
            }
        };

        final EventHandler<Event> eventPath;
        eventPath = mouseEvent -> {
            if (pathField.getText() != null && !pathField.getText().equals("new media")) {
                dialog.close();
                String name = title.getText();
                if (name == null || name.equals("")) {
                    name = "media" + musicList.getMediaList().size();
                }
                final MediaFile mf;
                if (tfi.getText().equals(gameContext.getTranslator().translate("ChooseImage"))) {
                    mf = new MediaFile("MEDIA", pathField.getText(), name, null);
                } else {
                    mf = new MediaFile("MEDIA", pathField.getText(), name, tfi.getText());
                }
                musicList.addMedia(mf);
                primaryStage.getScene().getRoot().setEffect(null);
            } else {
                t.setText("Invalid File !");
                t.setFill(Color.RED);
            }
        };

        buttonPath.addEventHandler(MouseEvent.MOUSE_CLICKED, eventPath);
        buttonURL.addEventHandler(MouseEvent.MOUSE_CLICKED, eventURL);

        urlSide.setAlignment(Pos.CENTER);
        pathSide.setAlignment(Pos.CENTER);
        sides.getChildren().addAll(urlSide, new Separator(Orientation.VERTICAL), pathSide);

        choicePane.getChildren().addAll(titleText, tfi, title, sides, t);

        final Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        dialog.setScene(scene);

        return dialog;
    }

    private Button createMediaButton(String text, double width, double height) {
        Button button = createButton(text, width, height);
        button.getStyleClass().add("gameChooserButton");
        button.getStyleClass().add("gameVariation");
        button.getStyleClass().add("button");
        return button;
    }


    private String getPath(final Stage primaryStage) {
        String s = null;
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        final File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
        }
        return s;
    }

    private EventHandler<Event> handlerURL(MediaFile mf) {
        return e -> {
            stopMedia();

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            final String videoUrl = mf.getPath();
            final WebView webview = new WebView();
            webview.getEngine().load(videoUrl);
            webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p

            BorderPane.setAlignment(webview, Pos.CENTER);
            ((StackPane) videoRoot.getCenter()).getChildren().set(1, webview);
            play = true;

            musicList.setPlayingMediaIndex(musicList.getMediaList().indexOf(mf));

            musicTitle.setText(mf.getName());
        };
    }

    private EventHandler<Event> handlerMedia(MediaFile mf) {
        return e -> {

            stopMedia();

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            final File media = new File(mf.getPath());
            final MediaPlayer player = new MediaPlayer(new Media(media.toURI().toString()));
            final MediaView mediaView = new MediaView(player);
            mediaView.setFitHeight(dimension2D.getHeight() / 2);
            mediaView.setFitWidth(dimension2D.getWidth() / 3);

            BorderPane.setAlignment(mediaView, Pos.CENTER);

            ((StackPane) videoRoot.getCenter()).getChildren().set(1, mediaView);
            player.play();
            play = true;

            musicList.setPlayingMediaIndex(musicList.getMediaList().indexOf(mf));
            musicTitle.setText(mf.getName());
        };
    }

    private void setupMedia(int i, MediaFile mediaFile) {
        mediaButtons[i].setText(mediaFile.getName());
        mediaButtons[i].setMediaFile(mediaFile);
        switch (mediaFile.getType()) {
            case "URL":
                mediaButtons[i].setupEvent(handlerURL(mediaFile));
                break;
            case "MEDIA":
                mediaButtons[i].setupEvent(handlerMedia(mediaFile));
                break;
        }
    }

    private void putMusic(final boolean ArrowDirection) {
        int index = musicList.getIndexofFirsToDisplay();
        if (index != -1) {
            for (int i = 0; i < 3; i++) {

                MediaFile mediaFile = musicList.getMediaList().get(index);

                if (mediaFile != null) {
                    setupMedia(i, mediaFile);
                    mediaButtons[i].setupImage();
                }

                if (ArrowDirection) {
                    index = (index + 1) % musicList.getMediaList().size();
                } else {
                    index = (index - 1 + musicList.getMediaList().size()) % musicList.getMediaList().size();
                }
            }
        }

    }

    private void fullScreenCheck() {
        if (!full) {
            enableFullScreen();
        } else {
            disableFullScreen();
        }
        full = !full;
    }

    private void enableFullScreen() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
            final MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            mediaView.setFitWidth(dimension2D.getWidth());
            if (mediaView.getMediaPlayer().getMedia().getWidth() != 0) {
                mediaView.setFitHeight((7 * dimension2D.getHeight()) / 8);
            } else {
                mediaView.setFitHeight(0);
            }
            final double size = (7 * dimension2D.getHeight()) / 8;
            final Rectangle r = new Rectangle(0, 0, size, size);
            r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
            ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);

            gameContext.getChildren().clear();
            videoSide.setSpacing(0);
            videoSide.getChildren().remove(addVideo);
            final BorderPane bp = new BorderPane();
            bp.setCenter(videoSide);
            // double x = dimension2D.getHeight() - mediaView.getFitHeight() + left.getHeight();
            bp.setLayoutY(0);
            gameContext.getChildren().add(bp);
            BorderPane.setAlignment(mediaView, Pos.CENTER);
        } else if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof WebView) {
            final WebView webview = (WebView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            webview.setPrefSize(dimension2D.getWidth(), (7 * dimension2D.getHeight()) / 8); // 360p
            gameContext.getChildren().clear();
            videoSide.setSpacing(0);
            videoSide.getChildren().remove(addVideo);
            final BorderPane bp = new BorderPane();
            bp.setCenter(videoSide);
            gameContext.getChildren().add(bp);
        }

    }

    private void disableFullScreen() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
            final MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            mediaView.setFitHeight(dimension2D.getHeight() / 2);
            mediaView.setFitWidth(dimension2D.getWidth() / 3);

            final Rectangle r = new Rectangle(0, 0, dimension2D.getWidth() / 3, dimension2D.getHeight() / 2);
            r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
            ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);

            gameContext.getChildren().clear();
            videoSide.getChildren().setAll(addVideo, videoRoot, musicTitle, tools);
            videoSide.setSpacing(dimension2D.getHeight() / 30);
            window.getChildren().clear();
            window.getChildren().addAll(scrollList, videoSide);
            gameContext.getChildren().add(window);
        } else if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof WebView) {
            final WebView webview = (WebView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p
            gameContext.getChildren().clear();
            videoSide.setSpacing(dimension2D.getHeight() / 30);
            window.getChildren().clear();
            videoSide.getChildren().setAll(addVideo, videoRoot, musicTitle, tools);
            window.getChildren().addAll(scrollList, videoSide);
            gameContext.getChildren().add(window);
        }
    }

    private String getImage(final Button tfi, final Stage primaryStage) {
        String s = null;
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff"),
            new ExtensionFilter("PNG Files", "*.png"), new ExtensionFilter("JPeg Files", "*.jpg", "*.jpeg"),
            new ExtensionFilter("GIF Files", "*.gif"), new ExtensionFilter("BMP Files", "*.bmp"),
            new ExtensionFilter("TIFF Files", "*.tiff"));
        final File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
            final ImageView iv;
            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            try {
                iv = new ImageView(new Image(new FileInputStream(selectedFile)));
                iv.setPreserveRatio(true);
                iv.setFitHeight(dimension2D.getHeight() / 10);
                tfi.setGraphic(iv);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return s;
    }
}
