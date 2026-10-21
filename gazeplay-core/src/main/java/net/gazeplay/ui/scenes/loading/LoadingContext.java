package net.gazeplay.ui.scenes.loading;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NLabel;

public class LoadingContext extends BorderPane {

    private LogoFactory logoFactory;

    public LoadingContext(GazePlay gazePlay) {

        gazePlayLoadingContext(gazePlay);
    }

    public void gazePlayLoadingContext(GazePlay gazePlay){
        //  ImageView backgroundImage = new ImageView(new Image("data/common/images/bravo.gif"));
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

    public void stopAnimation(){
        logoFactory.stopAnimation();
    }
}
