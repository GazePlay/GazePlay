package net.gazeplay.ui.scenes.gamemenu;

import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.*;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.ingame.GameContext;
import net.gazeplay.ui.scenes.loading.LoadingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GameMenuController {

    @Autowired
    private ApplicationContext applicationContext;

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

        } else {
            if (variants.size() == 1) {
                IGameVariant onlyGameVariant = variants.iterator().next();
                chooseAndStartNewGameProcess(gazePlay, gameSpec, onlyGameVariant);
            } else {
                chooseAndStartNewGameProcess(gazePlay, gameSpec, null);
            }
        }
    }

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

    public static void runProcessDisplayLoadAndWaitForNewJVMDisplayed(GazePlay gazePlay, ProcessBuilder builder) {

        Thread displayLoadingContextThread = new Thread(() -> {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ((LoadingContext) gazePlay.getPrimaryScene().getRoot()).stopAnimation();
            gazePlay.getPrimaryScene().setCursor(Cursor.DEFAULT);
            gazePlay.onReturnToMenu();
        });

        displayLoadingContextThread.start();

        try {
            builder.inheritIO().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProcessBuilder createBuilder(String game, IGameVariant gameVariant, int height, int width) {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        LinkedList<String> commands = new LinkedList<>(Arrays.asList(javaBin, "-Djdk.gtk.version=2", "-cp", classpath, GazePlayLauncher.class.getName()));

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

    public void chooseAndStartNewGame(
        GazePlay gazePlay,
        GameSpec selectedGameSpec,
        IGameVariant gameVariant
    ) {
        GameContext gameContext = applicationContext.getBean(GameContext.class);
        gazePlay.onGameLaunch(gameContext);

        IGameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        gazePlay.getTranslator().notifyLanguageChanged();

        final Scene scene = gazePlay.getPrimaryScene();
        final Stats stats = gameLauncher.createNewStats(scene);
        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, gameVariant, stats);

        gameContext.createControlPanel(gazePlay, stats, currentGame, false);
        gameContext.createQuitShortcut(gazePlay, stats, currentGame);

        if (selectedGameSpec.getGameSummary().getBackgroundMusicUrl() != null) {
            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            playBackgroundMusic(gameContext, selectedGameSpec, musicManager);
        }

        stats.start();

        String gameVariantLabel = (gameVariant != null) ? gameVariant.toString() : null;
        String gameNameCode = selectedGameSpec.getGameSummary().getNameCode();
        stats.setGameVariant(gameVariantLabel, gameNameCode);

        currentGame.launch();
    }

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
