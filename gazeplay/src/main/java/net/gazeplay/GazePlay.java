package net.gazeplay;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;

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

    public GazePlay() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        homeMenuScreen = new HomeMenuScreen(this, ConfigurationBuilder.createFromPropertiesResource().build());
        homeMenuScreen.setUpOnStage(primaryStage);
    }

    public void onGameLaunch(GameContext gameContext) {
        gameContext.setUpOnStage(primaryStage);
    }

    public void onReturnToMenu() {
        homeMenuScreen.setUpOnStage(primaryStage);
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
        primaryStage.show();
    }

}
