package net.gazeplay;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import net.gazeplay.commons.threads.LoggingUncaughtExceptionHandler;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadFactory;

@Slf4j
@SpringBootApplication
public class GazePlayLauncher {

    @Setter
    public static boolean doStartBootstrapThread = true;

    public static void main(String[] args) {

        try {
            if (args[0].contains("afsr")){
                log.info("AFSR GAZEPLAY");
                saveArgs("afsrGazeplay");
            } else if (args[0].contains("bera")){
                log.info("BERA GAZEPLAY");
                saveArgs("bera");
            } else if (args[0].contains("emmanuel")){
                log.info("EMMANUEL GAZEPLAY");
                saveArgs("emmanuel");
            }
        } catch (Exception e) {
            log.info("GAZEPLAY");
            saveArgs("gazeplay");
        }

        Thread.currentThread().setName(GazePlayLauncher.class.getSimpleName() + "-main");
        Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());

        Runnable rootTask = () -> {

            SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
            printStartupInfo();
            //fixDefaultCharset();

            initGazePlayDirectory();

            System.exit(SpringApplication.exit(SpringApplication.run(GazePlayLauncher.class, args)));
        };

        ThreadFactory threadFactory = new CustomThreadFactory("application-bootsrap",
            new GroupingThreadFactory("application-bootsrap-group"));
        Thread bootstrapThread = threadFactory.newThread(rootTask);
        bootstrapThread.start();
    }

    private static void initGazePlayDirectory() {
        // creation of GazePlay default folder if it does not exist.
        File gazePlayDirectory = GazePlayDirectories.getGazePlayFolder();
        log.info("FOLDER PATH " + gazePlayDirectory);
        if (!gazePlayDirectory.exists()) {
            boolean gazePlayDirectoryCreated = gazePlayDirectory.mkdir();
            log.debug("gazePlayDirectoryCreated = " + gazePlayDirectoryCreated);
        }
    }

    private static void printStartupInfo() {
        final String versionInfo = VersionInfo.findVersionInfo();

        log.info("GazePlay");
        log.info("Version : " + versionInfo);
        Utils.logSystemProperties();
        log.info("Max Memory: " + Runtime.getRuntime().maxMemory());

        File workingDirectory = new File(".").getAbsoluteFile();
        log.info("workingDirectory = {}", workingDirectory.getAbsolutePath());
    }

    private static void fixDefaultCharset() {
        try {
            System.setProperty("file.encoding", "UTF-8");
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Exception while fixing default Charset", e);
        }
    }

    @SuppressWarnings("PMD")
    private static void saveArgs(String args){

        String os = System.getProperty("os.name").toLowerCase();
        FileWriter myWritter = null;

        try {
            if (os.contains("nux") || os.contains("mac")){
                File myFile = new File("argsGazeplay.txt");
                log.info("Fil args is : " + myFile);
                myWritter = new FileWriter("argsGazeplay.txt", StandardCharsets.UTF_8);
                myWritter.write(args);
            }else if (os.contains("win")){
                String userName = System.getProperty("user.name");
                File myFolder = new File("C:\\Users\\" + userName + "\\Documents\\Gazeplay");
                boolean createFolder = myFolder.mkdirs();
                log.info("Folder created, path = " + createFolder);
                File myFile = new File("C:\\Users\\" + userName + "\\Documents\\Gazeplay\\argsGazeplay.txt");
                log.info("Fil args is : " + myFile);
                myWritter = new FileWriter("C:\\Users\\" + userName + "\\Documents\\Gazeplay\\argsGazeplay.txt", StandardCharsets.UTF_8);
                myWritter.write(args);
            }
            else{
                log.info("OS non reconnu !");
            }
        } catch (IOException e){
            log.info(String.valueOf(e));
        } finally {
            try {
                if (myWritter != null){
                    myWritter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
