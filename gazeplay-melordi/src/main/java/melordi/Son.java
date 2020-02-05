package melordi;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;

/**
 * Created by schwab on 10/08/2016.
 */
public class Son extends Parent {

    public final Slider slider;

    public Son(final Clavier clavier) {

        slider = new Slider(0, 127, 60);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setTranslateY(35);
        slider.valueProperty().addListener((ChangeListener) (o, oldVal, newVal) -> clavier.requestFocus());

        final ProgressIndicator indicateur = new ProgressIndicator(0.0);
        indicateur.progressProperty().bind(slider.valueProperty().divide(127.0));
        indicateur.setTranslateX(-15);

        this.getChildren().add(slider);
        this.getChildren().add(indicateur);
        this.setTranslateY(260);
        this.setTranslateX(60);

    }
}
