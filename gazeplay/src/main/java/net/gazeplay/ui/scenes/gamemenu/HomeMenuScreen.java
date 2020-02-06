package net.gazeplay.ui.scenes.gamemenu;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ConfigurationButtonFactory;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.games.LicenseUtils;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.GraphicalContext;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class HomeMenuScreen extends GraphicalContext<BorderPane> {

    private final GazeDeviceManager gazeDeviceManager;

    private final GameMenuFactory gameMenuFactory;

    private FlowPane choicePanel;

    private List<Node> gameCardsList;

    public HomeMenuScreen(
        GazePlay gazePlay,
        GazeDeviceManager gazeDeviceManager,
        GameMenuFactory gameMenuFactory,
        GamesLocator gamesLocator
    ) {
        super(gazePlay, new BorderPane());
        this.gazeDeviceManager = gazeDeviceManager;
        this.gameMenuFactory = gameMenuFactory;

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        CustomButton exitButton = createExitButton(screenDimension);
        CustomButton logoutButton = createLogoutButton(gazePlay, screenDimension);

        ConfigurationButton configurationButton = ConfigurationButtonFactory.createConfigurationButton(gazePlay);

        Configuration config = ActiveConfigurationContext.getInstance();

        HBox leftControlPane = QuickControlPanel.getInstance().createQuickControlPanel(gazePlay, getMusicControl(), configurationButton, config);

        I18NButton toggleFullScreenButton = createToggleFullScreenButtonInGameScreen(gazePlay);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER);
        rightControlPane.getChildren().add(toggleFullScreenButton);

        final List<GameSpec> games = gamesLocator.listGames(gazePlay.getTranslator());

        GamesStatisticsPane gamesStatisticsPane = new GamesStatisticsPane(gazePlay.getTranslator(), games);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControlPane);
        bottomPane.setCenter(gamesStatisticsPane);
        bottomPane.setRight(rightControlPane);

        Node logo = LogoFactory.getInstance().createLogoStatic(gazePlay.getPrimaryStage());

        StackPane topLogoPane = new StackPane();
        topLogoPane.setPadding(new Insets(15, 15, 15, 15));
        topLogoPane.getChildren().add(logo);

        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        topRightPane.getChildren().addAll(logoutButton, exitButton);

        ProgressIndicator indicator = new ProgressIndicator(0);
        Node gamePickerChoicePane = createGamePickerChoicePane(games, config, indicator);

        VBox centerPanel = new VBox();
        centerPanel.setSpacing(40);
        centerPanel.setAlignment(Pos.TOP_CENTER);
        centerPanel.getChildren().add(gamePickerChoicePane);

        final MenuBar menuBar = LicenseUtils.buildLicenceMenuBar();

        BorderPane topPane = new BorderPane();
        topPane.setTop(menuBar);
        topPane.setRight(topRightPane);
        topPane.setCenter(topLogoPane);
        topPane.setBottom(buildFilterByCategory(config, gazePlay.getTranslator()));

        //gamesStatisticsPane.refreshPreferredSize();

        root.setTop(topPane);
        root.setBottom(bottomPane);
        root.setCenter(centerPanel);

        root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
            + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
            + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    private ScrollPane createGamePickerChoicePane(
        List<GameSpec> games,
        final Configuration config,
        final ProgressIndicator indicator
    ) {
        gameCardsList = createGameCardsList(games, config, indicator);

        final int flowpaneGap = 20;
        choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.CENTER);
        choicePanel.setHgap(flowpaneGap);
        choicePanel.setVgap(flowpaneGap);
        choicePanel.setPadding(new Insets(20, 60, 20, 60));

        choicePanel.getChildren().addAll(gameCardsList);

        ScrollPane choicePanelScroller = new ScrollPane(choicePanel);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        filterGames(choicePanel, gameCardsList, config);

        return choicePanelScroller;
    }

    private List<Node> createGameCardsList(
        List<GameSpec> games,
        final Configuration config,
        final ProgressIndicator indicator
    ) {
        final Translator translator = getGazePlay().getTranslator();
        final GameButtonOrientation gameButtonOrientation = GameButtonOrientation.fromConfig(config);

        final List<Node> gameCardsList = new ArrayList<>();

        for (GameSpec gameSpec : games) {
            final GameButtonPane gameCard = gameMenuFactory.createGameButton(
                getGazePlay(),
                root,
                config,
                translator,
                gameSpec,
                gameButtonOrientation);

            gameCardsList.add(gameCard);

            gameCard.setEnterhandler(e -> {
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
                        final Timeline timelineProgressBar = new Timeline();
                        ((GameButtonPane) e.getSource()).setTimelineProgressBar(timelineProgressBar);

                        timelineProgressBar.setDelay(new Duration(500));

                        timelineProgressBar.getKeyFrames()
                            .add(new KeyFrame(new Duration(config.getFixationLength()),
                                new KeyValue(indicator.progressProperty(), 1)));

                        timelineProgressBar.onFinishedProperty()
                            .set(actionEvent -> {
                                indicator.setOpacity(0);
                                for (Node n : choicePanel.getChildren()) {
                                    if (n instanceof GameButtonPane) {
                                        if (((GameButtonPane) n).getTimelineProgressBar() != null) {
                                            ((GameButtonPane) n).getTimelineProgressBar().stop();
                                        }
                                    }
                                }
                                ((GameButtonPane) e.getSource()).getEventhandler().handle(null);
                            });
                        timelineProgressBar.play();
                    }
                }
            });

            gameCard.setExithandler(e -> {
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
            });

            if (ActiveConfigurationContext.getInstance().isGazeMenuEnable()) {
                gameCard.addEventFilter(GazeEvent.GAZE_ENTERED, gameCard.getEnterhandler());
                gameCard.addEventFilter(GazeEvent.GAZE_EXITED, gameCard.getExithandler());
                gazeDeviceManager.addEventFilter(gameCard);
            }

        }

        return gameCardsList;
    }

    private static void filterGames(FlowPane choicePanel, List<Node> completeGameCardsList, Configuration config, String searchText, Translator translator) {
        List<Node> filteredList = completeGameCardsList.stream()
            .filter(node -> {
                GameButtonPane gameButtonPane = (GameButtonPane) node;
                return (new GameCardVisiblePredicate(config)).test(node) &&
                    translator.translate(gameButtonPane.getGameSpec().getGameSummary().getNameCode()).toLowerCase().contains(searchText.toLowerCase());
            })
            .collect(Collectors.toList());

        choicePanel.getChildren().clear();
        choicePanel.getChildren().addAll(filteredList);
    }

    private static void filterGames(FlowPane choicePanel, List<Node> completeGameCardsList, Configuration config) {
        Predicate<Node> gameCardPredicate = new GameCardVisiblePredicate(config);
        List<Node> filteredList = completeGameCardsList.stream()
            .filter(gameCardPredicate)
            .collect(Collectors.toList());

        choicePanel.getChildren().clear();
        choicePanel.getChildren().addAll(filteredList);
    }


    private TextField buildSearchBar(Configuration config, Translator translator) {
        TextField gameSearchBar = new TextField();

        gameSearchBar.textProperty().addListener((obs, oldValue, newValue) -> {
            log.debug(newValue);
            filterGames(choicePanel, gameCardsList, config, newValue, translator);
        });

        return gameSearchBar;
    }

    private HBox buildFilterByCategory(Configuration config, Translator translator) {


        TextField searchBar = buildSearchBar(config, translator);
        searchBar.maxWidthProperty().bind(root.widthProperty().multiply(1d / 4d));
        searchBar.prefWidthProperty().bind(root.widthProperty().multiply(1d / 4d));
        searchBar.minWidthProperty().bind(root.widthProperty().multiply(1d / 4d));

        List<CheckBox> allCheckBoxes = new ArrayList<>();
        for (GameCategories.Category category : GameCategories.Category.values()) {
            CheckBox checkBox = buildCategoryCheckBox(category, config, translator, choicePanel, gameCardsList, searchBar);
            allCheckBoxes.add(checkBox);
        }

        HBox categoryFilters = new HBox(10);
        categoryFilters.setAlignment(Pos.CENTER);
        categoryFilters.setPadding(new Insets(15, 12, 15, 12));
        categoryFilters.getChildren().add(searchBar);
        categoryFilters.getChildren().addAll(allCheckBoxes);

        return categoryFilters;
    }

    private boolean isFavorite(GameSpec g, Configuration configuration) {
        return configuration.getFavoriteGamesProperty().contains(g.getGameSummary().getNameCode());
    }

    private CustomButton createExitButton(Dimension2D screenDimension) {
        CustomButton exitButton = new CustomButton("data/common/images/power-off.png", screenDimension);
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> System.exit(0));
        return exitButton;
    }

    private CustomButton createLogoutButton(GazePlay gazePlay, Dimension2D screenDimension) {
        CustomButton logoutButton = new CustomButton("data/common/images/logout.png", screenDimension);
        logoutButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> gazePlay.goToUserPage());
        return logoutButton;
    }

    private static CheckBox buildCategoryCheckBox(
        GameCategories.Category category,
        Configuration config,
        Translator translator,
        FlowPane choicePanel,
        List<Node> gameCardsList,
        TextField searchBar
    ) {
        I18NText label = new I18NText(translator, category.getGameCategory());
        CheckBox categoryCheckbox = new CheckBox(label.getText());
        categoryCheckbox.setTextFill(Color.WHITE);

        categoryCheckbox.setSelected(!config.getHiddenCategoriesProperty().contains(category.getGameCategory()));
        categoryCheckbox.selectedProperty().addListener((o) -> {
            if (categoryCheckbox.isSelected()) {
                config.getHiddenCategoriesProperty().remove(category.getGameCategory());
            } else {
                config.getHiddenCategoriesProperty().add(category.getGameCategory());
            }
            filterGames(choicePanel, gameCardsList, config, searchBar.getText(), translator);
        });
        return categoryCheckbox;
    }

    @AllArgsConstructor
    private static class GameCardVisiblePredicate implements Predicate<Node> {

        private final Configuration config;

        @Override
        public boolean test(Node node) {
            GameButtonPane gameButtonPane = (GameButtonPane) node;
            SortedSet<GameCategories.Category> gameCategories = gameButtonPane.getGameSpec().getGameSummary().getCategories();
            List<@NonNull String> gameCategoriesNames = gameCategories.stream().map(GameCategories.Category::getGameCategory).collect(Collectors.toList());
            return !config.getHiddenCategoriesProperty().containsAll(gameCategoriesNames);
        }

    }
}
