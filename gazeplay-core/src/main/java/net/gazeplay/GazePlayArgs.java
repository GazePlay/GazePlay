package net.gazeplay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Scanner;

@Slf4j
@Component
public class GazePlayArgs {

    public static String returnArgs(){

        String os = System.getProperty("os.name").toLowerCase();
        String data = "";

        try {
            File myFile = null;
            if (os.indexOf("nux") >= 0){
                myFile = new File("argsGazeplay.txt");
            }else if (os.indexOf("win") >= 0){
                String userName = System.getProperty("user.name");
                myFile = new File("C:\\Users\\" + userName + "\\Documents\\Gazeplay\\argsGazeplay.txt");
            }else {
                log.info("Os non reconnu !");
            }

            Scanner myReader = new Scanner(myFile);
            data = myReader.nextLine();
            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
