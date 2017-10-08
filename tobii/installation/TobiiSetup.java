package tobii.installation;

import utils.games.Utils;

import java.io.File;

public class TobiiSetup {

    String file1 = "tobii_stream_engine.dll";
    String file2 = "GazePlayTobiiLibrary2.dll";

    public static void main(String [] argv){

        if(System.getProperty("os.name").indexOf("indow")<0) {

            (new File(Utils.getGazePlayFolder())).mkdir();
            (new File(Utils.getDllFolder())).mkdir();

        }
    }
}
