package net.gazeplay.ui.scenes.gamemenu;

import javafx.animation.Interpolator;
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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.ReplayingGameFromJson;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.soundsmanager.SoundManager;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.I18NTooltip;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ConfigurationButtonFactory;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.games.MenuUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.GraphicalContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class HomeMenuScreen extends GraphicalContext<BorderPane> {

    private final SoundManager soundManager;

    private final GameMenuFactory gameMenuFactory;

    private FlowPane choicePanel;

    private List<Node> gameCardsList;
    private List<Node> favGameCardsList;

    @Getter
    private Label errorMessageLabel;
    @Getter
    private StackPane errorMessage;
    @Getter
    private VBox centerPanel;

    public HomeMenuScreen(
        GazePlay gazePlay,
        SoundManager soundManager,
        GameMenuFactory gameMenuFactory,
        GamesLocator gamesLocator
    ) {
        super(gazePlay, new BorderPane());
        this.soundManager = soundManager;
        this.gameMenuFactory = gameMenuFactory;

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        CustomButton exitButton = createExitButton(screenDimension);
        CustomButton logoutButton = createLogoutButton(gazePlay, screenDimension);

        ConfigurationButton configurationButton = ConfigurationButtonFactory.createConfigurationButton(gazePlay);

        Configuration config = ActiveConfigurationContext.getInstance();

        HBox leftControlPane = QuickControlPanel.getInstance().createQuickControlPanel(gazePlay, getMusicControl(), configurationButton, config);

        I18NButton toggleFullScreenButton = createToggleFullScreenButtonInGameScreen(gazePlay);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER);
        rightControlPane.getChildren().add(toggleFullScreenButton);

        final List<GameSpec> games = gamesLocator.listGames(gazePlay.getTranslator());

        CustomButton replayGameButton = createReplayGameButton(gazePlay, screenDimension, games);


        I18NTooltip tooltipExit = new I18NTooltip(gazePlay.getTranslator(), "Exit");
        I18NTooltip tooltipLogout = new I18NTooltip(gazePlay.getTranslator(), "Logout");
        I18NTooltip tooltipLogoutOptions = new I18NTooltip(gazePlay.getTranslator(), "Options");
        I18NTooltip tooltipReplay = new I18NTooltip(gazePlay.getTranslator(), "Replay");

        I18NTooltip.install(exitButton, tooltipExit);
        I18NTooltip.install(logoutButton, tooltipLogout);
        I18NTooltip.install(configurationButton, tooltipLogoutOptions);
        I18NTooltip.install(replayGameButton, tooltipReplay);

        GamesStatisticsPane gamesStatisticsPane = new GamesStatisticsPane(gazePlay.getTranslator(), games);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControlPane);
        bottomPane.setCenter(gamesStatisticsPane);
        bottomPane.setRight(rightControlPane);

        Node logo = LogoFactory.getInstance().createLogoStatic(gazePlay.getPrimaryStage());

        HBox logosBox = new HBox();
        logosBox.getChildren().add(logo);
        logosBox.setSpacing(20);
        logosBox.setAlignment(Pos.CENTER);

        ImageView iv = new ImageView(new Image("data/common/images/logos/Logo-AFSR.png"));
        iv.fitHeightProperty().bind(((ImageView)logo).fitHeightProperty().multiply(0.7));
        iv.setPreserveRatio(true);
        logosBox.getChildren().add(iv);

        StackPane topLogoPane = new StackPane();
        topLogoPane.setPadding(new Insets(15, 15, 15, 15));
        topLogoPane.getChildren().add(logosBox);

        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        topRightPane.getChildren().addAll(replayGameButton, logoutButton, exitButton);

        ProgressIndicator dwellTimeIndicator = new ProgressIndicator(0);
        Node gamePickerChoicePane = createGamePickerChoicePane(games, config, dwellTimeIndicator);

        centerPanel = new VBox();
        centerPanel.setSpacing(40);
        centerPanel.setAlignment(Pos.TOP_CENTER);
        centerPanel.getChildren().add(gamePickerChoicePane);

        final MenuBar menuBar = MenuUtils.buildMenuBar();

        BorderPane topPane = new BorderPane();
        topPane.setTop(menuBar);
        topPane.setRight(topRightPane);
        topPane.setCenter(topLogoPane);
        topPane.setBottom(buildFilterByCategory(config, gazePlay.getTranslator(), dwellTimeIndicator));

        //gamesStatisticsPane.refreshPreferredSize();

        StackPane centerStackPane = new StackPane();
        errorMessage = new StackPane();
        Rectangle errorBackground = new Rectangle();
        errorBackground.setFill(new Color(1, 0, 0, 0.75));
        errorMessageLabel = new Label("Error message goes here");
        errorBackground.widthProperty().bind(errorMessageLabel.widthProperty().multiply(1.2));
        errorBackground.heightProperty().bind(errorMessageLabel.heightProperty().multiply(1.2));
        errorMessage.getChildren().addAll(errorBackground, errorMessageLabel);
        centerStackPane.getChildren().add(centerPanel);
        centerStackPane.getChildren().add(errorMessage);

        errorMessage.setOnMouseClicked((event) -> {
            final Timeline opacityTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
                new KeyValue(errorMessage.opacityProperty(), 0, Interpolator.EASE_OUT)));
            opacityTimeline.setOnFinished(e -> errorMessage.setMouseTransparent(true));
            this.centerPanel.setEffect(null);
            opacityTimeline.play();
        });

        errorMessage.setOpacity(0);
        errorMessage.setMouseTransparent(true);

        root.setTop(topPane);
        root.setBottom(bottomPane);
        root.setCenter(centerStackPane);

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
        final ProgressIndicator dwellTimeIndicator
    ) {
        gameCardsList = createGameCardsList(games, config, dwellTimeIndicator);
        favGameCardsList = createFavGameCardsList(games, config, dwellTimeIndicator);

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

        filterGames(choicePanel, gameCardsList, favGameCardsList, config, dwellTimeIndicator, new GameCardVisiblePredicate(config));

        return choicePanelScroller;
    }

    private List<Node> createGameCardsList(
        List<GameSpec> games,
        final Configuration config,
        final ProgressIndicator dwellTimeIndicator
    ) {
        final Translator translator = getGazePlay().getTranslator();
        final GameButtonOrientation gameButtonOrientation = GameButtonOrientation.fromConfig(config);

        final List<Node> gameCardsList = new ArrayList<>();

        for (GameSpec gameSpec : games) {
            final GameButtonPane gameCard = createGameCard(config, gameSpec, translator, gameButtonOrientation, dwellTimeIndicator);
            gameCardsList.add(gameCard);
        }

        return gameCardsList;
    }

    private List<Node> createFavGameCardsList(
        List<GameSpec> games,
        final Configuration config,
        final ProgressIndicator dwellTimeIndicator
    ) {
        final Translator translator = getGazePlay().getTranslator();
        final GameButtonOrientation gameButtonOrientation = GameButtonOrientation.fromConfig(config);

        List<GameSpec> filteredFavList = games.stream()
            .filter(gameSpec -> (isFavorite(gameSpec, config)))
            .collect(Collectors.toList());

        final List<Node> favGameCardsList = new ArrayList<>();

        for (GameSpec gameSpec : filteredFavList) {
            final GameButtonPane gameCard = createGameCard(config, gameSpec, translator, gameButtonOrientation, dwellTimeIndicator);
            favGameCardsList.add(gameCard);
        }

        return favGameCardsList;
    }

    private GameButtonPane createGameCard(final Configuration config, GameSpec gameSpec, final Translator translator, GameButtonOrientation gameButtonOrientation, final ProgressIndicator dwellTimeIndicator) {

        GameButtonPane gameCard = gameMenuFactory.createGameButton(
            getGazePlay(),
            root,
            config,
            translator,
            gameSpec,
            gameButtonOrientation);

        return gameCard;
    }

    private void filterGames(FlowPane choicePanel, List<Node> completeGameCardsList, List<Node> completeFavGameCardsList, Configuration config, ProgressIndicator dwellTimeIndicator, Predicate<Node> gameCardPredicate) {
        List<Node> filteredList = completeGameCardsList
            .stream()
            .filter(gameCardPredicate)
            .collect(Collectors.toList());

        List<Node> filteredFavList = completeFavGameCardsList
            .stream()
            .filter(gameCardPredicate)
            .collect(Collectors.toList());

        choicePanel.getChildren().clear();

        Separator s = new Separator();
        s.minWidthProperty().bind(choicePanel.widthProperty().multiply(0.9));

        choicePanel.getChildren().addAll(filteredFavList);
        choicePanel.getChildren().add(s);
        choicePanel.getChildren().addAll(filteredList);
    }

    private TextField buildSearchBar(Configuration config, Translator translator, ProgressIndicator dwellTimeIndicator) {
        TextField gameSearchBar = new TextField();
        Utils.addTextLimiter(gameSearchBar, 32);

        gameSearchBar.textProperty().addListener((obs, oldValue, newValue) -> {
            log.debug(newValue);
            filterGames(choicePanel, gameCardsList, favGameCardsList, config, dwellTimeIndicator, node -> {
                GameButtonPane gameButtonPane = (GameButtonPane) node;
                return (new GameCardVisiblePredicate(config)).test(node) &&
                    translator.translate(gameButtonPane.getGameSpec().getGameSummary().getNameCode()).toLowerCase().contains(newValue.toLowerCase());
            });
        });

        return gameSearchBar;
    }

    private HBox buildFilterByCategory(Configuration config, Translator translator, ProgressIndicator dwellTimeIndicator) {


        TextField searchBar = buildSearchBar(config, translator, dwellTimeIndicator);
        searchBar.maxWidthProperty().bind(root.widthProperty().multiply(1d / 4d));
        searchBar.prefWidthProperty().bind(root.widthProperty().multiply(1d / 4d));
        searchBar.minWidthProperty().bind(root.widthProperty().multiply(1d / 4d));

        List<CheckBox> allCheckBoxes = new ArrayList<>();
        for (GameCategories.Category category : GameCategories.Category.values()) {
            CheckBox checkBox = buildCategoryCheckBox(category, config, translator, choicePanel, gameCardsList, searchBar, dwellTimeIndicator);
            allCheckBoxes.add(checkBox);
        }

        HBox categoryFilters = new HBox(10);
        categoryFilters.setAlignment(Pos.CENTER);
        categoryFilters.setPadding(new Insets(15, 12, 15, 12));
        categoryFilters.getChildren().add(searchBar);
        categoryFilters.getChildren().addAll(allCheckBoxes);

        return categoryFilters;
    }

    private static boolean isFavorite(GameSpec g, Configuration configuration) {
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

    private CustomButton createReplayGameButton(GazePlay gazePlay, Dimension2D screenDimension, List<GameSpec> games) {
        CustomButton replayButton = new CustomButton("data/common/images/replay_button.png", screenDimension);
        replayButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> {
            try {
                ReplayingGameFromJson replayingGame = new ReplayingGameFromJson(gazePlay, gameMenuFactory.getApplicationContext(), games);
                replayingGame.pickJSONFile(replayingGame.getFileName());
                if (ReplayingGameFromJson.replayIsAllowed(replayingGame.getCurrentGameNameCode())) {
                    replayingGame.replayGame();
                } else if (replayingGame.getCurrentGameNameCode() != null) {
                    Translator translator = gazePlay.getTranslator();
                    this.errorMessageLabel.setText(
                        translator.translate("SorryButReplayInvalid")
                            .replace("{}", translator.translate(replayingGame.getCurrentGameNameCode()))
                            .replace("\\n", "\n"));
                    this.errorMessageLabel.setTextAlignment(TextAlignment.CENTER);
                    ColorAdjust colorAdjust = new ColorAdjust();
                    colorAdjust.setBrightness(-0.8);
                    this.centerPanel.setEffect(colorAdjust);
                    errorMessage.setOpacity(1);
                    errorMessage.setMouseTransparent(false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        return replayButton;
    }

    private CheckBox buildCategoryCheckBox(
        GameCategories.Category category,
        Configuration config,
        Translator translator,
        FlowPane choicePanel,
        List<Node> gameCardsList,
        TextField searchBar,
        ProgressIndicator dwellTimeIndicator
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
            filterGames(choicePanel, gameCardsList, favGameCardsList, config, dwellTimeIndicator, node -> {
                GameButtonPane gameButtonPane = (GameButtonPane) node;
                return (new GameCardVisiblePredicate(config)).test(node) &&
                    translator.translate(gameButtonPane.getGameSpec().getGameSummary().getNameCode()).toLowerCase().contains(searchBar.getText().toLowerCase());
            });
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
