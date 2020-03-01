package net.gazeplay.latestnews;

import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;
import net.gazeplay.IntegrationTestBase;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.DefaultTranslator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LatestNewsPopupIntegrationTest extends IntegrationTestBase {

    @BeforeEach
    void setUpClass() throws Exception {
        launch(TestApp.class);
    }

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    @Test
    void shouldBeNamedGazePlayNews() {
        assertEquals("GazePlay News", ((Stage) listWindows().get(0)).getTitle());
    }

    @Test
    void shouldCloseOnContinue() {
        CustomButton continueButton = (CustomButton) lookup("#continue").queryAll().iterator().next();
        clickOn(continueButton);
        assertEquals(0, listWindows().size());
    }

    public static class TestApp extends Application {

        @Override
        public void start(Stage primaryStage) {
            Configuration configuration = ActiveConfigurationContext.getInstance();
            Translator translator = new DefaultTranslator(configuration, Multilinguism.getForResource("data/multilinguism/multilinguism.csv"));

            LatestNewsPopup popup = new LatestNewsPopup(
                configuration,
                translator,
                () -> new Dimension2D(1920, 1080)
            );

            popup.show();
            primaryStage.close(); // Ensures only one window is open.
        }
    }
}
