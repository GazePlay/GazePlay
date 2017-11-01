package tobii.installation;

import gaze.configuration.Configuration;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import utils.games.Utils;

import java.io.File;

public class TobiiSetup extends Application {

    static String file1 = "tobii_stream_engine.dll";
    static String file2 = "GazePlayTobiiLibrary2.dll";

    public static void main(String[] args) {
        Application.launch(TobiiSetup.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        boolean returnValue = false;

        // if (System.getProperty("os.name").indexOf("indow") > 0) {

        (new File(Utils.getGazePlayFolder())).mkdir();
        (new File(Utils.getDllFolder())).mkdir();
        boolean bool1 = Utils.copyFromJar(file1, Utils.getDllFolder() + file1);
        boolean bool2 = Utils.copyFromJar(file2, Utils.getDllFolder() + file2);
        returnValue = bool1 && bool2;
        // }

        Configuration C = new Configuration("tobii");
        C.saveConfig();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tobii Eye-Tracker Set Up");
        alert.setHeaderText(null);
        if (returnValue)
            alert.setContentText("Installation Ok !!");
        else
            alert.setContentText("Installation not done\n\nSorry, Tobii trackers only designed for Windows.");

        alert.showAndWait();
    }
}
