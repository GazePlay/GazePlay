package net.gazeplay.ui.scenes.gamemenu;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
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
        Collection<GameSpec.GameVariant> variants = gameSpec.getGameVariantGenerator().getVariants();

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
                GameSpec.GameVariant onlyGameVariant = variants.iterator().next();
                chooseGame(gazePlay, gameSpec, onlyGameVariant);
            } else {
                chooseGame(gazePlay, gameSpec, null);
            }
        }
    }

    public void chooseGame(
        GazePlay gazePlay,
        GameSpec selectedGameSpec,
        GameSpec.GameVariant gameVariant
    ) {
        GameContext gameContext = applicationContext.getBean(GameContext.class);

        gazePlay.onGameLaunch(gameContext);

        GameSpec.GameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        final Scene scene = gazePlay.getPrimaryScene();
        final Stats stats = gameLauncher.createNewStats(scene);

        // if (config.isHeatMapDisabled()) {
        // log.info("HeatMap is disabled, skipping instantiation of the HeatMap Data model");
        // } else {
        // // gameContext.getGazeDeviceManager().addGazeMotionListener(stats);
        // }

        // gameContext.getGazeDeviceManager().addGazeMotionListener(secondScreen);

        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, gameVariant, stats);

        gameContext.createControlPanel(gazePlay, stats, currentGame);

        gameContext.createQuitShortcut(gazePlay, stats, currentGame);

        if (selectedGameSpec.getGameSummary().getBackgroundMusicUrl() != null) {

            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            log.info("is default music set : {}", musicManager.getIsCustomMusicSet().getValue());
            if (!musicManager.getIsCustomMusicSet().getValue() || musicManager.getPlaylist().isEmpty()) {
                musicManager.emptyPlaylist();
                musicManager.playMusicAlone(selectedGameSpec.getGameSummary().getBackgroundMusicUrl());
                gameContext.getMusicControl().updateMusicControler();
            }
        }

        stats.start();
        currentGame.launch();
    }

}
