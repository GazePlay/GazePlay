package net.gazeplay.ui.scenes.loading;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NLabel;

/**
 * Screen shown while a game starts in its own JVM: the animated logo and "Loading...".
 */
public class LoadingContext extends BorderPane {

    private LogoFactory logoFactory;

    public LoadingContext(GazePlay gazePlay) {

        gazePlayLoadingContext(gazePlay);
    }

    /**
     * Builds the screen, for the light or dark theme.
     */
    public void gazePlayLoadingContext(GazePlay gazePlay){
        I18NLabel loadingLabel = new I18NLabel(gazePlay.getTranslator(), "Loading...");
        Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isDarkThemeEnabled()) {
            loadingLabel.setStyle("\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-font-weight: bold;\n" +
                "    -fx-font-size: 25pt;\n" +
                "    -fx-font-smoothing-type: lcd;");
        }
        else {
            loadingLabel.setStyle("\n" + "    -fx-text-fill: Black;\n" +
                    "    -fx-font-weight: bold;\n" +
                    "    -fx-font-size: 25pt;\n" +
                    "    -fx-font-smoothing-type: lcd;"+
                    "-fx-background-color: #fffaf0; " );
        }


        logoFactory = LogoFactory.getInstance();
        VBox stackPane = new VBox(logoFactory.createLogoAnimated(gazePlay.getPrimaryStage()), loadingLabel);
        loadingLabel.setAlignment(Pos.CENTER);
        stackPane.setAlignment(Pos.CENTER);

        if (config.isDarkThemeEnabled()) {
            this.setStyle("-fx-background-color: black");
        }
        else {
            this.setStyle("-fx-background-color: #fffaf0");
        }

        this.setCenter(stackPane);
    }

    /**
     * Stops the animation of the logo.
     */
    public void stopAnimation(){
        logoFactory.stopAnimation();
    }
}
