package net.gazeplay.ui.scenes.gamemenu;

import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.*;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.soundsmanager.SoundManager;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.menus.MenuButtons;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The menu of the games (look of the menus, menus.css, REFONTE_GRAPHIQUE.md 4.3):
 * <ul>
 *     <li>a top bar: the logo, the search bar, then the profile (a click goes back to the profiles), replay, settings,
 *     full screen and quit;</li>
 *     <li>the categories as switches (chips), saved in the HIDDEN_CATEGORIES setting, after a "Favorites" switch
 *     showing only the favorite games;</li>
 *     <li>the cards of the games; arrows above and below scroll the list while the pointer is on them;</li>
 *     <li>a bottom bar: the music controls, the number of games and the version (opening the licence).</li>
 * </ul>
 */
@Slf4j
public class HomeMenuScreen extends GraphicalContext<BorderPane> {

    private static final double LOGO_HEIGHT = 64;

    /**
     * Below this width of the window, the top bar has no logo.
     */
    private static final double NARROW_WIDTH = 1100;

    /**
     * Below this height of the window, no scroll arrows nor statistics and version.
     */
    private static final double LOW_HEIGHT = 720;

    private final SoundManager soundManager;

    private final GameMenuFactory gameMenuFactory;

    private final ThumbnailImage thumbnailImage;

    /**
     * The cards of all the games, in the order of the catalog.
     */
    private List<GameButtonPane> gameCardsList;

    private FlowPane allGamesPanel;
    private Label noGameLabel;
    private TextField searchBar;

    /**
     * Whether the "Favorites" switch shows only the favorite games.
     */
    private boolean onlyFavorites;

    /**
     * Kept here: the setting holds it weakly.
     */
    private InvalidationListener favoritesListener;

    public GamesStatisticsPane gamesStatisticsPane;
    private ScrollPane choicePanelScroller;
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
        this.thumbnailImage = new ThumbnailImage();

        gazeplayHomeMenuScreen(gazePlay, gamesLocator);
    }

    /**
     * Builds the menu.
     */
    public void gazeplayHomeMenuScreen(GazePlay gazePlay, GamesLocator gamesLocator) {
        MenuStyle.applyTo(root);
        final Translator translator = gazePlay.getTranslator();
        final Configuration config = ActiveConfigurationContext.getInstance();

        // The evaluations have their own menu (mode "Evaluations"); a replay may concern any entry of the catalog
        final List<GameSpec> allGames = gamesLocator.listGames(translator);
        final List<GameSpec> games = AppMode.GAMES.filter(allGames);

        searchBar = buildSearchBar(config, translator);
        final Node gamePickerChoicePane = createGamePickerChoicePane(games, config, translator);

        final Node upArrow = createArrow(MenuIcon.ARROW_UP, -0.002);
        final Node downArrow = createArrow(MenuIcon.ARROW_DOWN, 0.002);
        centerPanel = new VBox(upArrow, gamePickerChoicePane, downArrow);
        centerPanel.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(gamePickerChoicePane, Priority.ALWAYS);

        final Node logo = LogoFactory.getInstance().createLogoStatic(LOGO_HEIGHT);
        final VBox top = new VBox(createTopBar(gazePlay, translator, allGames, logo),
            buildCategoryFilters(config, translator));

        final StackPane centerStackPane = new StackPane(centerPanel, createErrorMessage());

        gamesStatisticsPane = new GamesStatisticsPane(translator, games, MenuButtons.currentProfileName(translator));
        final Node bottomInfo = createBottomInfo(gazePlay, translator);

        root.setTop(top);
        root.setCenter(centerStackPane);
        root.setBottom(createBottomBar(translator, config, bottomInfo));
        adaptToSmallWindows(logo, upArrow, downArrow, bottomInfo);
    }

    /**
     * Keeps the menu usable down to 800 x 600: the logo of the top bar is hidden on a narrow window, the arrows and the
     * information of the bottom bar on a low one (the music stays).
     */
    private void adaptToSmallWindows(final Node logo, final Node... hiddenWhenLow) {
        root.widthProperty().addListener((observable, oldValue, newValue) ->
            setShown(logo, newValue.doubleValue() >= NARROW_WIDTH));
        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            for (final Node node : hiddenWhenLow) {
                setShown(node, newValue.doubleValue() >= LOW_HEIGHT);
            }
        });
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    /**
     * @return the top bar: logo, search bar, profile, replay, settings, full screen, quit
     */
    private HBox createTopBar(final GazePlay gazePlay, final Translator translator, final List<GameSpec> allGames,
                              final Node logo) {
        final StackPane searchIcon = MenuIcon.SEARCH.create(24);
        final HBox search = new HBox(8, searchIcon, searchBar);
        search.getStyleClass().add("gp-search");
        search.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        search.setMaxWidth(420);
        HBox.setHgrow(search, Priority.SOMETIMES);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filled round buttons in color, like quit, with their tooltip (maintainer's feedback of 28/10/2026); the
        // logout goes back to the profiles, the current profile being shown at the bottom right
        final Button replayButton = MenuStyle.roundButton(MenuIcon.REPLAY, "teal", translator.translate("Replay"),
            () -> replayGame(gazePlay, allGames));
        final Button settingsButton = MenuStyle.roundButton(MenuIcon.SETTINGS, "sky", translator.translate("Options"),
            gazePlay::onDisplayConfigurationManagement);
        final Button fullScreenButton = MenuButtons.fullScreenButton(gazePlay, translator);
        final Button logoutButton = MenuStyle.roundButton(MenuIcon.LOGOUT, "accent", translator.translate("Logout"),
            gazePlay::goToUserPage);
        final Button exitButton = MenuStyle.quitButton(translator.translate("Exit"), gazePlay::quit);

        // Settings first, as in the menu of the evaluations (maintainer's feedback of 28/10/2026)
        final HBox topBar = new HBox(logo, search, spacer, settingsButton, replayButton, fullScreenButton,
            MenuButtons.themeButton(gazePlay, translator), logoutButton, exitButton);
        topBar.getStyleClass().add("gp-top-bar");
        return topBar;
    }

    /**
     * @return an arrow scrolling the games while the pointer (or the gaze) is on it, and by a page on a click
     */
    private Node createArrow(final MenuIcon icon, final double step) {
        final Button arrow = MenuStyle.iconButton(icon, "", () -> choicePanelScroller.setVvalue(
            choicePanelScroller.getVvalue() + Math.signum(step) * pageStep()));
        arrow.getStyleClass().add("gp-scroll-arrow");
        arrow.setFocusTraversable(false);
        arrow.setTooltip(null);
        final AnimationTimer scrollTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                choicePanelScroller.setVvalue(choicePanelScroller.getVvalue() + step);
            }
        };
        arrow.addEventHandler(MouseEvent.MOUSE_ENTERED, (EventHandler<Event>) e -> scrollTimer.start());
        arrow.addEventHandler(MouseEvent.MOUSE_EXITED, (EventHandler<Event>) e -> scrollTimer.stop());
        arrow.addEventHandler(GazeEvent.GAZE_ENTERED, (EventHandler<Event>) e -> scrollTimer.start());
        arrow.addEventHandler(GazeEvent.GAZE_EXITED, (EventHandler<Event>) e -> scrollTimer.stop());
        final StackPane pane = new StackPane(arrow);
        pane.setPadding(new Insets(4));
        return pane;
    }

    /**
     * @return the part of the list scrolled by a click on an arrow: the height of the visible part
     */
    private double pageStep() {
        final double content = choicePanelScroller.getContent().getBoundsInLocal().getHeight();
        final double viewport = choicePanelScroller.getViewportBounds().getHeight();
        return content <= viewport ? 0 : viewport / (content - viewport) * 0.8;
    }

    /**
     * @return the scrolling pane of the cards of the games, with a message when none is shown
     */
    private ScrollPane createGamePickerChoicePane(
        final List<GameSpec> games,
        final Configuration config,
        final Translator translator
    ) {
        final GameButtonOrientation orientation = GameButtonOrientation.fromConfig(config);
        gameCardsList = new ArrayList<>();
        for (final GameSpec gameSpec : games) {
            gameCardsList.add(createGameCard(config, gameSpec, translator, orientation));
        }

        allGamesPanel = new FlowPane(20, 20);
        allGamesPanel.setAlignment(Pos.TOP_CENTER);
        noGameLabel = new Label(translator.translate("NoGameFound"));
        noGameLabel.getStyleClass().add("gp-subtitle");
        noGameLabel.setWrapText(true);

        final VBox content = new VBox(12, allGamesPanel, noGameLabel);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(16, 48, 32, 48));

        choicePanelScroller = new ScrollPane(content);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // With the "Favorites" switch, a game whose heart is clicked appears or disappears at once
        favoritesListener = observable -> filterGames(config, translator);
        config.getFavoriteGamesProperty().addListener(new WeakInvalidationListener(favoritesListener));

        filterGames(config, translator);
        return choicePanelScroller;
    }

    /**
     * @return the card of a game
     */
    private GameButtonPane createGameCard(final Configuration config, GameSpec gameSpec, final Translator translator,
                                          GameButtonOrientation gameButtonOrientation) {
        return gameMenuFactory.createGameButton(
            getGazePlay(),
            root,
            config,
            translator,
            gameSpec,
            gameButtonOrientation,
            thumbnailImage);
    }

    /**
     * Shows the cards of the games of the visible categories that match the search bar; with the "Favorites" switch,
     * only the favorite games (no row of their own: maintainer's decision of 28/10/2026).
     */
    private void filterGames(final Configuration config, final Translator translator) {
        final Predicate<GameButtonPane> visible = new GameCardVisiblePredicate(config).and(card ->
            translator.translate(card.getGameSpec().getGameSummary().getNameCode()).toLowerCase()
                .contains(searchBar.getText().toLowerCase()));

        final List<GameButtonPane> shownGames = gameCardsList.stream().filter(visible)
            .filter(card -> !onlyFavorites || isFavorite(card.getGameSpec(), config))
            .collect(Collectors.toList());

        allGamesPanel.getChildren().setAll(shownGames);
        noGameLabel.setText(translator.translate(onlyFavorites ? "NoFavoriteGame" : "NoGameFound"));
        setShown(noGameLabel, shownGames.isEmpty());
    }

    private static void setShown(final Node node, final boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    /**
     * @return the search bar, filtering the games by their translated name as it is typed
     */
    private TextField buildSearchBar(Configuration config, Translator translator) {
        TextField gameSearchBar = new TextField();
        Utils.addTextLimiter(gameSearchBar, 32);
        gameSearchBar.setPromptText(translator.translate("SearchGame"));
        gameSearchBar.textProperty().addListener((obs, oldValue, newValue) -> {
            log.debug(newValue);
            filterGames(config, translator);
        });
        return gameSearchBar;
    }

    /**
     * @return one switch (chip) per category, with its icon; the hidden categories are saved in the HIDDEN_CATEGORIES
     * setting
     */
    private FlowPane buildCategoryFilters(Configuration config, Translator translator) {
        final FlowPane categoryFilters = new FlowPane(10, 10);
        categoryFilters.getStyleClass().add("gp-chips");
        categoryFilters.setAlignment(Pos.CENTER);
        categoryFilters.getChildren().add(buildFavoritesChip(config, translator));
        for (GameCategories.Category category : GameCategories.Category.values()) {
            categoryFilters.getChildren().add(buildCategoryChip(category, config, translator));
        }
        return categoryFilters;
    }

    /**
     * @return the switch showing only the favorite games (maintainer's feedback of 28/10/2026), off at each start of
     * the menu: not saved, unlike the categories
     */
    private ToggleButton buildFavoritesChip(Configuration config, Translator translator) {
        final ToggleButton chip = new ToggleButton(translator.translate("FavoriteGames"),
            MenuIcon.FAVORITE_FILLED.create(20));
        chip.getStyleClass().addAll("gp-chip", "gp-chip-favorites");
        chip.setTooltip(MenuStyle.tooltip(translator.translate("FavoriteOnly")));
        chip.selectedProperty().addListener((o) -> {
            onlyFavorites = chip.isSelected();
            filterGames(config, translator);
        });
        return chip;
    }

    /**
     * @return the switch showing or hiding the games of a category
     */
    private ToggleButton buildCategoryChip(GameCategories.Category category, Configuration config, Translator translator) {
        final ToggleButton chip = new ToggleButton(translator.translate(category.getGameCategory()),
            GameMenuFactory.categoryIcon(category).create(20));
        chip.getStyleClass().addAll("gp-chip", GameMenuFactory.categoryStyleClass(category));
        chip.setSelected(!config.getHiddenCategoriesProperty().contains(category.getGameCategory()));
        chip.selectedProperty().addListener((o) -> {
            if (chip.isSelected()) {
                config.getHiddenCategoriesProperty().remove(category.getGameCategory());
            } else {
                config.getHiddenCategoriesProperty().add(category.getGameCategory());
            }
            filterGames(config, translator);
        });
        return chip;
    }

    /**
     * @return the number of games and the version (opening the licence)
     */
    private VBox createBottomInfo(final GazePlay gazePlay, final Translator translator) {
        final VBox info = new VBox(6, gamesStatisticsPane, MenuButtons.versionButton(gazePlay, translator));
        info.setAlignment(Pos.BOTTOM_RIGHT);
        return info;
    }

    /**
     * @return the bottom bar: music and volumes on the left, information on the right
     */
    private HBox createBottomBar(final Translator translator, final Configuration config, final Node info) {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final HBox bottomBar = new HBox(16,
            getMusicControl().createMusicControlPane(),
            getMusicControl().createVolumeLevelControlPane(config, translator),
            spacer, info);
        bottomBar.getStyleClass().add("gp-bottom-bar");
        return bottomBar;
    }

    /**
     * @return the message shown over the games when a replay is not possible; a click hides it
     */
    private StackPane createErrorMessage() {
        errorMessageLabel = new Label("Error message goes here");
        errorMessageLabel.setWrapText(true);
        errorMessageLabel.setTextAlignment(TextAlignment.CENTER);
        final StackPane card = new StackPane(errorMessageLabel);
        card.getStyleClass().add("gp-error-card");
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        errorMessage = new StackPane(card);
        errorMessage.setOnMouseClicked((event) -> {
            final Timeline opacityTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5),
                new KeyValue(errorMessage.opacityProperty(), 0, Interpolator.EASE_OUT)));
            opacityTimeline.setOnFinished(e -> errorMessage.setMouseTransparent(true));
            this.centerPanel.setEffect(null);
            opacityTimeline.play();
        });
        errorMessage.setOpacity(0);
        errorMessage.setMouseTransparent(true);
        return errorMessage;
    }

    /**
     * @return whether a game is in the favorites of the settings
     */
    private static boolean isFavorite(GameSpec g, Configuration configuration) {
        return configuration.getFavoriteGamesProperty().contains(g.getGameSummary().getNameCode());
    }

    /**
     * Asks for a replayData.json file and replays its game, if replayable (see {@code ReplayingGameFromJson});
     * otherwise shows a message over the games.
     */
    private void replayGame(final GazePlay gazePlay, final List<GameSpec> games) {
        try {
            ReplayingGameFromJson replayingGame = new ReplayingGameFromJson(gazePlay, gameMenuFactory.getGameMenuController().getGameContextFactory(), games);
            replayingGame.pickJSONFile(replayingGame.getFileName());
            if (ReplayingGameFromJson.replayIsAllowed(replayingGame.getCurrentGameNameCode())) {
                replayingGame.replayGame();
            } else if (replayingGame.getCurrentGameNameCode() != null) {
                Translator translator = gazePlay.getTranslator();
                this.errorMessageLabel.setText(
                    translator.translate("SorryButReplayInvalid")
                        .replace("{}", translator.translate(replayingGame.getCurrentGameNameCode()))
                        .replace("\\n", "\n"));
                ColorAdjust colorAdjust = new ColorAdjust();
                colorAdjust.setBrightness(-0.6);
                this.centerPanel.setEffect(colorAdjust);
                errorMessage.setOpacity(1);
                errorMessage.setMouseTransparent(false);
            }
        } catch (IOException ex) {
            log.warn("Cannot open the folder", ex);
        }
    }

    /**
     * Keeps the cards of the games having at least one visible category.
     */
    @AllArgsConstructor
    private static class GameCardVisiblePredicate implements Predicate<GameButtonPane> {

        private final Configuration config;

        @Override
        public boolean test(GameButtonPane gameButtonPane) {
            SortedSet<GameCategories.Category> gameCategories = gameButtonPane.getGameSpec().getGameSummary().getCategories();
            List<@NonNull String> gameCategoriesNames = gameCategories.stream().map(GameCategories.Category::getGameCategory).collect(Collectors.toList());
            return !config.getHiddenCategoriesProperty().containsAll(gameCategoriesNames);
        }

    }
}
