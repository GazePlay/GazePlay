package net.gazeplay;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.ImageDirectoryLocator;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.components.CssUtil;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

/**
 * Created by schwab on 17/12/2016.
 */
@Slf4j
public class GazePlay extends Application {

    @Getter
    private static GazePlay instance;

    @Getter
    @Setter
    private HomeMenuScreen homeMenuScreen;

    @Getter
    private UserProfilContext userProfileScreen;

    @Getter
    private Stage primaryStage;

    @Getter
    private LoadingScreen lds;

    @Getter
    private Translator translator;

    @Getter
    private Scene primaryScene;

    public GazePlay() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        LatestNewPopup.displayIfNeeded();

        String iconUrl = "data/common/images/gazeplayicon.png";
        Image icon = findApplicationIcon(iconUrl);

        if (icon != null)
            this.primaryStage.getIcons().add(icon);

        Screen screen = Screen.getPrimary();

        Rectangle2D screenBounds = screen.getBounds();

        primaryStage.setWidth(screenBounds.getWidth() * 0.95);
        primaryStage.setHeight(screenBounds.getHeight() * 0.90);

        primaryStage.setMaximized(false);

        Configuration config = Configuration.getInstance();
        final Multilinguism multilinguism = Multilinguism.getSingleton();

        translator = new DefaultTranslator(config, multilinguism);

        homeMenuScreen = HomeMenuScreen.newInstance(this, config);

        this.primaryScene = new Scene(homeMenuScreen.getRoot(), primaryStage.getWidth(), primaryStage.getHeight(),
            Color.BLACK);

        CssUtil.setPreferredStylesheets(config, primaryScene);

        primaryStage.setTitle("GazePlay");

        primaryStage.setScene(primaryScene);

        primaryStage.setOnCloseRequest((WindowEvent we) -> primaryStage.close());

        userProfileScreen = UserProfilContext.newInstance(this, config);

        lds = LoadingScreen.newInstance(this);

        userProfileScreen.setUpOnStage(primaryScene);

        primaryStage.setFullScreen(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        this.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.SPACE && Configuration.getInstance().isGazeMouseEnable()) {
                Platform.runLater(() -> {
                    try {
                        Robot robot = new Robot();
                        robot.mousePress(InputEvent.BUTTON1_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    } catch (AWTException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
            }
        });

        this.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.S && Configuration.getInstance().isGazeMouseEnable()) {
                Configuration.setMouseFree(!Configuration.isMouseFree());
            }
        });
    }

    public void onGameLaunch(GameContext gameContext) {

        gameContext.setUpOnStage(primaryScene);
        gameContext.updateMusicControler();
    }

    public void onReturnToMenu() {

        homeMenuScreen.setGazeDeviceManager(GazeDeviceManagerFactory.getInstance().createNewGazeListener());
        homeMenuScreen.setUpOnStage(primaryScene);
        final BackgroundMusicManager musicMananger = BackgroundMusicManager.getInstance();
        musicMananger.onEndGame();

        // log.info("here is the list of pausedEvent = {}", homeMenuScreen.getGameMenuFactory().getPausedEvents());

        // homeMenuScreen.updateMusicControler();
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

    public void loading() {
        lds.setUpOnStage(primaryScene);
        primaryStage.show();

    }

    public void goToUserPage() {

        Configuration.setCONFIGPATH(Utils.getGazePlayFolder() + "GazePlay.properties");
        Configuration.setInstance(Configuration.createFromPropertiesResource());

        Configuration config = Configuration.getInstance();

        if (getTranslator() instanceof DefaultTranslator) {
            ((DefaultTranslator) getTranslator()).setConfig(config);
        }

        CssUtil.setPreferredStylesheets(config, getPrimaryScene());

        BackgroundMusicManager.getInstance().stop();

        BackgroundMusicManager.setInstance(new BackgroundMusicManager());

        userProfileScreen = UserProfilContext.newInstance(this, config);
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

    Image findApplicationIcon(String iconLocation) {
        Image icon = null;

        try {
            icon = new Image(iconLocation);
            log.debug("findApplicationIcon : icon found at location : {}", iconLocation);
        } catch (IllegalArgumentException ie) {
            log.debug("findApplicationIcon : icon not be found at location : {}", iconLocation);

            File iconImageDirectory = ImageDirectoryLocator
                .locateImagesDirectoryInUnpackedDistDirectory(
                    "data/common/images/");
            if (iconImageDirectory != null) {
                try {
                    String filePath = iconImageDirectory.getCanonicalPath() + Utils.FILESEPARATOR + "gazeplayicon.png";
                    log.debug("findApplicationIcon : looking for icon at location = " + filePath);
                    icon = new Image(filePath);
                } catch (IOException ioe) {
                    log.debug("findApplicationIcon : image directory {} is invalid. - {}", iconImageDirectory.getAbsolutePath(), ioe.toString());
                } catch (IllegalArgumentException iae) {
                    log.debug("findApplicationIcon : image could not be found. - {}", iae.toString());
                } catch (Exception e) {
                    log.debug("findApplicationIcon : something unexpected happened - {}", e.toString());
                }
            }
        }

        return icon;
    }

}
