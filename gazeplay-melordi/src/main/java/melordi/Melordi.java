package melordi;/**
                * Created by schwab on 09/08/2016.
                */

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Melordi extends Application {

    public static void main(String[] args) {
        Application.launch(Melordi.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Melordi");
        Group root = new Group();
        Scene scene = new Scene(root, 500, 500, Color.WHITE);

        Instru mon_instru = new Instru();

        Clavier mon_clavier = new Clavier(mon_instru);// on créé un objet clavier
        ChangeInstru instrus = new ChangeInstru(mon_instru);

        root.getChildren().add(mon_clavier);// on l'ajoute à notre groupe root
        root.getChildren().add(instrus);

        Son mon_son = new Son(mon_clavier);
        root.getChildren().add(mon_son);

        Metronome mon_metronome = new Metronome();
        root.getChildren().add(mon_metronome);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
