package net.gazeplay;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CachingSupplier;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.screen.PrimaryScreenDimensionSupplier;
import net.gazeplay.commons.utils.screen.PrimaryScreenSupplier;
import net.gazeplay.commons.utils.screen.ScreenDimensionSupplier;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.CssUtil;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.gamemenu.HomeMenuScreen;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.stats.AreaOfInterest;
import net.gazeplay.ui.scenes.stats.ScanpathView;
import net.gazeplay.ui.scenes.stats.StatsContext;
import net.gazeplay.ui.scenes.userselect.UserProfilContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class GazePlay {

    @Setter
    @Getter
    private Stage primaryStage;

    @Setter
    @Getter
    private Scene primaryScene;

    @Autowired
    @Getter
    private Translator translator;

    @Autowired
    @Getter
    private GamesLocator gamesLocator;

    @Autowired
    @Getter
    private PrimaryScreenDimensionSupplier primaryScreenDimensionSupplier;

    @Getter
    private Supplier<Dimension2D> currentScreenDimensionSupplier = new CachingSupplier<>(new ScreenDimensionSupplier(new CurrentScreenSupplier(this)));

    @Autowired
    private ApplicationContext applicationContext;

    public GazePlay() {
    }

    public void onGameLaunch(GameContext gameContext) {
        gameContext.setUpOnStage(primaryScene);
    }

    public void onReturnToMenu() {
        HomeMenuScreen homeMenuScreen = applicationContext.getBean(HomeMenuScreen.class);

        homeMenuScreen.setUpOnStage(primaryScene);
        BackgroundMusicManager.getInstance().onEndGame();
    }

    public void onDisplayStats(StatsContext statsContext) {
        statsContext.setUpOnStage(primaryScene);
    }

    public void onDisplayAOI(Stats stats) {
        AreaOfInterest areaOfInterest = new AreaOfInterest(this, stats);
        areaOfInterest.setUpOnStage(primaryScene);
    }

    public void onDisplayScanpath(ScanpathView scanPath) {
        scanPath.setUpOnStage(primaryScene);
    }

    public void onDisplayConfigurationManagement() {
        ConfigurationContext configurationContext = applicationContext.getBean(ConfigurationContext.class);
        configurationContext.setUpOnStage(primaryScene);
    }

    public void goToUserPage() {
        ActiveConfigurationContext.switchToDefaultUser();

        Configuration config = ActiveConfigurationContext.getInstance();

        translator.notifyLanguageChanged();

        CssUtil.setPreferredStylesheets(config, getPrimaryScene());

        BackgroundMusicManager.onConfigurationChanged();

        UserProfilContext userProfileScreen = applicationContext.getBean(UserProfilContext.class);
        userProfileScreen.setUpOnStage(primaryScene);
        primaryStage.show();
    }

    public void toggleFullScreen() {

        boolean fullScreen = !primaryStage.isFullScreen();
        log.info("fullScreen = {}", fullScreen);
        primaryStage.setFullScreen(fullScreen);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public boolean isFullScreen() {
        return primaryStage.isFullScreen();
    }

    public ReadOnlyBooleanProperty getFullScreenProperty() {
        return primaryStage.fullScreenProperty();
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class CurrentScreenSupplier implements Supplier<Screen> {

        @NonNull
        private final GazePlay gazePlay;

        private final PrimaryScreenSupplier primaryScreenSupplier = new PrimaryScreenSupplier();

        @Override
        public Screen get() {
            log.warn("get()");
            if (gazePlay.primaryScene == null) {
                log.warn("primaryScene is null");
                return primaryScreenSupplier.get();
            }
            //double x = gazePlay.primaryScene.getX();
            //double y = gazePlay.primaryScene.getY();

            double x;
            double y;

            Window window = gazePlay.primaryScene.getWindow();
            if (window == null) {
                log.warn("window is null");
                Stage primaryStage = gazePlay.getPrimaryStage();
                if (primaryStage == null) {
                    log.warn("primaryStage is null");
                    return primaryScreenSupplier.get();
                }
                x = primaryStage.getX() + 1;
                y = primaryStage.getY() + 1;
            } else {
                x = window.getX();
                y = window.getY();
            }

            log.info("x = {}, y = {}", x, y);
            ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(x, y, x, y);
            log.info("screensForRectangle.size() = {}", screensForRectangle.size());
            if (screensForRectangle.isEmpty()) {
                return primaryScreenSupplier.get();
            }
            Screen screen = screensForRectangle.get(0);
            log.info("current screen = {}", screen);
            return screen;
        }
    }
}
