package net.gazeplay;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.SystemInfo;

/**
 * Created by schwab on 17/12/2016.
 */
@Slf4j
public class GazePlay extends Application {

    @Getter
    private static GazePlay instance;

    @Getter
    private HomeMenuScreen homeMenuScreen;

    @Getter
    private Stage primaryStage;

    @Getter
    private Translator translator;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public GazePlay() {
        instance = this;
    }

    @Getter
    private BooleanProperty gazeMenuActivated;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        gazeMenuActivated = new SimpleBooleanProperty();
        gazeMenuActivated.setValue(false);

        Screen screen = Screen.getPrimary();

        Rectangle2D screenBounds = screen.getBounds();

        primaryStage.setWidth(screenBounds.getWidth() * 0.95);
        primaryStage.setHeight(screenBounds.getHeight() * 0.90);

        primaryStage.setMaximized(false);

        final Configuration config = Configuration.getInstance();
        final Multilinguism multilinguism = Multilinguism.getSingleton();

        translator = new DefaultTranslator(config, multilinguism);

        homeMenuScreen = HomeMenuScreen.newInstance(this, config);
        homeMenuScreen.setUpOnStage(primaryStage);

        primaryStage.centerOnScreen();

        primaryStage.setFullScreen(true);
    }

    public void onGameLaunch(GameContext gameContext) {
        gameContext.setUpOnStage(primaryStage);
    }

    public void onReturnToMenu() {
        homeMenuScreen.setUpOnStage(primaryStage);
        log.info("Mem info : {}", SystemInfo.MemInfo());
    }

    public void onDisplayStats(StatsContext statsContext) {
        statsContext.setUpOnStage(primaryStage);
    }

    public void onDisplayConfigurationManagement(ConfigurationContext configurationContext) {
        configurationContext.setUpOnStage(primaryStage);
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
