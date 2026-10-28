package net.gazeplay.commons.app;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates the logo shown at the top of the main screens: the animated letters, or the static image of GazePlay
 * Learning.
 */
@Slf4j
public class LogoFactory {

    @Getter
    private static final LogoFactory instance = new LogoFactory();

    /**
     * GAZE PLAY and its mascot, with "Learning" below (October 2026). The word is written with the Fredoka font
     * (SIL Open Font License 1.1); the images are built by gradle/logo/make_logo.py.
     */
    final static String staticLogoImagePath = "data/common/images/logos/gazeplay-learning-top-logo.png";

    final static String learningWordImagePath = "data/common/images/GazePlayLetters/learning.png";

    private PauseTransition logoAnimationDelay;
    private SequentialTransition logoAnimatedAnimationTransition;

    /**
     * @return the animated logo, a tenth of the height of the window, whose animation starts after a short delay
     */
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

        // "Learning" below the animated GAZE PLAY letters
        final ImageView learningWord = new ImageView(new Image(learningWordImagePath));
        learningWord.setPreserveRatio(true);
        learningWord.setFitHeight(preferredHeight * 0.45d);
        final VBox logo = new VBox(gazePlayAnimatedLogo.getLetters(), learningWord);
        logo.setAlignment(Pos.CENTER);
        // Room above the letters, which bump upwards in the animation
        logo.setPadding(new Insets(preferredHeight * 0.4d, 0, 0, 0));
        logo.setStyle(gazePlayAnimatedLogo.getLetters().getStyle());
        return logo;
    }

    /**
     * Stops the animation of the animated logo, if any.
     */
    public void stopAnimation(){
        if (logoAnimationDelay != null) {
            logoAnimationDelay.stop();
        }
        if (logoAnimatedAnimationTransition != null) {
            logoAnimatedAnimationTransition.stop();
        }
    }

    /**
     * @return the static logo, which follows the size of the window
     */
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

    /**
     * @return the static logo at a fixed height, e.g. in the top bar of a menu
     */
    public Node createLogoStatic(double height) {
        final ImageView logoView = new ImageView(new Image(staticLogoImagePath));
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        logoView.setFitHeight(height);
        return logoView;
    }

    /**
     * Fits the logo in half of the width and a tenth of the height of the window.
     */
    private static void fitStaticLogo(ImageView logoView, Stage stage) {
        final double preferredWidth = stage.getWidth() * 0.5d;
        final double preferredHeight = stage.getHeight() * 0.1d;
        logoView.setFitHeight(preferredHeight);
        logoView.setFitWidth(preferredWidth);

    }

}
