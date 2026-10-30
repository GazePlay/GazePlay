package net.gazeplay;

import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
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
import net.gazeplay.commons.gamevariants.generators.NoVariantGenerator;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import net.gazeplay.ui.scenes.gamemenu.GameVariantDialog;
import net.gazeplay.ui.scenes.userselect.User;
import net.gazeplay.ui.scenes.userselect.UserProfileContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The JavaFX application. Without options it shows the choice of the mode (games or evaluations), then the user
 * selection page; the command line options (see {@link MainCommand}) can select the profile (the menu of the games is
 * then shown, as before the modes), start a game directly (with its variant, or show its variants), or replay a
 * replayData.json file. A game started from the menu runs in its own JVM, started this way.
 */
@Slf4j
public class GazePlayFxApp extends Application {

    // Static on purpose: JavaFX creates the Application itself (Application.launch), the launcher cannot give it
    // its components through a constructor. Set once by MainCommand before the launch, read in start()
    @Setter
    private static CompositionRoot compositionRoot;

    @Setter
    private static ReusableOptions applicationOptions;

    private ReusableOptions options;

    private GazePlay gazePlay;

    private GameMenuController gameMenuController;

    private GamesLocator gamesLocator;

    private Translator translator;

    @Override
    public void init() {
        final CompositionRoot root = compositionRoot;
        this.options = applicationOptions;
        log.info("options = {}", options);
        gazePlay = root.getGazePlay();
        gameMenuController = root.getGameMenuController();
        gamesLocator = root.getGamesLocator();
        translator = root.getTranslator();
    }

    @Override
    public void start(final Stage primaryStage) {

        boolean displayStageAtTheEnd = true;

        boolean showUserSelectPage = true;
        if (options != null) {
            final UserSelectionOptions userSelectionOptions = options.getUserSelectionOptions();
            if (userSelectionOptions != null) {
                if (userSelectionOptions.getUserid() != null) {
                    showUserSelectPage = false;

                    final User user = new User(userSelectionOptions.getUserid());

                    if (!UserProfileContext.findAllUsersProfiles().contains(user.getName())) {

                        ActiveConfigurationContext.switchToUser(user.getName());
                        final Configuration conf = ActiveConfigurationContext.getInstance();
                        final File userDirectory = GazePlayDirectories.getUserProfileDirectory(user.getName());
                        final boolean userDirectoryCreated = userDirectory.mkdirs();
                        log.debug("userDirectoryCreated = {}", userDirectoryCreated);

                        conf.setUserName(user.getName());
                        conf.setFileDir(GazePlayDirectories.getFileDirectoryUserValue(user.getName()).getAbsolutePath());

                    } else {
                        ActiveConfigurationContext.switchToUser(userSelectionOptions.getUserid());
                    }


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

        gazePlay.setPrimaryScene(primaryScene);
        gazePlay.setPrimaryStage(primaryStage);

        if (showUserSelectPage) {
            // The choice of the mode (games or evaluations), then the profile page
            gazePlay.goToModePage();
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
                            ReplayingGameFromJson replayingGameFromJson = new ReplayingGameFromJson(gazePlay, compositionRoot.getGameContextFactory(), games);
                            try {
                                replayingGameFromJson.pickJSONFile(replayJsonFileOptions.getJsonFileName());
                                replayingGameFromJson.drawLines();
                            } catch (IOException ex) {
                                log.error("Cannot replay the file {}", replayJsonFileOptions.getJsonFileName(), ex);
                            }
                        } else {
                            if (selectedVariantCode != null) {
                                IGameVariant variant = IGameVariant.toGameVariant(selectedVariantCode);
                                gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, variant);
                            } else if (hasNoVariant(selectedGameSpec)) {
                                gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, null);
                            } else {
                                GameVariantDialog dialog = new GameVariantDialog(
                                    gazePlay,
                                    gameMenuController,
                                    gazePlay.getPrimaryStage(),
                                    selectedGameSpec,
                                    new BorderPane(),
                                    selectedGameSpec.getGameVariantGenerator().getVariantChooseText());
                                dialog.setTitle(selectedGameNameCode);
                                dialog.show();
                                dialog.toFront();
                                dialog.setAlwaysOnTop(true);
                                displayStageAtTheEnd = false;
                            }
                        }
                    } else {
                        if (selectedVariantCode != null) {
                            IGameVariant variant = IGameVariant.toGameVariant(selectedVariantCode);
                            gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, variant);
                        } else if (hasNoVariant(selectedGameSpec)) {
                            gameMenuController.chooseAndStartNewGame(gazePlay, selectedGameSpec, null);
                        } else {
                            GameVariantDialog dialog = new GameVariantDialog(
                                gazePlay,
                                gameMenuController,
                                gazePlay.getPrimaryStage(),
                                selectedGameSpec,
                                new BorderPane(),
                                selectedGameSpec.getGameVariantGenerator().getVariantChooseText());
                            dialog.setTitle(selectedGameNameCode);
                            dialog.show();
                            dialog.toFront();
                            dialog.setAlwaysOnTop(true);
                            displayStageAtTheEnd = false;
                        }
                    }
                } else {
                    gazePlay.onReturnToMenu();
                }
            } else {
                gazePlay.onReturnToMenu();
            }
        }

        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), gazePlay.getPrimaryScene());

        if (displayStageAtTheEnd) {
            // Not in full screen: the window is already shown in full screen by the first page, and centerOnScreen
            // would move it up, in the screen without the taskbar (top cut, taskbar seen below; see
            // GazePlay.toggleFullScreen)
            if (!primaryStage.isFullScreen()) {
                primaryStage.centerOnScreen();
            }
            primaryStage.show();
        }
        log.info("Window: x = {}, y = {}, {} x {}, full screen = {}", primaryStage.getX(), primaryStage.getY(),
            primaryStage.getWidth(), primaryStage.getHeight(), primaryStage.isFullScreen());
    }

    /**
     * @return whether a game has no variant to choose: as in the menu ({@code GameMenuController.onGameSelection}), it
     * is then started without variant (e.g. GazePlay Eval with no imported evaluation), instead of an empty choice
     */
    static boolean hasNoVariant(final GameSpec gameSpec) {
        return gameSpec.getGameVariantGenerator() instanceof NoVariantGenerator
            || gameSpec.getGameVariantGenerator().getVariants().isEmpty();
    }

    /**
     * Creates the scene of the window, of the size of the window, with the stylesheets of the settings.
     */
    private Scene createPrimaryScene(final Stage primaryStage) {
        final Pane rootPane = new Pane();
        final Scene primaryScene = new Scene(rootPane, primaryStage.getWidth(), primaryStage.getHeight(), Color.BLACK);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), primaryScene);
        primaryStage.setScene(primaryScene);
        return primaryScene;
    }

    /**
     * Creates the scene of the window, of a given size (--width and --height options), with the stylesheets of the
     * settings.
     */
    private Scene createPrimaryScene(final Stage primaryStage, final double width, final double height) {
        final Pane rootPane = new Pane();
        final Scene primaryScene = new Scene(rootPane, width, height, Color.BLACK);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), primaryScene);
        primaryStage.setScene(primaryScene);
        return primaryScene;
    }

    /**
     * Sets the title and the icon of the window.
     */
    private void configurePrimaryStage(final Stage primaryStage) {
        primaryStage.setTitle("GazePlay Learning");
        // No "Press Esc to exit full screen" message in the middle of the screen when the full screen starts (seen
        // by the participant at the start of an evaluation); Esc still leaves the full screen
        primaryStage.setFullScreenExitHint("");
        primaryStage.setOnCloseRequest((WindowEvent we) -> primaryStage.close());

        final String iconImagePath = "data/common/images/gazeplayicon.png";
        final Image icon = new Image(iconImagePath);
        primaryStage.getIcons().add(icon);
    }

    /**
     * Makes the window fill the screen, in full screen mode.
     */
    private void autosize(final Stage primaryStage) {
        final Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        primaryStage.setWidth(screenDimension.getWidth());
        primaryStage.setHeight(screenDimension.getHeight());
        primaryStage.setMaximized(true);

        primaryStage.setFullScreen(true);
    }

}
