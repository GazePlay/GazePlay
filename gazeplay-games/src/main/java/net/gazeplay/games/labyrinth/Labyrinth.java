package net.gazeplay.games.labyrinth;

import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.frog.Nenuphar;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Labyrinth extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;
    private String gameName = "Labyrinth";
    private final Stats stats;
    public final double fixationlength;

    private GameBox[][] walls;
    private int[][] wallsPlacement;

    int nbBoxesLine = 4;
    int nbBoxesColumns = 6;
    int iteration = 1;
    int actualSeed;

    double entiereRecX;
    double entiereRecY;
    double entiereRecWidth;
    double entiereRecHeight;

    double caseHeight;
    double caseWidth;
    double adjustmentCaseWidth;
    double adjustmentCaseHeight;

    private Cheese cheese;
    private Mouse mouse;

    private final LabyrinthGameVariant variant;

    private final ReplayablePseudoRandom randomGenerator;
    private List<Integer> listAnim = new ArrayList<Integer>();

    private boolean firstTime = true;
    String pathStatsGame;
    public Timestamp timestamp;
    SimpleDateFormat sdf;
    String startTime;
    String endTime;
    ArrayList<Long> computerTimestamp = new ArrayList<>();
    ArrayList<Integer> step = new ArrayList<>();

    ArrayList<String> eventButtonUp = new ArrayList<>();
    ArrayList<String> fixationLengthButtonUp = new ArrayList<>();
    ArrayList<String> eventButtonDown = new ArrayList<>();
    ArrayList<String> fixationLengthButtonDown = new ArrayList<>();
    ArrayList<String> eventButtonRight = new ArrayList<>();
    ArrayList<String> fixationLengthButtonRight = new ArrayList<>();
    ArrayList<String> eventButtonLeft = new ArrayList<>();
    ArrayList<String> fixationLengthButtonLeft = new ArrayList<>();

    public Labyrinth(final IGameContext gameContext, final Stats stats, final LabyrinthGameVariant variant) {
        super();

        this.gameContext = gameContext;
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.stats = stats;

        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        this.variant = variant;
        final Configuration config = gameContext.getConfiguration();
        fixationlength = config.getFixationLength();

        this.generateStatsFolder();
    }

    public Labyrinth(final IGameContext gameContext, final Stats stats, final LabyrinthGameVariant variant, double gameSeed) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.variant = variant;
        final Configuration config = gameContext.getConfiguration();
        fixationlength = config.getFixationLength();

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
        if (this.firstTime){
            this.firstTime = false;

            this.sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            this.timestamp = new Timestamp(System.currentTimeMillis());

            this.computerTimestamp.add(this.timestamp.getTime());
            this.startTime = this.sdf.format(this.timestamp);
            this.step.add(this.iteration-1);

            this.eventButtonUp.add("");
            this.fixationLengthButtonUp.add("");
            this.eventButtonDown.add("");
            this.fixationLengthButtonDown.add("");
            this.eventButtonRight.add("");
            this.fixationLengthButtonRight.add("");
            this.eventButtonLeft.add("");
            this.fixationLengthButtonLeft.add("");

        }

    }

    public void updateStats(){
        this.computerTimestamp.add(new Timestamp(System.currentTimeMillis()).getTime());
        this.step.add(this.iteration);
    }

    public GameBox getBoxAt(final int i, final int j) {
        return walls[i][j];
    }

    public void prepareLaunch(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.debug("dimension2D = {}", dimension2D);

        entiereRecX = dimension2D.getWidth() * 0.25;
        entiereRecY = dimension2D.getHeight() * 0.15;
        entiereRecWidth = dimension2D.getWidth() * 0.6;
        entiereRecHeight = dimension2D.getHeight() * 0.7;

        caseWidth = entiereRecWidth / nbBoxesColumns;
        caseHeight = entiereRecHeight / nbBoxesLine;
        adjustmentCaseWidth = caseWidth / 6;
        adjustmentCaseHeight = caseHeight / 6;
    }

    @Override
    public void launch() {
        this.prepareLaunch();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Rectangle recJeu = new Rectangle(entiereRecX, entiereRecY, entiereRecWidth, entiereRecHeight);
        gameContext.getChildren().add(recJeu);

        this.wallsPlacement = constructionWallMatrix();
        walls = creationLabyrinth();

        // Creation of cheese
        cheese = new Cheese(entiereRecX, entiereRecY, caseHeight, caseHeight * 0.8, this, randomGenerator);
        mouse = createMouse();
        gameContext.getChildren().add(mouse);

        // launch of cheese
        cheese.beginCheese();
        gameContext.getChildren().add(cheese);
        gameContext.start();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        gameContext.getGazeDeviceManager().addStats(stats);

        this.firstStat();
    }

    private Mouse createMouse() {
        // Creation of the mouse
        Mouse mouse = new MouseArrowsV2(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
        mouse.setImage();
        return mouse;
    }

    public void nextLvl(){
        if (this.iteration < 1){
            this.iteration ++;
            this.gameContext.getChildren().clear();
            this.nbBoxesColumns += 1;
            this.nbBoxesLine += 1;
            this.launch();
        }else {
            this.dispose();
        }

    }
    @Override
    public void dispose() {
        this.endTime = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime() - this.timestamp.getTime());
        this.gameContext.getChildren().clear();
        this.createExcelFile();
        this.gameContext.showRoundStats(stats, this);
    }

    protected double positionX(final int j) {
        return entiereRecX + j * caseWidth;
    }

    protected double positionY(final int i) {
        return entiereRecY + i * caseHeight;
    }

    private GameBox[][] creationLabyrinth() {
        final GameBox[][] walls = new GameBox[nbBoxesLine][nbBoxesColumns];
        for (int i = 0; i < nbBoxesLine; i++) { // i = rows number = Coord Y
            for (int j = 0; j < nbBoxesColumns; j++) { // j = columns number = Coord X
                final GameBox g = new GameBox(caseHeight, caseWidth, entiereRecX + j * caseWidth,
                    entiereRecY + i * caseHeight, wallsPlacement[i][j], j, i);
                walls[i][j] = g;
                gameContext.getChildren().add(g);
            }
        }
        return walls;
    }

    public int generateSeed(){
        Random seed = new Random();
        return seed.nextInt(2);
    }

    private int[][] constructionWallMatrix() {
        this.actualSeed =  this.generateSeed();
        switch (this.iteration){
            case 1:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 0, 0, 0, 0, 1},
                            {0, 1, 1, 1, 0, 0},
                            {0, 0, 0, 1, 1, 1},
                            {1, 1, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0},
                            {0, 0, 0, 1, 1, 0},
                            {1, 1, 0, 1, 1, 0},
                            {1, 0, 0, 0, 1, 0}
                        };
                }

            case 2:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 1, 0},
                            {0, 1, 0, 1, 0, 0, 0},
                            {0, 0, 0, 1, 1, 1, 1},
                            {0, 1, 0, 1, 0, 0, 0},
                            {0, 1, 0, 0, 0, 1, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 0, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 0, 1},
                            {0, 1, 0, 0, 0, 0, 0},
                            {0, 0, 0, 1, 0, 1, 0},
                            {1, 1, 1, 0, 0, 1, 0}
                        };
                }

            case 3:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 0, 0, 0},
                            {0, 1, 0, 0, 0, 1, 1, 0},
                            {0, 1, 1, 1, 0, 0, 0, 1},
                            {0, 0, 0, 0, 0, 1, 0, 0},
                            {1, 1, 1, 1, 1, 1, 1, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 0, 0, 0, 1, 1},
                            {0, 1, 1, 1, 1, 0, 1, 1},
                            {0, 0, 0, 1, 1, 0, 0, 0},
                            {0, 1, 0, 0, 1, 1, 0, 1},
                            {1, 1, 1, 0, 1, 0, 0, 0}
                        };
                }

            case 4:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 0, 0, 0},
                            {0, 1, 1, 1, 0, 0, 0, 1, 0},
                            {0, 1, 0, 0, 0, 1, 1, 1, 1},
                            {0, 1, 0, 1, 0, 0, 0, 0, 0},
                            {0, 0, 0, 1, 1, 1, 0, 1, 1},
                            {0, 1, 1, 0, 0, 0, 0, 1, 1},
                            {0, 0, 0, 0, 1, 1, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 0, 0, 0, 0, 1, 1, 1, 1},
                            {0, 1, 1, 1, 0, 1, 0, 0, 0},
                            {0, 0, 0, 1, 0, 0, 0, 1, 0},
                            {1, 1, 0, 1, 1, 1, 1, 0, 0},
                            {1, 0, 0, 0, 1, 1, 1, 0, 1},
                            {1, 0, 1, 0, 0, 1, 1, 0, 1},
                            {0, 0, 1, 1, 0, 1, 0, 0, 0}
                        };
                }

            case 5:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 1, 0, 0},
                            {0, 0, 0, 1, 1, 1, 0, 0, 0, 1},
                            {0, 1, 0, 0, 0, 0, 1, 1, 1, 0},
                            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 1, 1, 0, 1, 1},
                            {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1},
                            {1, 1, 0, 0, 0, 0, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 1, 1, 1},
                            {0, 0, 0, 1, 1, 1, 0, 0, 1, 1},
                            {0, 1, 1, 0, 0, 1, 1, 0, 1, 1},
                            {0, 0, 0, 0, 0, 1, 1, 0, 0, 0},
                            {0, 1, 1, 1, 0, 1, 1, 0, 1, 1},
                            {0, 1, 0, 0, 0, 1, 1, 0, 0, 0},
                            {1, 1, 0, 1, 0, 0, 0, 1, 1, 0},
                            {1, 1, 0, 1, 1, 1, 0, 1, 0, 0}
                        };
                }

            case 6:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1},
                            {0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0},
                            {0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 0},
                            {0, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1},
                            {0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                            {0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0},
                            {1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0},
                            {1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                            {0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1},
                            {0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0},
                            {0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
                            {0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1},
                            {0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1},
                            {1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0}
                        };
                }

            case 7:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
                            {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1},
                            {0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0},
                            {0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},
                            {0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0},
                            {0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0},
                            {0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0},
                            {0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
                            {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1},
                            {0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0},
                            {0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},
                            {0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0},
                            {1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0},
                            {0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0},
                            {0, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0}
                        };
                }

            default:
                return new int[][]
                    {
                        {0, 0},
                        {0, 0}
                    };
        }
    }

    boolean isFreeForMouse(final int i, final int j) {
        if (i >= nbBoxesLine || j >= nbBoxesColumns) {
            return false;
        }
        return (!walls[i][j].isAWall());
    }

    boolean isFreeForCheese(final int i, final int j) {
        return (!walls[i][j].isAWall() && !mouse.isTheMouse(i, j));
    }

    void testIfCheese(final int i, final int j) {
        if (cheese.isTheCheese(i, j)) {
            stats.incrementNumberOfGoalsReached();
            gameContext.updateScore(stats, this);
            cheese.moveCheese();
            stats.incrementNumberOfGoalsToReach();
            mouse.nbMove = 0;
            this.nextLvl();
        }
    }

    public void createExcelFile(){
        String pathStats = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";
        this.stats.actualFile = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet(this.gameName);

        Object[][] bookData = new Object[this.computerTimestamp.size()+1][13];

        bookData[0][0] = "Recording timestamp";
        bookData[0][1] = "Computer timestamp";
        bookData[0][2] = "Recording start time";
        bookData[0][3] = "Recording duration";
        bookData[0][4] = "Step";
        bookData[0][5] = "Event Button Up";
        bookData[0][6] = "Fixation Button Up";
        bookData[0][7] = "Event Button Right";
        bookData[0][8] = "Fixation Button Right";
        bookData[0][9] = "Event Button Down";
        bookData[0][10] = "Fixation Button Down";
        bookData[0][11] = "Event Button Left";
        bookData[0][12] = "Fixation Button Left";

        for (int i=0; i<this.computerTimestamp.size(); i++){
            bookData[i+1][0] = String.valueOf(this.computerTimestamp.get(i) - this.computerTimestamp.get(0));
            bookData[i+1][1] = String.valueOf(this.computerTimestamp.get(i));
            bookData[i+1][2] = String.valueOf(this.startTime);
            bookData[i+1][3] = String.valueOf(this.endTime);
            bookData[i+1][4] = String.valueOf(this.step.get(i));
            bookData[i+1][5] = String.valueOf(this.eventButtonUp.get(i));
            bookData[i+1][6] = String.valueOf(this.fixationLengthButtonUp.get(i));
            bookData[i+1][7] = String.valueOf(this.eventButtonRight.get(i));
            bookData[i+1][8] = String.valueOf(this.fixationLengthButtonRight.get(i));
            bookData[i+1][9] = String.valueOf(this.eventButtonDown.get(i));
            bookData[i+1][10] = String.valueOf(this.fixationLengthButtonDown.get(i));
            bookData[i+1][11] = String.valueOf(this.eventButtonLeft.get(i));
            bookData[i+1][12] = String.valueOf(this.fixationLengthButtonLeft.get(i));
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
