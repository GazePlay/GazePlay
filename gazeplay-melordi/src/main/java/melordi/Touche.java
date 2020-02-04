package melordi;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Created by schwab on 09/08/2016.
 */
public class Touche extends Parent {

    public final String lettre;// lettre de la touche, c'est une variable public  pour qu'elle puisse être lue depuis les
    private final int positionY;// ordonnée de la touche
    private final int note;// note correspond au numéro MIDI de la note qui doit être jouée quand on appuie sur la touche
    private final Instru instru;

    final Rectangle fond_touche;
    final Text lettre_touche;

    public Touche(final String l, final int posX, final int posY, final int note, final Instru instru) {
        lettre = l;
        // autres classes
        // abscisse
        positionY = posY;
        this.note = note;
        this.instru = instru;

        fond_touche = new Rectangle(75, 75, Color.WHITESMOKE);
        fond_touche.setArcHeight(10);
        fond_touche.setArcWidth(10);
        this.getChildren().add(fond_touche);// ajout du rectangle de fond de la touche

        lettre_touche = new Text(lettre);
        lettre_touche.setFont(new Font(25));
        lettre_touche.setFill(Color.GREY);
        lettre_touche.setX(25);
        lettre_touche.setY(45);
        this.getChildren().add(lettre_touche);// ajout de la lettre de la touche

        /*
         * Light.Distant light = new Light.Distant(); light.setAzimuth(-45.0); Lighting li = new Lighting();
         * li.setLight(light); fond_touche.setEffect(li);
         */

        this.setTranslateX(posX);// positionnement de la touche sur le clavier
        this.setTranslateY(positionY);

        this.setOnMouseEntered(me -> fond_touche.setFill(Color.LIGHTGREY));
        this.setOnMouseExited(me -> fond_touche.setFill(Color.WHITE));
        this.setOnMousePressed(me -> appuyer());
        this.setOnMouseReleased(me -> relacher());
    }

    public void appuyer() {
        fond_touche.setFill(Color.DARKGREY);
        this.setTranslateY(positionY + 2);
        instru.note_on(note);
    }

    public void relacher() {
        fond_touche.setFill(Color.WHITE);
        this.setTranslateY(positionY);
        instru.note_off(note);
    }

}
