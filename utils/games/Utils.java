package utils.games;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by schwab on 23/12/2016.
 */
public class Utils {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");

    private static Bravo bravo = new Bravo();

    public static Image[] getImages(String folder) {

        File directory = new File(folder);

        ArrayList<Image> images = new ArrayList<>(directory.list().length);

        for(String imagePath : directory.list()){

            String fileSeparator = System.getProperties().getProperty("file.separator");

            String file = "file:" + directory.getAbsoluteFile() + fileSeparator + imagePath;

            if(file.indexOf("\\.")<0 && isImage(file)) //Problems with files starting with a point on Windows
                images.add(new Image(file));
        }

        return obj2im(images.toArray());
    }

    private static Image[] obj2im(Object[] objects) {

        Image[] images = new Image[objects.length];

        for(int i = 0; i < objects.length; i++){

            images[i] = (Image)objects[i];
        }

        return images;
    }

    public static boolean isImage(String file) {

        String mimetype= new MimetypesFileTypeMap().getContentType(file);

        return mimetype.startsWith("image/");
    }

    public static void clear(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        scene.setFill(Color.BLACK);

        for(Node N : root.getChildren()){

            N.setTranslateX(-10000);
        }

        root.getChildren().remove(0,root.getChildren().size());

        root.getChildren().add(bravo);

        root.getChildren().add(home(scene, root, cbxGames));
    }

    public static Node home(Scene scene, Group root, ChoiceBox<String> cbxGames) {


        double width = scene.getWidth()/10;
        double height = width;
        double X = scene.getWidth()*0.9;
        double Y = scene.getHeight()-height;

        Home home = new Home (X, Y, width, height);

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if(e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    clear(scene, root, cbxGames);
                    if (cbxGames != null) {
                        cbxGames.getSelectionModel().clearSelection();
                        root.getChildren().add(cbxGames);


                     cbxGames.setTranslateX(scene.getWidth() * 0.9 / 2);
                    cbxGames.setTranslateY(scene.getHeight() * 0.9 / 2);
                     }
                    root.getChildren().add(exit(scene));
                }
            }
        };

        home.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return home;
    }

    public static Node exit(Scene scene) {

        double width = scene.getWidth()/10;
        double heigth = width;
        double X = scene.getWidth()*0.9;
        double Y = scene.getHeight()-heigth;

        Rectangle exit = new Rectangle(X, Y, width, heigth);

        exit.setFill(new ImagePattern(new Image("data/common/images/power-off.png"),0,0,1,1, true));

        EventHandler<javafx.event.Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if(e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    System.exit(0);

                }
            }
        };

        exit.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return exit;
    }
}
