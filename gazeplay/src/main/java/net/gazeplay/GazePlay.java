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
import net.gazeplay.ui.scenes.gamemenu.HomeMenuScreen;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.stats.AreaOfInterest;
import net.gazeplay.ui.scenes.stats.ScanpathView;
import net.gazeplay.ui.scenes.stats.StatsContext;
import net.gazeplay.ui.scenes.userselect.UserProfileContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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

        CssUtil.setPreferredStylesheets(config, getPrimaryScene(), getCurrentScreenDimensionSupplier());

        BackgroundMusicManager.onConfigurationChanged();

        UserProfileContext userProfileScreen = applicationContext.getBean(UserProfileContext.class);
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


    public void pickJSONFile(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }

//        final JFrame frame = new JFrame("Open File Example");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLayout(new BorderLayout());
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "JSON files", "json");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        }
//        frame.pack();
//        frame.setLocationByPlatform(true);
//        frame.setVisible(true);


    }

}
