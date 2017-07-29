package utils.games;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import javax.activation.MimetypesFileTypeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

        for (String imagePath : directory.list()) {

            String fileSeparator = System.getProperties().getProperty("file.separator");

            String file = "file:" + directory.getAbsoluteFile() + fileSeparator + imagePath;

            if (file.indexOf("\\.") < 0 && isImage(file)) //Problems with files starting with a point on Windows
                images.add(new Image(file));
        }

        return obj2im(images.toArray());
    }

    private static Image[] obj2im(Object[] objects) {

        Image[] images = new Image[objects.length];

        for (int i = 0; i < objects.length; i++) {

            images[i] = (Image) objects[i];
        }

        return images;
    }

    public static boolean isImage(String file) {

        String mimetype = new MimetypesFileTypeMap().getContentType(file);

        return mimetype.startsWith("image/");
    }

    public static void clear(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        scene.setFill(Color.BLACK);

        for (Node N : root.getChildren()) {

            N.setTranslateX(-10000);
        }

        root.getChildren().remove(0, root.getChildren().size());

        root.getChildren().add(bravo);


    }

    public static Node home(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        double width = scene.getWidth() / 10;
        double height = width;
        double X = scene.getWidth() * 0.9;
        double Y = scene.getHeight() - height * 1.1;

        Home home = new Home(X, Y, width, height);

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    clear(scene, root, cbxGames);


                    if (cbxGames != null) {
                        cbxGames.getSelectionModel().clearSelection();
                        root.getChildren().add(cbxGames);

                        cbxGames.setTranslateX(scene.getWidth() * 0.9 / 2);
                        cbxGames.setTranslateY(scene.getHeight() * 0.9 / 2);

                        addButtons(scene, root, cbxGames);
                    }

                }
            }
        };

        home.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return home;
    }

    public static void addButtons(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        double width = scene.getWidth() / 10;
        double heigth = width;
        double XExit = scene.getWidth() * 0.9;
        double XLicence = scene.getWidth() * 0.0;
        double Y = scene.getHeight() - heigth * 1.1;

      //  License license = new License(XLicence, Y, width, heigth, scene, root, cbxGames);

       // root.getChildren().add(license);

        Rectangle exit = new Rectangle(XExit, Y, width, heigth);

        exit.setFill(new ImagePattern(new Image("data/common/images/power-off.png"), 0, 0, 1, 1, true));

        EventHandler<javafx.event.Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    System.exit(0);

                }
            }
        };

        exit.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        root.getChildren().add(exit);

        root.getChildren().add(logo(scene));

        root.getChildren().add(Utils.BuildLicence());
    }

    public static MenuBar BuildLicence() {

        MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menu = new Menu("GazePlay");

        StringBuilder licence = new StringBuilder(10000);
        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader("data/common/licence.txt"));

            while ((line = br.readLine()) != null) {
                licence.append('\n');
                licence.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MenuItem MenuLicence = new MenuItem(licence.toString());

        menu.getItems().add(MenuLicence);

        menuBar.getMenus().addAll(menu);

        return menuBar;
    }

    public static Node logo(Scene scene) {

        double width = scene.getWidth()*0.5;
        double height = scene.getHeight()*0.2;

        double posY = scene.getHeight()*0.1;
        double posX = (scene.getWidth() - width)/2;

        Rectangle logo = new Rectangle(posX,posY, width,height);
        logo.setFill(new ImagePattern(new Image("data/common/images/gazeplay.jpg"),0,0,1,1, true));

        return logo;
    }
}
