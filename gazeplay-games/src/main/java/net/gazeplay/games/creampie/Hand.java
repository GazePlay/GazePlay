package net.gazeplay.games.creampie;

import javafx.animation.*;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 17/08/2016.
 */

@Slf4j
public class Hand extends Parent {

    private static final int size = 40;
    private static final int maxSize = 100;

    private static final String IMAGES_BASE_PATH = "data/creampie/images";

    private static final String PIE_IMAGE_PATH = IMAGES_BASE_PATH + "/gateau.png";

    private static final String HAND_IMAGE_PATH = IMAGES_BASE_PATH + "/hand.png";

    private final ImageView hand;

    private final ImageView pie;

    public Hand() {
        recomputePosition();

        hand = new ImageView(new Image(HAND_IMAGE_PATH));
        hand.setFitWidth(maxSize);
        hand.setFitHeight(maxSize);
        hand.setPreserveRatio(true);

        pie = new ImageView(new Image(PIE_IMAGE_PATH));
        pie.setFitWidth(size);
        pie.setFitHeight(size);
        pie.setPreserveRatio(true);

        this.getChildren().add(pie);
        this.getChildren().add(hand);
    }

    public void recomputePosition() {
        final Pane parent = (Pane) this.getParent();
        if (parent != null) {
            final double handTranslateX = (parent.getWidth() - maxSize) / 2;
            final double handTranslateY = parent.getHeight() - maxSize;
            final double pieTranslateX = (parent.getWidth() - size) / 2;
            final double pieTranslateY = parent.getHeight() - maxSize;

            hand.setTranslateX(handTranslateX);
            hand.setTranslateY(handTranslateY);

            pie.setTranslateX(pieTranslateX);
            pie.setTranslateY(pieTranslateY);
        }
    }

    public void onTargetHit(final Target target) {

        final Animation animation = createAnimation(target);
        animation.play();
    }

    private Animation createAnimation(final Target target) {
        final Timeline timeline = new Timeline();
        final Timeline timeline2 = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(200), new KeyValue(hand.fitHeightProperty(), size)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(200), new KeyValue(hand.fitWidthProperty(), size)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),
            new KeyValue(pie.translateXProperty(), target.getCenterX() - maxSize)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),
            new KeyValue(pie.translateYProperty(), target.getCenterY() - maxSize)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(pie.fitHeightProperty(), maxSize * 2)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(pie.fitWidthProperty(), maxSize * 2)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(pie.rotateProperty(), pie.getRotate() + 360)));

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(hand.fitHeightProperty(), maxSize)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(hand.fitWidthProperty(), maxSize)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(pie.fitHeightProperty(), size)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(pie.fitWidthProperty(), size)));
        timeline2.getKeyFrames()
            .add(new KeyFrame(new Duration(1), new KeyValue(hand.translateXProperty(), hand.getTranslateX())));
        timeline2.getKeyFrames()
            .add(new KeyFrame(new Duration(1), new KeyValue(hand.translateYProperty(), hand.getTranslateY())));
        timeline2.getKeyFrames()
            .add(new KeyFrame(new Duration(1), new KeyValue(pie.translateXProperty(), pie.getTranslateX())));
        timeline2.getKeyFrames()
            .add(new KeyFrame(new Duration(1), new KeyValue(pie.translateYProperty(), pie.getTranslateY())));

        return new SequentialTransition(timeline, timeline2);
    }
}
