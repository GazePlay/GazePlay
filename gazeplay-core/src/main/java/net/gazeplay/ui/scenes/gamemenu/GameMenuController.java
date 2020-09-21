package net.gazeplay.ui.scenes.gamemenu;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.ui.scenes.ingame.GameContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

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
                chooseGame(gazePlay, gameSpec, onlyGameVariant);
            } else {
                chooseGame(gazePlay, gameSpec, null);
            }
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
