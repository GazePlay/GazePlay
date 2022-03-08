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
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.GazePlayArgs;

public class LoadingContext extends BorderPane {

    private LogoFactory logoFactory;

    public LoadingContext(GazePlay gazePlay) {

        String gazeplayType = GazePlayArgs.returnArgs();

        if (gazeplayType.equals("afsrGazeplay")){
            afsrGazePlayLoadindContext(gazePlay);
        }else {
            gazePlayLoadingContext(gazePlay);
        }
    }

    public void gazePlayLoadingContext(GazePlay gazePlay){
        //  ImageView backgroundImage = new ImageView(new Image("data/common/images/bravo.gif"));
        I18NLabel loadingLabel = new I18NLabel(gazePlay.getTranslator(), "Loading...");
        loadingLabel.setStyle("\n" +
            "    -fx-text-fill: white;\n" +
            "    -fx-font-weight: bold;\n" +
            "    -fx-font-size: 25pt;\n" +
            "    -fx-font-smoothing-type: lcd;");
        logoFactory = LogoFactory.getInstance();
        VBox stackPane = new VBox(logoFactory.createLogoAnimated(gazePlay.getPrimaryStage()), loadingLabel);
        loadingLabel.setAlignment(Pos.CENTER);
        stackPane.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: black");
        this.setCenter(stackPane);
    }

    public void afsrGazePlayLoadindContext(GazePlay gazePlay){
        //  ImageView backgroundImage = new ImageView(new Image("data/common/images/bravo.gif"));
        I18NLabel loadingLabel = new I18NLabel(gazePlay.getTranslator(), "Loading...");
        loadingLabel.setStyle("\n" +
            "    -fx-text-fill: white;\n" +
            "    -fx-font-weight: bold;\n" +
            "    -fx-font-size: 25pt;\n" +
            "    -fx-font-smoothing-type: lcd;");
        VBox logoVBox = new VBox();
        logoVBox.setSpacing(10);

        logoFactory = LogoFactory.getInstance();
        Node animatedLogo = logoFactory.createLogoAnimated(gazePlay.getPrimaryStage());

        HBox logoHBox = new HBox();
        logoHBox.setAlignment(Pos.CENTER);
        logoHBox.getChildren().add(animatedLogo);
        logoHBox.setSpacing(20);

        ImageView iv = new ImageView(new Image("data/common/images/logos/Logo-AFSR.png"));
        iv.fitHeightProperty().bind(logoHBox.heightProperty().multiply(0.7));
        iv.setPreserveRatio(true);

        logoHBox.getChildren().add(iv);

        logoVBox.getChildren().addAll(logoHBox,loadingLabel);
        loadingLabel.setAlignment(Pos.CENTER);
        logoVBox.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: black");
        this.setCenter(logoVBox);
    }

    public void stopAnimation(){
        logoFactory.stopAnimation();
    }
}
