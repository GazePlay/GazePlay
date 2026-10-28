package net.gazeplay;

import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.ConfigurationSource;
import net.gazeplay.ui.scenes.gamemenu.GameButtonOrientation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The orientation of the cards of the menu, chosen in the settings (maintainer's report of 28/10/2026: no difference
 * seen): the menu shows it when it comes back, without restarting, and again after a restart. The real application
 * (CompositionRoot) on the default profile of the test folder.
 */
@ExtendWith(ApplicationExtension.class)
class MenuOrientationTest {

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        // Smaller than the screen of Monocle (1280 x 800)
        stage.setScene(new Scene(new Pane(), 1200, 700));
        stage.show();
    }

    @AfterEach
    void restoreTheOrientation() {
        ActiveConfigurationContext.switchToDefaultUser();
        ActiveConfigurationContext.getInstance().setMenuButtonsOrientation("HORIZONTAL");
    }

    @Test
    void shouldShowTheOrientationChosenInTheSettings() throws Exception {
        GazePlay gazePlay = WaitForAsyncUtils.asyncFx(() -> {
            ActiveConfigurationContext.switchToDefaultUser();
            ActiveConfigurationContext.getInstance().setMenuButtonsOrientation("HORIZONTAL");
            GazePlay application = start();
            application.onDisplayConfigurationManagement();
            return application;
        }).get();
        // The content of a ScrollPane joins the scene at its first layout
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.asyncFx(() -> ((ToggleButton) List.copyOf(stage.getScene().getRoot()
            .lookupAll(".gp-settings-nav-item")).get(3)).fire()).get();
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.asyncFx(() -> {
            @SuppressWarnings("unchecked")
            ChoiceBox<GameButtonOrientation> chooser = (ChoiceBox<GameButtonOrientation>) stage.getScene().getRoot()
                .lookupAll(".choice-box").stream()
                .filter(node -> ((ChoiceBox<?>) node).getItems().contains(GameButtonOrientation.VERTICAL))
                .findFirst().orElseThrow();
            chooser.setValue(GameButtonOrientation.VERTICAL);
            gazePlay.onReturnToMenu();
        }).get();
        WaitForAsyncUtils.waitForFxEvents();

        // Back in the menu, without restarting
        assertEquals("vertical", shownOrientation());
        assertEquals("VERTICAL", ConfigurationSource.createFromDefaultProfile().getMenuButtonsOrientation());

        // After a restart
        WaitForAsyncUtils.asyncFx(() -> {
            ActiveConfigurationContext.switchToDefaultUser();
            start();
        }).get();
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("vertical", shownOrientation());
    }

    /**
     * @return the application started as by a user: the games, the default profile, the menu
     */
    private GazePlay start() {
        GazePlay application = new CompositionRoot().getGazePlay();
        application.setPrimaryStage(stage);
        application.setPrimaryScene(stage.getScene());
        application.goToModePage();
        application.chooseMode(AppMode.GAMES);
        application.onReturnToMenu();
        return application;
    }

    /**
     * @return the orientation of the cards shown in the menu: "vertical", "horizontal", or both if mixed
     */
    private String shownOrientation() throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            int vertical = stage.getScene().getRoot().lookupAll(".gp-game-card-vertical").size();
            int horizontal = stage.getScene().getRoot().lookupAll(".gp-game-card-horizontal").size();
            return vertical > 0 && horizontal == 0 ? "vertical"
                : horizontal > 0 && vertical == 0 ? "horizontal" : vertical + " vertical, " + horizontal + " horizontal";
        }).get();
    }
}
