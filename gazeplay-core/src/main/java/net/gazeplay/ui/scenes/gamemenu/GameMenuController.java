package net.gazeplay.ui.scenes.gamemenu;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.*;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.ingame.GameContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.lang.System.getProperty;

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
                chooseGame1(gazePlay, gameSpec, onlyGameVariant);
            } else {
                chooseGame1(gazePlay, gameSpec, null);
            }
        }
    }

    public void chooseGame1(
        GazePlay gazePlay,
        GameSpec selectedGameSpec,
        IGameVariant gameVariant
    ) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        ProcessBuilder builder = new ProcessBuilder(javaBin,"-cp",classpath,GazePlayLauncher.class.getName(), "--user", "Seb", "--game", "Scribble");
        try {
            Process process = builder.inheritIO().start();
            process.waitFor(10, TimeUnit.SECONDS);
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void chooseGame(
        GazePlay gazePlay,
        GameSpec selectedGameSpec,
        IGameVariant gameVariant
    ) {
        GameContext gameContext = applicationContext.getBean(GameContext.class);

        gazePlay.onGameLaunch(gameContext);

        IGameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        final Scene scene = gazePlay.getPrimaryScene();
        final Stats stats = gameLauncher.createNewStats(scene);

        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, gameVariant, stats);

        gameContext.createControlPanel(gazePlay, stats, currentGame);

        gameContext.createQuitShortcut(gazePlay, stats, currentGame);

        if (selectedGameSpec.getGameSummary().getBackgroundMusicUrl() != null) {
            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            playBackgroundMusic(gameContext, selectedGameSpec, musicManager);
        }

        stats.start();
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
