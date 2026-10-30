package net.gazeplay.ui.scenes.gamemenu;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.*;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.loading.LoadingContext;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Starts the games chosen in the menu. Each game runs in its own JVM (decision of the maintainer: isolation and fresh
 * state for each game): the menu shows a loading screen, starts the JVM, and comes back after 6 seconds.
 */
@Slf4j
public class GameMenuController {

    @Getter
    private final Supplier<GameContext> gameContextFactory;

    public GameMenuController(final Supplier<GameContext> gameContextFactory) {
        this.gameContextFactory = gameContextFactory;
    }

    /**
     * Starts a game chosen in the menu: shows its variants when it has several, otherwise starts it directly.
     */
    public void onGameSelection(
        @NonNull GazePlay gazePlay,
        @NonNull Parent root,
        @NonNull GameSpec gameSpec,
        String gameName
    ) {
        Collection<IGameVariant> variants = gameSpec.getGameVariantGenerator().getVariants();
        if (variants.size() > 1) {
            root.setEffect(new BoxBlur());
            root.setDisable(true);
            GameVariantDialog dialog = new GameVariantDialog(gazePlay, this, gazePlay.getPrimaryStage(), gameSpec, root, gameSpec.getGameVariantGenerator().getVariantChooseText());
            dialog.setTitle(gameName);
            dialog.show();
            dialog.toFront();
            dialog.setAlwaysOnTop(true);
        } else if (variants.size() == 1) {
            IGameVariant onlyGameVariant = variants.iterator().next();
            chooseAndStartNewGameProcess(gazePlay, gameSpec, onlyGameVariant);
        } else {
            chooseAndStartNewGameProcess(gazePlay, gameSpec, null);
        }
    }

    /**
     * Starts a game, and its variant, in a new JVM of the size of the window (full screen if the window is).
     */
    public void chooseAndStartNewGameProcess(
        GazePlay gazePlay,
        GameSpec selectedGameSpec,
        IGameVariant gameVariant
    ) {
        gazePlay.getPrimaryScene().setCursor(Cursor.WAIT);
        gazePlay.getPrimaryScene().setRoot(new LoadingContext(gazePlay));
        ProcessBuilder builder;

        int height = 0;
        int width = 0;
        if (!gazePlay.isFullScreen()) {
            height = (int) gazePlay.getPrimaryScene().getWindow().getHeight();
            width = (int) gazePlay.getPrimaryScene().getWindow().getWidth();
        }
        builder = createBuilder(selectedGameSpec.getGameSummary().getNameCode(), gameVariant, height, width);
        final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
        musicManager.stop();
        runProcessDisplayLoadAndWaitForNewJVMDisplayed(gazePlay, builder);
    }



    /**
     * Starts a JVM, its output in the output of this one, and shows the menu again after 6 seconds (the time for the
     * game to appear over it).
     */
    public static void runProcessDisplayLoadAndWaitForNewJVMDisplayed(GazePlay gazePlay, ProcessBuilder builder) {

        Thread displayLoadingContextThread = new Thread(() -> {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for the game to start", e);
            }

            // The menu is rebuilt on the JavaFX thread: from this thread, its CSS raced with the pulse
            Platform.runLater(() -> {
                ((LoadingContext) gazePlay.getPrimaryScene().getRoot()).stopAnimation();
                gazePlay.getPrimaryScene().setCursor(Cursor.DEFAULT);
                gazePlay.onReturnToMenu();
            });
        });

        displayLoadingContextThread.start();

        try {
            builder.inheritIO().start();
        } catch (Exception e) {
            log.error("Cannot start the game process", e);
        }
    }

    /**
     * @return the command starting a game in a new JVM: the same Java and classpath, the JVM options of
     *   {@link GazePlayLauncher#jvmOptions()}, the GazePlay folder, the user, the game, its variant and the size of the
     *   window (none for the full screen)
     */
    public ProcessBuilder createBuilder(String game, IGameVariant gameVariant, int height, int width) {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        LinkedList<String> commands = new LinkedList<>(Arrays.asList(javaBin, "-Djdk.gtk.version=2"));
        commands.addAll(GazePlayLauncher.jvmOptions());
        // The same GazePlay folder as the menu: the game does not look for it, nor move it, again
        commands.add("-D" + GazePlayDirectories.GAZEPLAY_FOLDER_PROPERTY + "="
            + GazePlayDirectories.getGazePlayFolder().getAbsolutePath());
        commands.addAll(Arrays.asList("-cp", classpath, GazePlayLauncher.class.getName()));

        String user = ActiveConfigurationContext.getInstance().getUserName();
        if (user != null && !user.equals("")) {
            commands.addAll(Arrays.asList("--user", user));
        } else {
            commands.add("--default-user");
        }

        commands.addAll(Arrays.asList("--game", game));

        if (gameVariant != null) {
            commands.addAll(Arrays.asList("--variant", gameVariant.toString()));
        }


        if (height != 0 && width != 0) {
            commands.addAll(Arrays.asList("--height", "" + height, "--width", "" + width));
        }

        return new ProcessBuilder(commands);
    }

    /**
     * Starts a game in this JVM (used by the JVM of the game itself, started with --game): shows the game screen,
     * creates the game and its statistics, and starts it.
     */
    public void chooseAndStartNewGame(
        GazePlay gazePlay,
        GameSpec selectedGameSpec,
        IGameVariant gameVariant
    ) {
        GameContext gameContext = gameContextFactory.get();
        gazePlay.onGameLaunch(gameContext);

        IGameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        gazePlay.getTranslator().notifyLanguageChanged();

        final Scene scene = gazePlay.getPrimaryScene();
        final Stats stats = gameLauncher.createNewStats(scene);
        // The variant and the Stats come from the same GameSpec as the launcher: their types match at run time
        @SuppressWarnings("unchecked")
        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, gameVariant, stats);

        gameContext.createControlPanel(gazePlay, stats, currentGame, false);

        if (selectedGameSpec.getGameSummary().getBackgroundMusicUrl() != null) {
            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            playBackgroundMusic(gameContext, selectedGameSpec, musicManager);
        }

        stats.start();

        String gameVariantLabel;
        if (gameVariant != null) {
            gameVariantLabel = gameVariant.toString();
        } else {
            gameVariantLabel = null;
        }


        String gameNameCode = selectedGameSpec.getGameSummary().getNameCode();
        stats.setGameVariant(gameVariantLabel, gameNameCode);

        currentGame.launch();
        log.info("Game launched : {}, variant {}", gameNameCode, gameVariantLabel);
    }

    /**
     * Plays the music of the game, if it has one, instead of the background music; otherwise the background music goes
     * on.
     */
    void playBackgroundMusic(GameContext gameContext, GameSpec selectedGameSpec, BackgroundMusicManager musicManager) {
        MediaPlayer currentMusic = musicManager.getCurrentMusic();
        boolean defaultMusicPlaying = true;
        if (currentMusic != null) {
            Media currentMedia = currentMusic.getMedia();
            if (currentMedia != null) {
                defaultMusicPlaying = currentMedia.getSource().contains(Configuration.DEFAULT_VALUE_BACKGROUND_MUSIC);
            }
        }
        log.info("is default music set : {}", defaultMusicPlaying);
        if (defaultMusicPlaying || musicManager.getPlaylist().isEmpty()) {
            musicManager.backupPlaylist();
            musicManager.emptyPlaylist();
            musicManager.playMusicAlone(selectedGameSpec.getGameSummary().getBackgroundMusicUrl());
            gameContext.getMusicControl().updateMusicController();
        }
    }

}
