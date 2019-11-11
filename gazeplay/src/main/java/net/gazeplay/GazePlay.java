package net.gazeplay;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
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
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GazePlay {

    @Setter
    @Getter
    private Stage primaryStage;

    @Setter
    @Getter
    private Scene primaryScene;

    @Getter
    private Translator translator;

    @Autowired
    @Getter
    private GamesLocator gamesLocator;

    public GazePlay() {
        Configuration config = ActiveConfigurationContext.getInstance();
        final Multilinguism multilinguism = Multilinguism.getSingleton();

        translator = new DefaultTranslator(config, multilinguism);
    }

    public void onGameLaunch(GameContext gameContext) {
        gameContext.setUpOnStage(primaryScene);
    }

    public void onReturnToMenu() {

        List<GameSpec> games = gamesLocator.listGames(translator);

        BorderPane root = new BorderPane();

        HomeMenuScreen homeMenuScreen = new HomeMenuScreen(this, games, root, ActiveConfigurationContext.getInstance());

        homeMenuScreen.setGazeDeviceManager(GazeDeviceManagerFactory.getInstance().createNewGazeListener());
        homeMenuScreen.setUpOnStage(primaryScene);
        BackgroundMusicManager.getInstance().onEndGame();
    }

    public void onDisplayStats(StatsContext statsContext) {
        statsContext.setUpOnStage(primaryScene);
    }

    public void onDisplayAOI(AreaOfInterest areaOfInterest) {
        areaOfInterest.setUpOnStage(primaryScene);
    }

    public void onDisplayScanpath(ScanpathView scanPath) {
        scanPath.setUpOnStage(primaryScene);
    }

    public void onDisplayConfigurationManagement(ConfigurationContext configurationContext) {
        configurationContext.setUpOnStage(primaryScene);
    }

    public void goToUserPage() {
        ActiveConfigurationContext.switchToDefaultUser();

        Configuration config = ActiveConfigurationContext.getInstance();

        getTranslator().notifyLanguageChanged();

        CssUtil.setPreferredStylesheets(config, getPrimaryScene());

        BackgroundMusicManager.onConfigurationChanged();

        UserProfilContext userProfileScreen = UserProfilContext.newInstance(this, config);
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

}
