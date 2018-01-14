package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class GameContext extends GraphicalContext<Pane> {

    public static GameContext newInstance(GazePlay gazePlay) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        CssUtil.setPreferredStylesheets(config, scene);

        Bravo bravo = new Bravo();

        Pane gamingRoot = new Pane();
        gamingRoot.setStyle("-fx-background-color: black;");

        HBox menuHBox = createHBox();
        menuHBox.toFront();

        Rectangle blindFoldPanel = new Rectangle(0, 0, 0, 0);

        StackPane bottomStackPane = new StackPane();
        bottomStackPane.getChildren().add(blindFoldPanel);
        bottomStackPane.getChildren().add(menuHBox);

        EventHandler<MouseEvent> mouseEnterControlPanelEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                resizeBlindFoldPanel(blindFoldPanel, menuHBox);
                blindFoldPanel.toBack();
            }
        };
        EventHandler<MouseEvent> mouseExitControlPanelEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                resizeBlindFoldPanel(blindFoldPanel, menuHBox);
                blindFoldPanel.toFront();
            }
        };

        bottomStackPane.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnterControlPanelEventHandler);
        bottomStackPane.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitControlPanelEventHandler);

        mouseEnterControlPanelEventHandler.handle(null);

        root.setBottom(bottomStackPane);
        root.setCenter(gamingRoot);

        GamePanelDimensionProvider gamePanelDimensionProvider = new GamePanelDimensionProvider(gamingRoot, scene);

        RandomPositionGenerator randomPositionGenerator = new RandomPanePositionGenerator(gamePanelDimensionProvider);

        return new GameContext(gazePlay, gamingRoot, scene, bravo, bottomStackPane, menuHBox,
                gamePanelDimensionProvider, randomPositionGenerator, root);
    }

    private static void resizeBlindFoldPanel(Rectangle blindFoldPanel, HBox menuHBox) {
        blindFoldPanel.setX(0);
        blindFoldPanel.setY(0);
        blindFoldPanel.setWidth(menuHBox.getWidth());
        blindFoldPanel.setHeight(menuHBox.getHeight());
    }

    public static HBox createHBox() {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        // hbox.setStyle("-fx-background-color: lightgrey;");
        // hbox.setBackground(new BackgroundFill()):

        hbox.backgroundProperty()
                .setValue(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        hbox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        hbox.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

        return hbox;
    }

    private final Bravo bravo;

    private final Pane bottomPane;

    private final HBox menuHBox;

    @Getter
    private final RandomPositionGenerator randomPositionGenerator;

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    private final BorderPane rootBorderPane;

    private GameContext(GazePlay gazePlay, Pane gamingRoot, Scene scene, Bravo bravo, Pane bottomPane, HBox menuHBox,
            GamePanelDimensionProvider gamePanelDimensionProvider, RandomPositionGenerator randomPositionGenerator,
            BorderPane rootBorderPane) {
        super(gazePlay, gamingRoot, scene);
        this.bravo = bravo;
        this.bottomPane = bottomPane;
        this.menuHBox = menuHBox;
        this.gamePanelDimensionProvider = gamePanelDimensionProvider;
        this.randomPositionGenerator = randomPositionGenerator;
        this.rootBorderPane = rootBorderPane;
    }

    public void resetBordersToFront() {
        rootBorderPane.setBottom(null);
        rootBorderPane.setBottom(bottomPane);
    }

    public HomeButton createHomeButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats) {
        HomeButton homeButton = new HomeButton();

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    scene.setCursor(Cursor.WAIT); // Change cursor to wait style

                    stats.stop();
                    GazeUtils.clear();

                    log.info("stats = " + stats);

                    Runnable asynchronousStatsPersistTask = new Runnable() {
                        @Override
                        public void run() {
                            stats.saveStats();
                        }
                    };
                    Thread asynchronousStatsPersistThread = new Thread(asynchronousStatsPersistTask);
                    asynchronousStatsPersistThread.start();

                    StatsContext statsContext = StatsContext.newInstance(gazePlay, stats);

                    gazePlay.onDisplayStats(statsContext);

                    scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
                }
            }
        };

        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        // homeButton.recomputeSizeAndPosition(scene);
        menuHBox.getChildren().add(homeButton);

        return homeButton;
    }

    public void createToggleFullScreenButtonInGameScreen(@NonNull GazePlay gazePlay) {

        EventHandler<Event> eventHandler = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {
                gazePlay.toggleFullScreen();
            }
        };

        Image buttonGraphics = new Image("data/common/images/fullscreen.png");

        Button button = new Button("FullScreen", new ImageView(buttonGraphics));
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

        // button.recomputeSizeAndPosition(scene);
        menuHBox.getChildren().add(button);
    }

    public void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler) {
        getChildren().add(bravo);
        bravo.playWinTransition(scene, delay, onFinishedEventHandler);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}
