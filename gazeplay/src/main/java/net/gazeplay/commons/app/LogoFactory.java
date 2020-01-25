package net.gazeplay.commons.app;

import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogoFactory {

    @Getter
    private static final LogoFactory instance = new LogoFactory();

    final static String staticLogoImagePath = "data/common/images/logos/gazeplay-top-logo.png";

    static class LogoCreationException extends RuntimeException {
        LogoCreationException(Throwable cause) {
            super(cause);
        }
    }

    public Node createLogoAnimated(Stage stage) {
        final double preferredHeight = stage.getHeight() * 0.1d;
        log.info("preferredHeight = {}", preferredHeight);

        GazePlayAnimatedLogo gazePlayAnimatedLogo = GazePlayAnimatedLogo.newInstance((int) preferredHeight);

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                throw new LogoCreationException(e);
            }
            SequentialTransition animation = gazePlayAnimatedLogo.createAnimation();
            animation.setCycleCount(-1);
            animation.play();
        });
        t.start();
        return gazePlayAnimatedLogo.getLetters();
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
