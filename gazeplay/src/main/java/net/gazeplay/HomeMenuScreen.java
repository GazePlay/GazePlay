package net.gazeplay;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.util.List;

@Data
@Slf4j
public class HomeMenuScreen extends GraphicalContext<BorderPane> {

    private final static String LOGO_PATH = "data/common/images/gazeplay.png";

    public static HomeMenuScreen newInstance(final GazePlay gazePlay, final Configuration config) {

        GamesLocator gamesLocator = new DefaultGamesLocator();
        List<GameSpec> games = gamesLocator.listGames();

        BorderPane root = new BorderPane();

        return new HomeMenuScreen(gazePlay, games, root, config);
    }

    private final List<GameSpec> games;

    private GameLifeCycle currentGame;

    @Setter
    @Getter
    private GazeDeviceManager gazeDeviceManager;

    private FlowPane choicePanel;

    private final GameMenuFactory gameMenuFactory = new GameMenuFactory();

    public HomeMenuScreen(GazePlay gazePlay, List<GameSpec> games, BorderPane root, Configuration config) {
        super(gazePlay, root);
        this.games = games;
        this.gazeDeviceManager = GazeDeviceManagerFactory.getInstance().createNewGazeListener();

        CustomButton exitButton = createExitButton();
        CustomButton logoutButton = createLogoutButton(gazePlay);

        ConfigurationContext configurationContext = ConfigurationContext.newInstance(gazePlay);
        ConfigurationButton configurationButton = ConfigurationButton.createConfigurationButton(configurationContext);

        HBox leftControlPane = new HBox();
        leftControlPane.setAlignment(Pos.CENTER);
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(leftControlPane);
        leftControlPane.getChildren().add(configurationButton);
        leftControlPane.getChildren().add(createMusicControlPane());
        leftControlPane.getChildren().add(createEffectsVolumePane());

        I18NButton toggleFullScreenButton = createToggleFullScreenButtonInGameScreen(gazePlay);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER);
        rightControlPane.getChildren().add(toggleFullScreenButton);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControlPane);
        bottomPane.setRight(rightControlPane);

        MenuBar menuBar = Utils.buildLicence();

        Node logo = createLogo();
        StackPane topLogoPane = new StackPane();
        topLogoPane.getChildren().add(logo);

        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        topRightPane.getChildren().addAll(logoutButton, exitButton);

        ProgressIndicator indicator = new ProgressIndicator(0);
        Node gamePickerChoicePane = createGamePickerChoicePane(games, config, indicator);

        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        centerCenterPane.getChildren().add(gamePickerChoicePane);

        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(menuBar);

        BorderPane centerPanel = new BorderPane();
        centerPanel.setCenter(centerCenterPane);
        centerPanel.setLeft(leftPanel);

        BorderPane topPane = new BorderPane();
        topPane.setTop(menuBar);
        topPane.setCenter(topLogoPane);
        topPane.setRight(topRightPane);

        root.setTop(topPane);
        root.setBottom(bottomPane);
        root.setCenter(centerPanel);

        /*
         * config.getWhiteBackgroundProperty().addListener((val, oldvalue, newvalue) -> { Color c =
         * (newvalue.booleanValue()) ? Color.WHITE : Color.BLACK; ((BorderPane) root.getBottom()).setBackground(new
         * Background(new BackgroundFill(c, null, null))); ((BorderPane) root.getTop()).setBackground(new Background(new
         * BackgroundFill(c, null, null))); });
         * 
         * int i = (config.isBackgroundWhite()) ? 1 : 0;
         * 
         * root.setStyle("-fx-background-color: rgba(" + i + "," + i + "," + i + ", 1); " +
         * "-fx-background-radius: 8px; " + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " +
         * "-fx-border-color: rgba(60, 63, 65, 0.7); " +
         * "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
         * 
         * Color c = (config.isBackgroundWhite()) ? Color.WHITE : Color.BLACK; ((BorderPane)
         * root.getBottom()).setBackground(new Background(new BackgroundFill(c, null, null))); ((BorderPane)
         * root.getTop()).setBackground(new Background(new BackgroundFill(c, null, null))); menuBar.setBackground(new
         * Background(new BackgroundFill(c, null, null)));
         */

        root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    private ScrollPane createGamePickerChoicePane(List<GameSpec> games, Configuration config,
            ProgressIndicator indicator) {

        final int flowpaneGap = 20;
        choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.CENTER);
        choicePanel.setHgap(flowpaneGap);
        choicePanel.setVgap(flowpaneGap);
        choicePanel.setPadding(new Insets(20, 60, 20, 60));

        ScrollPane choicePanelScroller = new ScrollPane(choicePanel);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        Multilinguism multilinguism = Multilinguism.getSingleton();

        final Translator translator = getGazePlay().getTranslator();

        final GameButtonOrientation gameButtonOrientation = GameButtonOrientation.fromConfig(config);
        BooleanProperty gameSelected = new SimpleBooleanProperty();
        gameSelected.setValue(false);

        for (GameSpec gameSpec : games) {
            final GameButtonPane gameCard = gameMenuFactory.createGameButton(getGazePlay(), root, config, multilinguism,
                    translator, gameSpec, gameButtonOrientation, gazeDeviceManager, gameSelected);
            choicePanel.getChildren().add(gameCard);

            gameCard.setEnterhandler(new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    if (config.isGazeMenuEnable()) {
                        if (e.getSource() == gameCard /* && !gameCard.isActive() */) {
                            indicator.setProgress(0);
                            indicator.setOpacity(1);
                            indicator.toFront();
                            switch (gameButtonOrientation) {
                            case HORIZONTAL:
                                ((BorderPane) ((GameButtonPane) e.getSource()).getLeft()).setRight(indicator);
                                break;
                            case VERTICAL:
                                ((BorderPane) ((GameButtonPane) e.getSource()).getCenter()).setRight(indicator);
                                break;
                            }
                            ((GameButtonPane) e.getSource()).setTimelineProgressBar(new Timeline());

                            ((GameButtonPane) e.getSource()).getTimelineProgressBar().setDelay(new Duration(500));

                            ((GameButtonPane) e.getSource()).getTimelineProgressBar().getKeyFrames()
                                    .add(new KeyFrame(new Duration(config.getFixationLength()),
                                            new KeyValue(indicator.progressProperty(), 1)));

                            ((GameButtonPane) e.getSource()).getTimelineProgressBar().onFinishedProperty()
                                    .set(new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent actionEvent) {
                                            indicator.setOpacity(0);
                                            for (Node n : choicePanel.getChildren()) {
                                                if (n instanceof GameButtonPane) {
                                                    if (((GameButtonPane) n).getTimelineProgressBar() != null) {
                                                        ((GameButtonPane) n).getTimelineProgressBar().stop();
                                                    }
                                                }
                                            }
                                            ((GameButtonPane) e.getSource()).getEventhandler().handle(null);
                                        }
                                    });
                            ((GameButtonPane) e.getSource()).getTimelineProgressBar().play();
                        }
                    }
                }
            });

            gameCard.setExithandler(new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    if (config.isGazeMenuEnable()) {
                        if (e.getSource() == gameCard /* && gameCard.isActive() */) {
                            indicator.setProgress(0);
                            ((GameButtonPane) e.getSource()).getTimelineProgressBar().stop();
                            indicator.setOpacity(0);
                            switch (gameButtonOrientation) {
                            case HORIZONTAL:
                                ((BorderPane) ((GameButtonPane) e.getSource()).getLeft()).setRight(null);
                                break;
                            case VERTICAL:
                                ((BorderPane) ((GameButtonPane) e.getSource()).getCenter()).setRight(null);
                                break;
                            }
                        }
                    }
                }
            });

            if (Configuration.getInstance().isGazeMenuEnable()) {
                gameCard.addEventFilter(GazeEvent.GAZE_ENTERED, gameCard.getEnterhandler());
                gameCard.addEventFilter(GazeEvent.GAZE_EXITED, gameCard.getExithandler());
                gazeDeviceManager.addEventFilter(gameCard);
            }

        }

        /*
         * choicePanel.setBackground(new Background(new BackgroundImage(new Image("data/common/images/back.gif"), null,
         * null, null, new BackgroundSize(1, 1, true, true, true, true))));
         */

        return choicePanelScroller;
    }

    private CustomButton createExitButton() {
        CustomButton exitButton = new CustomButton("data/common/images/power-off.png");
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> System.exit(0));
        return exitButton;
    }

    private CustomButton createLogoutButton(GazePlay gazePlay) {
        CustomButton logoutButton = new CustomButton("data/common/images/logout.png");
        logoutButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> gazePlay.goToUserPage());
        return logoutButton;
    }

    private Node createLogo() {
        double width = root.getWidth() * 0.5;
        double height = root.getHeight() * 0.2;

        log.info(LOGO_PATH);
        final Image logoImage = new Image(LOGO_PATH, width, height, true, true);
        final ImageView logoView = new ImageView(logoImage);

        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            final double newHeight = newValue.doubleValue() * 0.2;
            final Image newLogoImage = new Image(LOGO_PATH, width, newHeight, true, true);
            logoView.setImage(newLogoImage);
        });

        return logoView;
    }
}
