package net.gazeplay.games.frog;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.stats.Stats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class Frog implements GameLifeCycle {

    private String gameName = "Frog";
    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom randomGenerator;
    public Timestamp timestamp;
    IA ia;
    Nenuphar[] nenuphars;
    ImageView frog;
    int frogPosition;
    int correctFrogPosition;
    int actualIteration = 0;
    int nbNenuphars = 10;
    Rectangle2D screensBounds;
    double screenWidth;
    double screenHeight;
    ImageView info;
    String pathStatsGame;
    SimpleDateFormat sdf;
    ArrayList<Long> computerTimestamp = new ArrayList<>();
    ArrayList<Integer> step = new ArrayList<>();
    ArrayList<Integer> goodAnswer = new ArrayList<>();
    String startTime;
    String endTime;

    public Frog(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(this.randomGenerator.getSeed());

        this.generateStatsFolder();
    }

    public Frog(IGameContext gameContext, Stats stats, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.generateStatsFolder();
    }

    public void generateStatsFolder(){
        String username = System.getProperty("user.name");
        String directory = "C:/Users/" + username + "/Documents/Picardie_Project";
        String subDirectory = directory + "/" + this.gameName;

        File picardieProjet = new File(directory);
        if (!picardieProjet.exists()){
            picardieProjet.mkdirs();
        }

        File subFolder = new File(subDirectory);
        if (!subFolder.exists()){
            subFolder.mkdirs();
        }

        int index = 1;
        File dir = new File(subDirectory, this.gameName + index + "_" + DateUtils.today());
        while (dir.exists()){
            index++;
            dir = new File(subDirectory, this.gameName + index + "_" + DateUtils.today());
        }
        dir.mkdirs();
        this.pathStatsGame = dir.getPath();
    }

    public void firstStat(){
        this.sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        this.timestamp = new Timestamp(System.currentTimeMillis());

        this.computerTimestamp.add(this.timestamp.getTime());
        this.startTime = this.sdf.format(this.timestamp);
        this.step.add(this.actualIteration);
        this.goodAnswer.add(this.correctFrogPosition);

        for (Nenuphar nenuphar: this.nenuphars){
            nenuphar.eventNenuphar.add("");
            nenuphar.fixationLengthNenuphar.add("");
        }
    }

    public void updateStats(int indexNenuphar){
        this.computerTimestamp.add(new Timestamp(System.currentTimeMillis()).getTime());
        this.step.add(this.actualIteration);
        this.goodAnswer.add(this.correctFrogPosition);

        for (int i=0; i<this.nenuphars.length; i++){
            if (i != indexNenuphar){
                this.nenuphars[i].eventNenuphar.add("");
                this.nenuphars[i].fixationLengthNenuphar.add("");
            }
        }
    }

    @Override
    public void launch() {
        this.gameContext.setLimiterAvailable();
        this.stats.notifyNewRoundReady();
        this.gameContext.getGazeDeviceManager().addStats(this.stats);
        this.gameContext.firstStart();

        this.createInfo();
        Image nenupharImg = new Image("data/frog/images/lotus.png");
        Image frogImg = new Image("data/frog/images/frog.png");

        this.screensBounds = Screen.getPrimary().getVisualBounds();
        this.screenWidth = this.screensBounds.getWidth();
        this.screenHeight = this.screensBounds.getHeight();

        this.frog = new ImageView(frogImg);

        this.drawNenuphars(this.screenWidth, this.screenHeight, nenupharImg);
        this.generateRandomStart();

        this.gameContext.getChildren().add(this.frog);

        this.ia = new IA(this, this.gameContext);
        this.firstStat();
        this.iaPlay();
    }

    public void iaPlay(){
        this.actualIteration++;
        this.ia.iaMoves(this.actualIteration);
    }

    public void generateRandomStart(){
        Random random = new Random();
        this.frogPosition = random.nextInt(this.nbNenuphars);
        moveFrogTo(this.nenuphars[this.frogPosition]);
    }

    public void drawNenuphars(double screenWidth, double screenHeight, Image nenupharImg){
        this.nenuphars = new Nenuphar[this.nbNenuphars];
        for (int i=0; i<this.nbNenuphars; i++){
            this.nenuphars[i] = new Nenuphar(screenWidth, screenHeight, nenupharImg, i, this.nbNenuphars, this.gameContext, this);
        }
    }

    public void moveFrogTo(Nenuphar nenuphar){
        double frogSize = nenuphar.nenupharImgView.getFitWidth() * 0.5;
        this.frog.setFitWidth(frogSize);
        this.frog.setFitHeight(frogSize);
        this.frog.setX(nenuphar.nenupharImgView.getX() + (nenuphar.nenupharImgView.getFitWidth() - frogSize) / 2);
        this.frog.setY(nenuphar.nenupharImgView.getY() + (nenuphar.nenupharImgView.getFitHeight() - frogSize) / 2);

        for (Nenuphar value : this.nenuphars) {
            value.haveFrog = false;
        }
        nenuphar.haveFrog = true;
    }

    public void setGoodAnswer(String moveType){
        switch (moveType){
            case "oneBack":
                this.correctFrogPosition = this.frogPosition - 1;
                if (this.correctFrogPosition < 0){
                    this.correctFrogPosition = this.nenuphars.length - 1;
                }
                break;

            case "oneFront":
                this.correctFrogPosition = this.frogPosition + 1;
                if (this.correctFrogPosition > 9){
                    this.correctFrogPosition = 0;
                }
                break;

            case "twoBack":
                this.correctFrogPosition = this.frogPosition - 2;
                if (this.correctFrogPosition == -1){
                    this.correctFrogPosition = this.nenuphars.length - 1;
                }else {
                    this.correctFrogPosition = this.nenuphars.length - 2;
                }
                break;

            case "jump":
                if (this.correctFrogPosition != this.ia.futureFrogPosition){
                    this.correctFrogPosition = this.ia.futureFrogPosition;
                }else {
                    this.correctFrogPosition = this.ia.pastFrogPosition;
                }
                break;

            default:
                break;
        }
    }

    public void playerTurn(){
        for (Nenuphar value : this.nenuphars) {
            value.ignoreInput = false;
            value.nenupharImgView.setOpacity(1);
        }
        this.info.setVisible(true);
    }

    public void iaTurn(){
        for (Nenuphar value : this.nenuphars) {
            value.ignoreInput = true;
            value.nenupharImgView.setOpacity(0.5);
        }

        this.info.setVisible(false);
        this.iaPlay();
    }

    public void createInfo(){
        Region region = this.gameContext.getRoot();
        this.info = new ImageView("data/frog/images/frogInfo.png");
        this.info.setFitWidth(region.getWidth());
        this.info.setFitHeight(region.getHeight());
        this.info.setVisible(false);
        this.gameContext.getChildren().add(this.info);
    }

    @Override
    public void dispose() {
        this.endTime = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime() - this.timestamp.getTime());
        this.gameContext.getChildren().clear();
        this.createExcelFile();
        this.gameContext.showRoundStats(this.stats, this);
    }

    @SuppressWarnings("PMD")
    public void createExcelFile(){
        String pathStats = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";
        this.stats.actualFile = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet(this.gameName);

        Object[][] bookData = new Object[this.computerTimestamp.size()+1][26];

        bookData[0][0] = "Recording timestamp";
        bookData[0][1] = "Computer timestamp";
        bookData[0][2] = "Recording start time";
        bookData[0][3] = "Recording duration";
        bookData[0][4] = "Step";
        bookData[0][5] = "Good answer";
        bookData[0][6] = "Event Nenuphar 1";
        bookData[0][7] = "Fixation Length Nenuphar 1";
        bookData[0][8] = "Event Nenuphar 2";
        bookData[0][9] = "Fixation Length Nenuphar 2";
        bookData[0][10] = "Event Nenuphar 3";
        bookData[0][11] = "Fixation Length Nenuphar 3";
        bookData[0][12] = "Event Nenuphar 4";
        bookData[0][13] = "Fixation Length Nenuphar 4";
        bookData[0][14] = "Event Nenuphar 5";
        bookData[0][15] = "Fixation Length Nenuphar 5";
        bookData[0][16] = "Event Nenuphar 6";
        bookData[0][17] = "Fixation Length Nenuphar 6";
        bookData[0][18] = "Event Nenuphar 7";
        bookData[0][19] = "Fixation Length Nenuphar 7";
        bookData[0][20] = "Event Nenuphar 8";
        bookData[0][21] = "Fixation Length Nenuphar 8";
        bookData[0][22] = "Event Nenuphar 9";
        bookData[0][23] = "Fixation Length Nenuphar 9";
        bookData[0][24] = "Event Nenuphar 10";
        bookData[0][25] = "Fixation Length Nenuphar 10";

        for (int i=0; i<this.computerTimestamp.size(); i++){
            bookData[i+1][0] = String.valueOf(this.computerTimestamp.get(i) - this.computerTimestamp.get(0));
            bookData[i+1][1] = String.valueOf(this.computerTimestamp.get(i));
            bookData[i+1][2] = String.valueOf(this.startTime);
            bookData[i+1][3] = String.valueOf(this.endTime);
            bookData[i+1][4] = String.valueOf(this.step.get(i));
            bookData[i+1][5] = String.valueOf(this.goodAnswer.get(i)+1);

            for (int j=0; j<this.nenuphars.length; j++){
                bookData[i+1][6+j+j] = String.valueOf(this.nenuphars[j].eventNenuphar.get(i));
                bookData[i+1][7+j+j] = String.valueOf(this.nenuphars[j].fixationLengthNenuphar.get(i));
            }
        }

        int rowCount = 0;

        for (Object[] aBook : bookData) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(columnCount++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(pathStats)) {
            workbook.write(outputStream);
        } catch (Exception e){
            log.info("Error creation xls for GazePlay Eval stats game !");
            e.printStackTrace();
        }
    }
}
