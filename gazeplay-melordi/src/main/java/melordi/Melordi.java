package melordi;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Melordi extends Application {

    public static void main(final String[] args) {
        Application.launch(Melordi.class, args);
    }

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle("Melordi");
        final Group root = new Group();
        final Scene scene = new Scene(root, 500, 500, Color.WHITE);

        final Instru mon_instru = new Instru();

        final Clavier mon_clavier = new Clavier(mon_instru);// on créé un objet clavier
        final ChangeInstru instrus = new ChangeInstru(mon_instru);

        root.getChildren().add(mon_clavier);// on l'ajoute à notre groupe root
        root.getChildren().add(instrus);

        final Son mon_son = new Son(mon_clavier);
        root.getChildren().add(mon_son);

        final Metronome mon_metronome = new Metronome();
        root.getChildren().add(mon_metronome);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
