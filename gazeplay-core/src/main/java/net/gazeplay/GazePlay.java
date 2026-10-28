package net.gazeplay;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.CssUtil;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.evalmenu.EvalMenuScreen;
import net.gazeplay.ui.scenes.gamemenu.HomeMenuScreen;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.modeselect.ModeSelectionContext;
import net.gazeplay.ui.scenes.stats.AreaOfInterest;
import net.gazeplay.ui.scenes.stats.ScanpathView;
import net.gazeplay.ui.scenes.stats.StatsContext;
import net.gazeplay.ui.scenes.userselect.UserProfileContext;
import java.util.function.Supplier;

/**
 * The application seen by its screens: the window, its scene, and the navigation between the screens (mode selection,
 * user selection, menu, settings, game, statistics, areas of interest, scan path). Each screen is put on the same scene
 * with its setUpOnStage method. The mode of the session ({@link AppMode}: games or evaluations) chooses the menu and the
 * settings shown.
 */
@Slf4j
public class GazePlay {

    /**
     * Creates the screens of the application, each time they are displayed (see {@link CompositionRoot}).
     */
    public interface Screens {
        HomeMenuScreen newHomeMenuScreen();

        ConfigurationContext newConfigurationContext();

        UserProfileContext newUserProfileContext();

        ModeSelectionContext newModeSelectionContext();

        EvalMenuScreen newEvalMenuScreen();
    }

    @Setter
    @Getter
    private Stage primaryStage;

    @Setter
    @Getter
    private Scene primaryScene;

    @Getter
    private final Translator translator;

    @Getter
    private final GamesLocator gamesLocator;

    /**
     * Set after construction: the supplier itself depends on the stage of this object.
     */
    @Getter
    @Setter
    private Supplier<Dimension2D> currentScreenDimensionSupplier;

    private final Screens screens;

    /**
     * The mode of the session: the games until another one is chosen (e.g. started with --user or --default-user).
     */
    @Getter
    private AppMode mode = AppMode.GAMES;

    HomeMenuScreen homeMenuScreen;
    EvalMenuScreen evalMenuScreen;
    ConfigurationContext configurationContext;
    UserProfileContext userProfileScreen;

    Boolean loadedHomePage = false;
    Boolean loadedSettingsPage = false;

    public GazePlay(final Translator translator, final GamesLocator gamesLocator, final Screens screens) {
        this.translator = translator;
        this.gamesLocator = gamesLocator;
        this.screens = screens;
    }

    /**
     * Shows the game screen.
     */
    public void onGameLaunch(GameContext gameContext) {
        gameContext.setUpOnStage(primaryScene);
    }

    /**
     * Shows the menu of the mode (the games or the evaluations); the background music of the menu comes back.
     */
    public void onReturnToMenu() {
        this.loadHomePage();
        if (mode == AppMode.EVALUATIONS) {
            evalMenuScreen.setUpOnStage(primaryScene);
        } else {
            homeMenuScreen.setUpOnStage(primaryScene);
        }
        BackgroundMusicManager.getInstance().onEndGame();
    }

    /**
     * Shows the page choosing the mode (games or evaluations), at startup and from the profile page.
     */
    public void goToModePage() {
        ModeSelectionContext modeSelectionContext = screens.newModeSelectionContext();
        modeSelectionContext.setUpOnStage(primaryScene);
        primaryStage.show();
    }

    /**
     * Chooses the mode of the session, then shows the profile page. The menus and the settings page are built again for
     * this mode.
     */
    public void chooseMode(AppMode mode) {
        log.info("Mode = {}", mode);
        this.mode = mode;
        goToUserPage();
    }

    /**
     * Shows the statistics screen of a game.
     */
    public void onDisplayStats(StatsContext statsContext) {
        statsContext.setUpOnStage(primaryScene);
    }

    /**
     * Shows the areas of interest of a game, of the gaze if it has any, else of the mouse.
     */
    public void onDisplayAOI(Stats stats) {
        onDisplayAOI(stats, AreaOfInterest.defaultSource(stats));
    }

    /**
     * Shows the areas of interest of a game for one source.
     */
    public void onDisplayAOI(Stats stats, AreaOfInterest.Source source) {
        AreaOfInterest areaOfInterest = new AreaOfInterest(this, stats, source);
        areaOfInterest.setUpOnStage(primaryScene);
    }

    /**
     * Shows the scan path (fixation sequence) of a game.
     */
    public void onDisplayScanpath(ScanpathView scanPath) {
        scanPath.setUpOnStage(primaryScene);
    }

    /**
     * Shows the settings page.
     */
    public void onDisplayConfigurationManagement() {
        this.loadSettingsPage();
        configurationContext.setUpOnStage(primaryScene);
    }

    /**
     * Goes back to the user selection page: the default profile becomes the active one again, with its language and
     * theme.
     */
    public void goToUserPage() {
        ActiveConfigurationContext.switchToDefaultUser();

        Configuration config = ActiveConfigurationContext.getInstance();

        translator.notifyLanguageChanged();

        CssUtil.setPreferredStylesheets(config, getPrimaryScene(), getCurrentScreenDimensionSupplier());

        BackgroundMusicManager.onConfigurationChanged();

        this.loadedHomePage = false;
        this.loadedSettingsPage = false;

        userProfileScreen = screens.newUserProfileContext();
        userProfileScreen.setUpOnStage(primaryScene);
        primaryStage.show();
    }

    /**
     * Quits GazePlay (exit buttons of the menus).
     */
    public void quit() {
        System.exit(0);
    }

    /**
     * Switches the window between full screen and windowed. Only a windowed window is centered: centerOnScreen places
     * a window in the visual bounds of the screen (without the taskbar), a third of the way down, so a window of the size
     * of the screen moved up, its top cut and the taskbar seen below it.
     */
    public void toggleFullScreen() {

        boolean fullScreen = !primaryStage.isFullScreen();
        log.info("fullScreen = {}", fullScreen);
        primaryStage.setFullScreen(fullScreen);
        if (!fullScreen) {
            primaryStage.centerOnScreen();
        }
        primaryStage.show();
    }

    public boolean isFullScreen() {
        return primaryStage.isFullScreen();
    }

    public ReadOnlyBooleanProperty getFullScreenProperty() {
        return primaryStage.fullScreenProperty();
    }

    /**
     * Creates the menu of the mode (and the settings page) at the first call; creates the "music" folder with the
     * default music if needed.
     */
    public void loadHomePage() {
        if (!this.loadedHomePage) {
            this.loadedHomePage = true;
            BackgroundMusicManager.getInstance().checkDefaultMusic();
            this.loadSettingsPage();
            if (mode == AppMode.EVALUATIONS) {
                evalMenuScreen = screens.newEvalMenuScreen();
            } else {
                homeMenuScreen = screens.newHomeMenuScreen();
            }
        }
    }

    /**
     * Builds the menu again when it is shown next, e.g. after a change of the orientation of its cards in the settings.
     */
    public void refreshMenu() {
        this.loadedHomePage = false;
    }

    /**
     * Creates the settings page at the first call.
     */
    public void loadSettingsPage() {
        if (!this.loadedSettingsPage) {
            this.loadedSettingsPage = true;
            configurationContext = screens.newConfigurationContext();
        }
    }
}
