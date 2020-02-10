package melordi;

import javafx.scene.Parent;
import javafx.scene.effect.Reflection;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 * Created by schwab on 09/08/2016.
 */
public class Clavier extends Parent {

    public Clavier(final Instru instru) {

        final Rectangle fond_clavier = new Rectangle();
        fond_clavier.setWidth(400);
        fond_clavier.setHeight(200);
        fond_clavier.setArcWidth(30);
        fond_clavier.setArcHeight(30);
        fond_clavier.setFill( // on remplie notre rectangle avec un dégradé
            new LinearGradient(0f, 0f, 0f, 1f, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#333333")), new Stop(1, Color.web("#000000"))));
        final Reflection r = new Reflection();// on applique un effet de réflection
        r.setFraction(0.25);
        r.setBottomOpacity(0);
        r.setTopOpacity(0.5);
        fond_clavier.setEffect(r);

        final Touche[] touches = new Touche[]{new Touche("U", 50, 20, 60, instru), new Touche("I", 128, 20, 62, instru),
            new Touche("O", 206, 20, 64, instru), new Touche("P", 284, 20, 65, instru),
            new Touche("J", 75, 98, 67, instru), new Touche("K", 153, 98, 69, instru),
            new Touche("L", 231, 98, 71, instru), new Touche("M", 309, 98, 72, instru)};

        this.setTranslateX(50);
        this.setTranslateY(250);
        this.getChildren().add(fond_clavier);

        for (final Touche touche : touches) { // on insère chaque touche une par une.
            this.getChildren().add(touche);
        }

    }
}
