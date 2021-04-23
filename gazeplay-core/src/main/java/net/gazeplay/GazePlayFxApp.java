package net.gazeplay;

import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.cli.*;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.latestnews.LatestNewsPopup;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class GazePlayFxApp extends Application {

    @Setter
    private static ApplicationContext applicationContext;

    @Setter
    private static ReusableOptions applicationOptions;

    private ReusableOptions options;

    private GazePlay gazePlay;

    private GameMenuController gameMenuController;

    private GamesLocator gamesLocator;

    private Translator translator;

    @Override
    public void init() {
        final ApplicationContext context = applicationContext;
        this.options = applicationOptions;
        //
        log.info("options = {}", options);
        //
        gazePlay = context.getBean(GazePlay.class);
        gameMenuController = context.getBean(GameMenuController.class);
        gamesLocator = context.getBean(GamesLocator.class);
        translator = context.getBean(Translator.class);
    }

    @Override
    public void start(final Stage primaryStage) {

        final Configuration mainConfig = ActiveConfigurationContext.getInstance();

        boolean showUserSelectPage = true;
        if (options != null) {
            final UserSelectionOptions userSelectionOptions = options.getUserSelectionOptions();
            if (userSelectionOptions != null) {
                if (userSelectionOptions.getUserid() != null) {
                    showUserSelectPage = false;
                    ActiveConfigurationContext.switchToUser(userSelectionOptions.getUserid());
                }
                if (userSelectionOptions.isDefaultUser()) {
                    showUserSelectPage = false;
                    ActiveConfigurationContext.switchToDefaultUser();
                }
            }
        }
        log.info("showUserSelectPage = {}", showUserSelectPage);

        final Scene primaryScene;
        if (options != null) {
            final SizeOptions sizeOptions = options.getSizeOptions();
            if (sizeOptions != null) {
                if (sizeOptions.getGameHeight() > 0 && sizeOptions.getGameWidth() > 0) {
                    primaryScene = createPrimaryScene(primaryStage, sizeOptions.getGameWidth(), sizeOptions.getGameHeight());
                } else {
                    autosize(primaryStage);
                    primaryScene = createPrimaryScene(primaryStage);
                }
            } else {
                autosize(primaryStage);
                primaryScene = createPrimaryScene(primaryStage);
            }
        } else {
            autosize(primaryStage);
            primaryScene = createPrimaryScene(primaryStage);
        }

        configurePrimaryStage(primaryStage);

        LatestNewsPopup.displayIfNeeded(mainConfig, gazePlay.getTranslator(), gazePlay.getCurrentScreenDimensionSupplier());

        gazePlay.setPrimaryScene(primaryScene);
        gazePlay.setPrimaryStage(primaryStage);
        primaryStage.setOnCloseRequest((e) -> {
            BackgroundMusicManager.getInstance().stop();
        });

        if (showUserSelectPage) {
            gazePlay.goToUserPage();
        } else {
            log.info("options = {}", options);
            final GameSelectionOptions gameSelectionOptions = options.getGameSelectionOptions();
            if (gameSelectionOptions != null) {
                final List<GameSpec> gameSpecs = gamesLocator.listGames(translator);
                String selectedGameNameCode = gameSelectionOptions.getGameNameCode();
                if (selectedGameNameCode == null) {
                    if (gameSelectionOptions.isRandomGame()) {
                        final ReplayablePseudoRandom random = new ReplayablePseudoRandom();
                        final int randomGameIndex = random.nextInt(gameSpecs.size());
                        final GameSpec selectedGameSpec = gameSpecs.get(randomGameIndex);
                        selectedGameNameCode = selectedGameSpec.getGameSummary().getNameCode();
                    }
                }

                String selectedVariantCode = options.getVariantSelectionOptions().getGameVariant();
                if (selectedGameNameCode != null) {
                    final String searchGameNameCode = selectedGameNameCode;
                    final GameSpec selectedGameSpec = gameSpecs.stream()
                        .filter(gameSpec -> gameSpec.getGameSummary().getNameCode().equals(searchGameNameCode))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(searchGameNameCode));

                    log.info("gameSpecs = {}", gameSpecs);

                    ReplayJsonFileOptions replayJsonFileOptions = options.getReplayJsonFileOptions();
                    if (replayJsonFileOptions != null) {
                        String replayFileName = replayJsonFileOptions.getJsonFileName();
                        if (replayFileName != null) {
                            final List<GameSpec> games = gamesLocator.listGames(gazePlay.getTranslator());
                            ReplayingGameFromJson replayingGameFromJson = new ReplayingGameFromJson(gazePlay, applicationContext, games);
                            try {
                                replayingGameFromJson.pickJSONFile(replayJsonFileOptions.getJsonFileName());
                                replayingGameFromJson.drawLines();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            if (selectedVariantCode != null) {
                                IGameVariant variant = IGameVariant.toGameVariant(selectedVariantCode);
                                gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, variant);
                            } else {
                                gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, null);
                            }
                        }
                    } else {
                        if (selectedVariantCode != null) {
                            IGameVariant variant = IGameVariant.toGameVariant(selectedVariantCode);
                            gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, variant);
                        } else {
                            gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, null);
                        }
                    }
                } else {
                    gazePlay.onReturnToMenu();
                }
            } else {
                gazePlay.onReturnToMenu();
            }
        }

        File f = new File(GazePlayDirectories.getGazePlayFolder() + "/TokenLauncher");
        if (f.exists()) {
            if (!f.delete()) {
                log.warn("Token File has not been deleted !");
            }
        }

        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), gazePlay.getPrimaryScene(), gazePlay.getCurrentScreenDimensionSupplier());
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private Scene createPrimaryScene(final Stage primaryStage) {
        final Pane rootPane = new Pane();
        final Scene primaryScene = new Scene(rootPane, primaryStage.getWidth(), primaryStage.getHeight(), Color.BLACK);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), primaryScene, gazePlay.getCurrentScreenDimensionSupplier());
        primaryStage.setScene(primaryScene);
        return primaryScene;
    }

    private Scene createPrimaryScene(final Stage primaryStage, final double width, final double height) {
        final Pane rootPane = new Pane();
        final Scene primaryScene = new Scene(rootPane, width, height, Color.BLACK);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), primaryScene, gazePlay.getCurrentScreenDimensionSupplier());
        primaryStage.setScene(primaryScene);
        return primaryScene;
    }

    private void configurePrimaryStage(final Stage primaryStage) {
        primaryStage.setTitle("GazePlay");
        primaryStage.setOnCloseRequest((WindowEvent we) -> primaryStage.close());

        final String iconImagePath = "data/common/images/gazeplayicon.png";
        final Image icon = new Image(iconImagePath);
        primaryStage.getIcons().add(icon);
    }

    private void autosize(final Stage primaryStage) {
        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        primaryStage.setWidth(screenDimension.getWidth());
        primaryStage.setHeight(screenDimension.getHeight());
        primaryStage.setMaximized(true);

        primaryStage.setFullScreen(true);
    }

}
