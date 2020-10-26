package net.gazeplay.ui.scenes.loading;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.app.LogoFactory;
import net.gazeplay.commons.ui.I18NLabel;

public class LoadingContext extends BorderPane {

    public LoadingContext(GazePlay gazePlay) {
        //  ImageView backgroundImage = new ImageView(new Image("data/common/images/bravo.gif"));
        I18NLabel loadingLabel = new I18NLabel(gazePlay.getTranslator(), "Loading...");
        loadingLabel.setStyle("\n" +
            "    -fx-text-fill: white;\n" +
            "    -fx-font-weight: bold;\n" +
            "    -fx-font-size: 25pt;\n" +
            "    -fx-font-smoothing-type: lcd;");
        VBox stackPane = new VBox(LogoFactory.getInstance().createLogoAnimated(gazePlay.getPrimaryStage()), loadingLabel);
        loadingLabel.setAlignment(Pos.CENTER);
        stackPane.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: black");
        this.setCenter(stackPane);
    }

}
