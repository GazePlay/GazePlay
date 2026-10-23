package net.gazeplay.commons.app;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogoFactory {

    @Getter
    private static final LogoFactory instance = new LogoFactory();

    final static String staticLogoImagePath = "data/common/images/logos/gazeplay-top-logo.png";

    private PauseTransition logoAnimationDelay;
    private SequentialTransition logoAnimatedAnimationTransition;

    public Node createLogoAnimated(Stage stage) {
        final double preferredHeight = stage.getHeight() * 0.1d;
        log.info("preferredHeight = {}", preferredHeight);

        GazePlayAnimatedLogo gazePlayAnimatedLogo = GazePlayAnimatedLogo.newInstance((int) preferredHeight);

        // The animation starts on the JavaFX thread: it used to be created and played from a thread of its own
        logoAnimationDelay = new PauseTransition(Duration.seconds(2));
        logoAnimationDelay.setOnFinished(event -> {
            logoAnimatedAnimationTransition = gazePlayAnimatedLogo.createAnimation();
            logoAnimatedAnimationTransition.setCycleCount(Animation.INDEFINITE);
            logoAnimatedAnimationTransition.play();
        });
        logoAnimationDelay.play();
        return gazePlayAnimatedLogo.getLetters();
    }

    public void stopAnimation(){
        if (logoAnimationDelay != null) {
            logoAnimationDelay.stop();
        }
        if (logoAnimatedAnimationTransition != null) {
            logoAnimatedAnimationTransition.stop();
        }
    }

    public Node createLogoStatic(Stage stage) {
        final Image logoImage = new Image(staticLogoImagePath);
        final ImageView logoView = new ImageView(logoImage);
        logoView.setPreserveRatio(true);
        fitStaticLogo(logoView, stage);

        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            fitStaticLogo(logoView, stage);
        });

        return logoView;
    }

    private static void fitStaticLogo(ImageView logoView, Stage stage) {
        final double preferredWidth = stage.getWidth() * 0.5d;
        final double preferredHeight = stage.getHeight() * 0.1d;
        logoView.setFitHeight(preferredHeight);
        logoView.setFitWidth(preferredWidth);

    }

}
