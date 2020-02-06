package melordi;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Created by schwab on 10/08/2016.
 */
public class Metronome extends Parent {

    public Metronome() {
        // création du fond du métronome
        final ImageView fond_metronome = new ImageView(
            new Image(Metronome.class.getResourceAsStream("images/metronome.png")));
        fond_metronome.setFitHeight(40);
        fond_metronome.setPreserveRatio(true);

        // création de l'aiguille du métronome
        final ImageView aiguille = new ImageView(new Image(Metronome.class.getResourceAsStream("images/aiguille.png")));
        aiguille.setFitHeight(32);
        aiguille.setPreserveRatio(true);
        aiguille.setTranslateX(16);
        aiguille.setTranslateY(2);

        // on applique une transformation à l'aiguille
        final Rotate rotation = new Rotate(0, 3, 29);
        aiguille.getTransforms().add(rotation);

        // création de l'animation de l'aiguille
        final Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, new KeyValue(rotation.angleProperty(), 45)),
            new KeyFrame(new Duration(1000), new KeyValue(rotation.angleProperty(), -45)));
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        this.getChildren().add(fond_metronome);
        this.getChildren().add(aiguille);
        this.setTranslateX(400);
        this.setTranslateY(200);
    }
}
