package net.gazeplay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
@Component
public class GazePlayArgs {

    public static String returnArgs(){

        String os = System.getProperty("os.name").toLowerCase();
        String data = "";

        try {
            File myFile = null;
            if (os.contains("nux")){
                myFile = new File("argsGazeplay.txt");
            }else if (os.contains("win")){
                String userName = System.getProperty("user.name");
                myFile = new File("C:\\Users\\" + userName + "\\Documents\\Gazeplay\\argsGazeplay.txt");
            }else {
                log.info("Os non reconnu !");
            }

            assert myFile != null;
            Scanner myReader = new Scanner(myFile, StandardCharsets.UTF_8);
            data = myReader.nextLine();
            myReader.close();
            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
