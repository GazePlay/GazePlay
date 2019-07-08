package net.gazeplay;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Data
@Slf4j
public class HomeMenuScreen extends GraphicalContext<BorderPane> {

    private final static String LOGO_PATH = "data/common/images/gazeplay.png";

    // private static String currentLanguage;

    public static HomeMenuScreen newInstance(final GazePlay gazePlay, final Configuration config) {

        GamesLocator gamesLocator = new DefaultGamesLocator();
        List<GameSpec> games = gamesLocator.listGames();

        BorderPane root = new BorderPane();

        return new HomeMenuScreen(gazePlay, games, root, config);
    }

    private List<GameSpec> games;

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

        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(menuBar);

        // filters for games based on their category

        CheckBox selectionGames = buildCategoryCheckBox(GameCategories.Category.SELECTION, config,
                configurationContext);
        CheckBox memoGames = buildCategoryCheckBox(GameCategories.Category.MEMORIZATION, config, configurationContext);
        CheckBox actionReactionGames = buildCategoryCheckBox(GameCategories.Category.ACTION_REACTION, config,
                configurationContext);
        CheckBox logicGames = buildCategoryCheckBox(GameCategories.Category.LOGIC, config, configurationContext);

        EventHandler<Event> filterEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                filterGames(selectionGames.isSelected(), memoGames.isSelected(), actionReactionGames.isSelected(),
                        logicGames.isSelected());

                HomeMenuScreen hm = newInstance(gazePlay, config);
                gazePlay.setHomeMenuScreen(hm);
                // gazePlay.loading();
                gazePlay.onReturnToMenu();

            }
        };

        selectionGames.addEventHandler(MouseEvent.MOUSE_CLICKED, filterEvent);
        actionReactionGames.addEventFilter(MouseEvent.MOUSE_CLICKED, filterEvent);
        memoGames.addEventFilter(MouseEvent.MOUSE_CLICKED, filterEvent);
        logicGames.addEventFilter(MouseEvent.MOUSE_CLICKED, filterEvent);

        HBox categoryFilters = new HBox(10);
        categoryFilters.setAlignment(Pos.CENTER);
        categoryFilters.setPadding(new Insets(15, 12, 15, 12));
        categoryFilters.getChildren().addAll(selectionGames, memoGames, actionReactionGames, logicGames);

        ProgressIndicator indicator = new ProgressIndicator(0);
        Node gamePickerChoicePane = createGamePickerChoicePane(games, config, indicator);

        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        centerCenterPane.getChildren().add(gamePickerChoicePane);

        BorderPane centerPanel = new BorderPane();
        centerPanel.setTop(categoryFilters);
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

        // reorder games by Favourite Filter

        List<GameSpec> favGames = new ArrayList<GameSpec>();
        List<BooleanProperty> favourites = new ArrayList<BooleanProperty>();

        for (BooleanProperty p : config.getFavouriteGameProperties()) {
            if (p.getValue()) {
                favourites.add(p);
            }
        }
        for (GameSpec g : games) {
            for (BooleanProperty p : favourites) {
                if (p.getName().equals(g.getGameSummary().getNameCode().toUpperCase() + " Game fav")) {
                    favGames.add(g);
                }
            }
        }

        games.removeAll(favGames);
        games.addAll(0, favGames); // favourite Games are in the beginning of the list

        BooleanProperty favouriteGameProperty = null;

        for (GameSpec gameSpec : games) {

            for (BooleanProperty p : config.getFavouriteGameProperties()) {
                if (p.getName().equals(gameSpec.getGameSummary().getNameCode().toUpperCase() + " Game fav"))
                    favouriteGameProperty = p;
            }

            final GameButtonPane gameCard = gameMenuFactory.createGameButton(getGazePlay(), root, config, multilinguism,
                    translator, gameSpec, gameButtonOrientation, gazeDeviceManager, favouriteGameProperty);

            /* Game categories */
            if (config.selectionCategory() && config.memorizationCategory() && config.actionReactionCategory()
                    && config.logicCategory()) // all games
                choicePanel.getChildren().add(gameCard);
            else if (config.selectionCategory() && config.memorizationCategory() && config.actionReactionCategory()
                    && !config.logicCategory()) { // S & M & AR
                if (gameSpec.getGameSummary().getCategory() != GameCategories.Category.LOGIC)
                    choicePanel.getChildren().add(gameCard);
            } else if (config.selectionCategory() && !config.memorizationCategory() && config.actionReactionCategory()
                    && config.logicCategory()) { // S & L & AR
                if (gameSpec.getGameSummary().getCategory() != GameCategories.Category.MEMORIZATION)
                    choicePanel.getChildren().add(gameCard);
            } else if (!config.selectionCategory() && config.memorizationCategory() && config.actionReactionCategory()
                    && config.logicCategory()) { // L & M & AR
                if (gameSpec.getGameSummary().getCategory() != GameCategories.Category.SELECTION)
                    choicePanel.getChildren().add(gameCard);
            } else if (config.selectionCategory() && config.memorizationCategory() && !config.actionReactionCategory()
                    && config.logicCategory()) { // S & M & L
                if (gameSpec.getGameSummary().getCategory() != GameCategories.Category.ACTION_REACTION)
                    choicePanel.getChildren().add(gameCard);
            } else if (config.selectionCategory() && config.memorizationCategory() && !config.actionReactionCategory()
                    && !config.logicCategory()) { // S & M
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.SELECTION
                        || gameSpec.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION)
                    choicePanel.getChildren().add(gameCard);
            } else if (config.selectionCategory() && !config.memorizationCategory() && config.actionReactionCategory()
                    && !config.logicCategory()) { // AR & S
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.SELECTION
                        || gameSpec.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION)
                    choicePanel.getChildren().add(gameCard);
            } else if (!config.selectionCategory() && config.memorizationCategory() && config.actionReactionCategory()
                    && !config.logicCategory()) { // AR & M
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION
                        || gameSpec.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION)
                    choicePanel.getChildren().add(gameCard);
            } else if (!config.selectionCategory() && !config.memorizationCategory() && config.actionReactionCategory()
                    && config.logicCategory()) { // AR & L
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION
                        || gameSpec.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    choicePanel.getChildren().add(gameCard);
            } else if (config.selectionCategory() && !config.memorizationCategory() && !config.actionReactionCategory()
                    && config.logicCategory()) { // S & L
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.SELECTION
                        || gameSpec.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    choicePanel.getChildren().add(gameCard);
            } else if (!config.selectionCategory() && config.memorizationCategory() && !config.actionReactionCategory()
                    && config.logicCategory()) { // M & L
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION
                        || gameSpec.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    choicePanel.getChildren().add(gameCard);
            } else if (config.selectionCategory() && !config.memorizationCategory() && !config.actionReactionCategory()
                    && !config.logicCategory()) { // only S
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.SELECTION)
                    choicePanel.getChildren().add(gameCard);
            } else if (!config.selectionCategory() && config.memorizationCategory() && !config.actionReactionCategory()
                    && !config.logicCategory()) { // only M
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION)
                    choicePanel.getChildren().add(gameCard);
            } else if (!config.selectionCategory() && !config.memorizationCategory() && config.actionReactionCategory()
                    && !config.logicCategory()) { // only AR
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION)
                    choicePanel.getChildren().add(gameCard);
            } else if (!config.selectionCategory() && !config.memorizationCategory() && !config.actionReactionCategory()
                    && config.logicCategory()) { // only L
                if (gameSpec.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    choicePanel.getChildren().add(gameCard);
            }

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

    private static CheckBox buildCategoryCheckBox(GameCategories.Category category, Configuration config,
            ConfigurationContext confContext) {

        I18NText label = new I18NText(confContext.getGazePlay().getTranslator(), category.getGameCategory());
        CheckBox categoryCheckbox = new CheckBox(label.getText());
        categoryCheckbox.setTextFill(Color.WHITE);

        switch (category) {
        case SELECTION:
            categoryCheckbox.setSelected(config.selectionCategory());
            categoryCheckbox.selectedProperty().addListener((o) -> {
                config.getSelectionCategoryProperty().setValue(categoryCheckbox.isSelected());
                config.saveConfigIgnoringExceptions();
            });
            break;
        case MEMORIZATION:
            categoryCheckbox.setSelected(config.memorizationCategory());
            categoryCheckbox.selectedProperty().addListener((o) -> {
                config.getMemorizationCategoryProperty().setValue(categoryCheckbox.isSelected());
                config.saveConfigIgnoringExceptions();
            });
            break;
        case ACTION_REACTION:
            categoryCheckbox.setSelected(config.actionReactionCategory());
            categoryCheckbox.selectedProperty().addListener((o) -> {
                config.getActionReactionCategoryProperty().setValue(categoryCheckbox.isSelected());
                config.saveConfigIgnoringExceptions();

            });
            break;
        case LOGIC:
            categoryCheckbox.setSelected(config.logicCategory());
            categoryCheckbox.selectedProperty().addListener((o) -> {
                config.getLogicCategoryProperty().setValue(categoryCheckbox.isSelected());
                config.saveConfigIgnoringExceptions();
            });
        }

        return categoryCheckbox;
    }

    private void filterGames(boolean selectionFilter, boolean memoFilter, boolean actionReactionFilter,
            boolean logicFiler) {
        DefaultGamesLocator df = new DefaultGamesLocator();
        List<GameSpec> allGames = df.listGames();
        List<GameSpec> filteredGames = new ArrayList<GameSpec>();
        // maybe reorder here ... ? favourites on top

        /**
         * 2^4 options
         */

        if (selectionFilter && memoFilter && actionReactionFilter && logicFiler) // all games
            this.games = allGames;

        else if (selectionFilter && memoFilter && actionReactionFilter && !logicFiler) { // selection & memorization &
            // actionReaction
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() != GameCategories.Category.LOGIC) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (selectionFilter && memoFilter && !actionReactionFilter && logicFiler) { // Selection & Memorization &
            // Logic
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() != GameCategories.Category.ACTION_REACTION) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (!selectionFilter && memoFilter && actionReactionFilter && logicFiler) { // ActionReaction &
            // Memorization & Logic
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() != GameCategories.Category.SELECTION) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (selectionFilter && !memoFilter && actionReactionFilter && logicFiler) { // Selection & ActionReaction
            // & Logic
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() != GameCategories.Category.MEMORIZATION) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (selectionFilter && memoFilter && !actionReactionFilter && !logicFiler) { // Selection & memorization
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.SELECTION
                        && g.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (selectionFilter && !memoFilter && actionReactionFilter && !logicFiler) { // Selection &
            // Action_Reaction games
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.SELECTION
                        && g.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (!selectionFilter && memoFilter && actionReactionFilter && !logicFiler) { // memorization and
            // Action_Reaction games
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION
                        && g.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION)
                    filteredGames.add(g);
            }
            // this.games = filteredGames;
        } else if (!selectionFilter && memoFilter && !actionReactionFilter && logicFiler) { // memorization & logic
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION
                        && g.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    filteredGames.add(g);
            }
            // this.games = filteredGames;
        } else if (selectionFilter && !memoFilter && !actionReactionFilter && logicFiler) { // selection & logic
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.SELECTION
                        && g.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    filteredGames.add(g);
            }
            // this.games = filteredGames;
        } else if (selectionFilter && !memoFilter && actionReactionFilter && logicFiler) { // actionReaction & logic
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION
                        && g.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    filteredGames.add(g);
            }
            // this.games = filteredGames;
        } else if (selectionFilter && !memoFilter && !actionReactionFilter && !logicFiler) { // only selection games
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.SELECTION) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (!selectionFilter && memoFilter && !actionReactionFilter && !logicFiler) { // only memorization games
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.MEMORIZATION) {
                    filteredGames.add(g);
                }
            }
            // this.games = filteredGames;
        } else if (!selectionFilter && !memoFilter && actionReactionFilter && !logicFiler) { // only Action_Reaction
            // games
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.ACTION_REACTION) {
                    filteredGames.add(g);
                }
            }
        } else if (!selectionFilter && !memoFilter && !actionReactionFilter && logicFiler) { // only logic games
            // filteredGames.clear();
            for (GameSpec g : allGames) {
                if (g.getGameSummary().getCategory() == GameCategories.Category.LOGIC)
                    filteredGames.add(g);
            }
            // this.games = filteredGames;
        } else {
            filteredGames.clear();
        }

        this.games = filteredGames;

    }
}