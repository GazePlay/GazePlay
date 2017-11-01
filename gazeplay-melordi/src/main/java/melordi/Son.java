package melordi;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;

/**
 * Created by schwab on 10/08/2016.
 */
public class Son extends Parent {

    public Slider slider;
    private Clavier clavier;

    public Son(Clavier clavier) {

        this.clavier = clavier;

        slider = new Slider(0, 127, 60);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setTranslateY(35);
        slider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                clavier.requestFocus();
            }
        });

        ProgressIndicator indicateur = new ProgressIndicator(0.0);
        indicateur.progressProperty().bind(slider.valueProperty().divide(127.0));
        indicateur.setTranslateX(-15);

        this.getChildren().add(slider);
        this.getChildren().add(indicateur);
        this.setTranslateY(260);
        this.setTranslateX(60);

    }
}