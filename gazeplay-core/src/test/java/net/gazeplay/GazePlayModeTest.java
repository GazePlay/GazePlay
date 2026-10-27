package net.gazeplay;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import net.gazeplay.ui.scenes.evalmenu.EvalMenuScreen;
import net.gazeplay.ui.scenes.gamemenu.HomeMenuScreen;
import net.gazeplay.ui.scenes.modeselect.ModeSelectionContext;
import net.gazeplay.ui.scenes.userselect.UserProfileContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * The navigation between the modes: the mode page, the profile page, then the menu of the chosen mode, built again
 * when the mode changes.
 */
@ExtendWith(ApplicationExtension.class)
class GazePlayModeTest {

    private final GazePlay.Screens screens = mock(GazePlay.Screens.class);

    private final BackgroundMusicManager originalMusicManager = BackgroundMusicManager.getInstance();

    private GazePlay gazePlay;

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 400, 300));
    }

    @BeforeEach
    void setUp() {
        BackgroundMusicManager.setInstance(mock(BackgroundMusicManager.class));
        when(screens.newModeSelectionContext()).thenReturn(mock(ModeSelectionContext.class));
        when(screens.newUserProfileContext()).thenReturn(mock(UserProfileContext.class));
        when(screens.newConfigurationContext()).thenReturn(mock(ConfigurationContext.class));
        when(screens.newHomeMenuScreen()).thenReturn(mock(HomeMenuScreen.class));
        when(screens.newEvalMenuScreen()).thenReturn(mock(EvalMenuScreen.class));
        gazePlay = new GazePlay(mock(Translator.class), mock(GamesLocator.class), screens);
        gazePlay.setPrimaryStage(stage);
        gazePlay.setPrimaryScene(stage.getScene());
        gazePlay.setCurrentScreenDimensionSupplier(() -> new Dimension2D(400, 300));
    }

    @AfterEach
    void restoreGlobalState() {
        BackgroundMusicManager.setInstance(originalMusicManager);
    }

    @Test
    void shouldStartInTheGamesMode() {
        assertEquals(AppMode.GAMES, gazePlay.getMode());
    }

    @Test
    void shouldShowTheMenuOfTheEvaluationsInTheEvaluationsMode() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            gazePlay.goToModePage();
            gazePlay.chooseMode(AppMode.EVALUATIONS);
            gazePlay.onReturnToMenu();
        }).get();

        assertEquals(AppMode.EVALUATIONS, gazePlay.getMode());
        verify(screens).newModeSelectionContext();
        verify(screens).newUserProfileContext();
        verify(screens).newEvalMenuScreen();
        verify(screens, never()).newHomeMenuScreen();
    }

    @Test
    void shouldBuildTheMenuAgainWhenTheModeChanges() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            gazePlay.chooseMode(AppMode.GAMES);
            gazePlay.onReturnToMenu();
            gazePlay.onReturnToMenu();
            gazePlay.goToModePage();
            gazePlay.chooseMode(AppMode.EVALUATIONS);
            gazePlay.onReturnToMenu();
        }).get();

        // The menu of the games is kept while the mode stays the same
        verify(screens, times(1)).newHomeMenuScreen();
        verify(screens, times(1)).newEvalMenuScreen();
        // The settings page is built again for the new mode
        verify(screens, times(2)).newConfigurationContext();
    }
}
