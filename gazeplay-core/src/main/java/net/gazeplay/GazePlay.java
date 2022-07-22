package net.gazeplay;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.components.CssUtil;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.gamemenu.HomeMenuScreen;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.stats.AreaOfInterestContext;
import net.gazeplay.ui.scenes.stats.ScanpathContext;
import net.gazeplay.ui.scenes.stats.StatsContext;
import net.gazeplay.ui.scenes.userselect.UserProfileContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;
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
    @Lazy
    private Supplier<Dimension2D> currentScreenDimensionSupplier;

    @Autowired
    private ApplicationContext applicationContext;

    HomeMenuScreen homeMenuScreen;
    ConfigurationContext configurationContext;
    UserProfileContext userProfileScreen;

    Boolean loadedHomePage = false;
    Boolean loadedSettingsPage = false;
    Boolean loadedUserPage = false;

    public GazePlay() {
    }

    public void onGameLaunch(GameContext gameContext) {
        gameContext.setUpOnStage(primaryScene);
    }

    public void onReturnToMenu() {
        this.loadHomePage();
        homeMenuScreen.setUpOnStage(primaryScene);
        BackgroundMusicManager.getInstance().onEndGame();

        Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isFirstOpening()) {
            config.setFirstOpening(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Data collect information");
            alert.setHeaderText(
                "Your personal data (game statistics) is collected and sent to the development team in order to improve our services as closely as possible to your needs.\n" +
                    "This data is not intended for public release and will remain in the private sphere of the development team.\n" +
                    "If you do not want your personal data to be collected, you can deactivate the authorization at any time in the 'More Stats Settings' section in the 'Configuration' menu.");
            alert.show();
        }
    }

    public void onDisplayStats(StatsContext statsContext) {
        statsContext.setUpOnStage(primaryScene);
    }

    public void onDisplayAOI(AreaOfInterestContext areaOfInterest) {
        areaOfInterest.setUpOnStage(primaryScene);
    }

    public void onDisplayScanpath(ScanpathContext scanPath) {
        scanPath.setUpOnStage(primaryScene);
    }

    public void onDisplayConfigurationManagement() {
        this.loadSettingsPage();
        //configurationContext.resetPane(this);
        configurationContext.setUpOnStage(primaryScene);
    }

    public void goToUserPage() {
        ActiveConfigurationContext.switchToDefaultUser();

        Configuration config = ActiveConfigurationContext.getInstance();

        translator.notifyLanguageChanged();

        CssUtil.setPreferredStylesheets(config, getPrimaryScene(), getCurrentScreenDimensionSupplier());

        BackgroundMusicManager.onConfigurationChanged();

        this.loadUserPage();
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

    public void loadHomePage(){
        if (!this.loadedHomePage){
            this.loadedHomePage = true;
            homeMenuScreen = applicationContext.getBean(HomeMenuScreen.class);
        }
    }

    public void loadSettingsPage(){
        if (!this.loadedSettingsPage){
            this.loadedSettingsPage = true;
            configurationContext = applicationContext.getBean(ConfigurationContext.class);
        }
    }

    public void loadUserPage(){
        if (!this.loadedUserPage){
            this.loadedUserPage = true;
            userProfileScreen = applicationContext.getBean(UserProfileContext.class);
        }
    }
}
