package net.gazeplay;

import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogoFactory {

    @Getter
    private static final LogoFactory instance = new LogoFactory();

    final static String staticLogoImagePath = "data/common/images/logos/gazeplay1.6.1.png";

    // public final static String LOGO_PATH = "data/common/images/logos/gazeplayClassicLogo.png";

    public Node createLogo(Pane root) {
        return createLogoAnimated(root);
        //return createLogoStatic(root);
    }

    public Node createLogoAnimated(Pane root) {
        GazePlayAnimatedLogo gazePlayAnimatedLogo = GazePlayAnimatedLogo.newInstance();

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            SequentialTransition animation = gazePlayAnimatedLogo.createAnimation();
            animation.setCycleCount(-1);
            //animation.setCycleCount(3);
            animation.play();
        });
        t.start();
        return gazePlayAnimatedLogo.getRoot();
    }

    public Node createLogoStatic(Pane root) {
        double width = root.getWidth() * 0.5;
        double height = root.getHeight() * 0.2;

        final Image logoImage = new Image(staticLogoImagePath, width, height, true, true);
        final ImageView logoView = new ImageView(logoImage);
        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            final double newHeight = newValue.doubleValue() * 0.2;
            final Image newLogoImage = new Image(staticLogoImagePath, width, newHeight, true, true);
            logoView.setImage(newLogoImage);
        });

        return logoView;
    }

}
