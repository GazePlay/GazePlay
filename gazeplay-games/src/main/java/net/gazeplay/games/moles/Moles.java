package net.gazeplay.games.moles;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Moles extends Parent implements GameLifeCycle {

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        public final List<MolesChar> molesList;
        public final List<MolesObj> molesObjList;
    }

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    private int nbMolesWhacked = 0;

    public int limitMoleEntity = 0;
    public int limitObjEntity = 2;
    @Getter
    private AtomicInteger nbMolesOut = new AtomicInteger(0);
    @Getter
    private AtomicInteger nbObjOut = new AtomicInteger(0);
    private String gameName = "Taupe";
    String pathStatsGame;
    private Label lab;

    private RoundDetails currentRoundDetails;

    @Getter
    @Setter
    private ArrayList<TargetAOI> targetAOIList;
    private double moleRadius;

    private Timer minuteur;

    private final ReplayablePseudoRandom randomGenerator;

    private MolesGameVariant variant;
    private Text ruleText;
    private ImageView imgMoles;
    private String textRule = "Tape les taupes avec une carrote";
    public int difficulty = 1;
    Timeline difficulty1;
    Timeline difficulty2;
    Timeline difficulty3;
    SimpleDateFormat sdf;
    ArrayList<Long> computerTimestamp = new ArrayList<>();
    ArrayList<Integer> step = new ArrayList<>();
    String startTime;
    ArrayList<Integer> score = new ArrayList<>();
    String endTime;
    public Timestamp timestamp;
    Boolean firstTime = true;

    ArrayList<String> eventImage = new ArrayList<>();
    ArrayList<String> fixationLengthImage = new ArrayList<>();

    Moles(IGameContext gameContext, Stats stats, final MolesGameVariant type) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        targetAOIList = new ArrayList<>();
        moleRadius = 0;
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
        this.variant = type;
        this.generateStatsFolder();
    }

    Moles(IGameContext gameContext, Stats stats, final MolesGameVariant type, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        targetAOIList = new ArrayList<>();
        moleRadius = 0;
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
        this.variant = type;
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
        if (firstTime){
            firstTime = false;

            this.sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            this.timestamp = new Timestamp(System.currentTimeMillis());

            this.computerTimestamp.add(this.timestamp.getTime());
            this.startTime = this.sdf.format(this.timestamp);
            this.step.add(this.difficulty);
            this.score.add(this.nbMolesWhacked);
            eventImage.add("");
            fixationLengthImage.add("");
        }
    }

    public void onSelectedImg(String event, String value){
        this.computerTimestamp.add(new Timestamp(System.currentTimeMillis()).getTime());
        this.step.add(this.difficulty);
        this.score.add(this.nbMolesWhacked);
        eventImage.add(event);
        fixationLengthImage.add(value);
    }

    @Override
    public void launch() {
        final Transition animation = createRuleTransition();
        animation.play();
    }

    public Transition createRuleTransition(){
        ruleText = new Text(this.textRule);
        ruleText.setTranslateY(0);

        if (this.difficulty < 3){
            imgMoles = new ImageView(new Image("data/whackmole/images/molesCarrot.png"));
            imgMoles.setFitWidth(200);
            imgMoles.setFitHeight(200);
        }else {
            imgMoles = new ImageView(new Image("data/whackmole/images/moles.png"));
            imgMoles.setFitWidth(200);
            imgMoles.setFitHeight(200);
        }

        final String color = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<>() {
            @Override
            public String visitLight() {
                return "titleB";
            }

            @Override
            public String visitDark() {
                return "titleW";
            }
        });

        ruleText.setId(color);

        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double positionX = gamePaneDimension2D.getWidth() / 2 - ruleText.getBoundsInParent().getWidth() * 2;
        final double positionY = gamePaneDimension2D.getHeight() / 2 - ruleText.getBoundsInParent().getHeight() / 2;

        ruleText.setX(positionX);
        ruleText.setY(positionY);
        ruleText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(ruleText, Pos.CENTER);

        imgMoles.setX(positionX);
        imgMoles.setY(positionY);
        StackPane.setAlignment(imgMoles, Pos.BOTTOM_CENTER);

        gameContext.getChildren().add(ruleText);
        gameContext.getChildren().add(imgMoles);

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), ruleText);

        fullAnimation.setDelay(Duration.millis(3000));

        fullAnimation.setOnFinished(actionEvent -> {
            gameContext.getChildren().remove(ruleText);
            this.startGame();
        });

        return fullAnimation;
    }

    public void startGame(){
        if (currentRoundDetails != null) {
            if (currentRoundDetails.molesList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.molesList);
                currentRoundDetails.molesList.clear();
            }
            currentRoundDetails = null;
        }
        targetAOIList.clear();
        gameContext.getChildren().clear();

        gameContext.setLimiterAvailable();
        gameContext.start();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle imageFond = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageFond.setFill(new ImagePattern(new Image("data/whackmole/images/molesGround.jpg")));
        adjustBackground(imageFond);
        gameContext.getChildren().add(imageFond);

        List<MolesChar> molesList = initMoles(variant, randomGenerator);
        List<MolesObj> molesObjList = initObj(variant, randomGenerator);
        currentRoundDetails = new RoundDetails(molesList, molesObjList);
        this.getChildren().addAll(molesList);
        this.getChildren().addAll(molesObjList);
        gameContext.getChildren().add(this);

        Rectangle imageFondTrans = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageFondTrans.setFill(new ImagePattern(new Image("data/whackmole/images/molesGroundTransparent.png")));
        adjustBackground(imageFondTrans);
        gameContext.getChildren().add(imageFondTrans);

        /* Score display */
        lab = new Label();
        String s = "Score:" + nbMolesWhacked;
        lab.setText(s);
        Color col = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Color>() {
            @Override
            public Color visitLight() {
                return Color.BLACK;
            }

            @Override
            public Color visitDark() {
                return Color.WHITE;
            }
        });
        lab.setTextFill(col);
        lab.setFont(Font.font(dimension2D.getHeight() / 14));
        lab.setLineSpacing(10);
        lab.setLayoutX(0.4 * dimension2D.getWidth());
        lab.setLayoutY(0.08 * dimension2D.getHeight());
        gameContext.getChildren().add(lab);

        this.gameContext.resetBordersToFront();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        this.updateDifficulty1();
        this.updateDifficulty2();
        this.updateDifficulty3();
        play();
        this.firstStat();
    }

    public void updateDifficulty1(){
        difficulty1 = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            this.limitObjEntity += 2;
        }));

        difficulty1.setOnFinished(event -> {

            minuteur.purge();
            minuteur.cancel();
            difficulty1.stop();
            this.textRule = "Tape les taupes avec une carrote";
            this.reset();
        });

        difficulty1.setCycleCount(3);
    }

    public void updateDifficulty2(){
        difficulty2 = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            this.limitMoleEntity ++;
            this.limitObjEntity ++;
        }));

        difficulty2.setOnFinished(event -> {

            minuteur.purge();
            minuteur.cancel();
            difficulty2.stop();
            this.textRule = "Tape les taupes qui n'ont pas de carrote";
            this.reset();
        });

        difficulty2.setCycleCount(3);
    }

    public void updateDifficulty3(){
        difficulty3 = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            this.limitMoleEntity ++;
            this.limitObjEntity ++;
        }));

        difficulty3.setOnFinished(event -> {
            difficulty3.stop();
            this.dispose();
        });

        difficulty3.setCycleCount(3);
    }

    void adjustBackground(Rectangle image) {
        int backgroundStyleCoef = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Integer>() {
            @Override
            public Integer visitLight() {
                return 2;
            }

            @Override
            public Integer visitDark() {
                return 0;
            }
        });

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(backgroundStyleCoef * 0.25); //0.5 or 0
        image.setEffect(colorAdjust);
    }

    /* Moles get out randomly */
    private synchronized void play() {
        minuteur = new Timer();
        TimerTask tache = new TimerTask() {
            public void run() {
                if (nbMolesOut.get() < limitMoleEntity) {
                    chooseMoleToOut();
                }
                if (nbObjOut.get() < limitObjEntity){
                    chooseObjToOut();
                }
            }
        };

        minuteur.schedule(tache, 0, 500);

        if (this.difficulty == 1){
            nbMolesOut = new AtomicInteger(0);
            nbObjOut = new AtomicInteger(0);
            this.limitMoleEntity = 0;
            this.limitObjEntity = 2;
            this.nbMolesWhacked = 0;
            difficulty1.playFromStart();
        }else if (this.difficulty == 2){
            nbMolesOut = new AtomicInteger(0);
            nbObjOut = new AtomicInteger(0);
            this.limitMoleEntity = 1;
            this.limitObjEntity = 1;
            this.nbMolesWhacked = 0;
            difficulty2.playFromStart();
        }else {
            nbMolesOut = new AtomicInteger(0);
            nbObjOut = new AtomicInteger(0);
            this.limitMoleEntity = 1;
            this.limitObjEntity = 1;
            this.nbMolesWhacked = 0;
            difficulty3.playFromStart();
        }
    }

    @Override
    public void dispose() {
        stats.setTargetAOIList(targetAOIList);
        if (currentRoundDetails != null) {
            if (currentRoundDetails.molesList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.molesList);
                currentRoundDetails.molesList.clear();
            }
            if (currentRoundDetails.molesObjList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.molesObjList);
                currentRoundDetails.molesObjList.clear();
            }
            currentRoundDetails = null;
        }

        this.gameContext.getChildren().clear();
        this.createExcelFile();
        this.gameContext.showRoundStats(stats, this);
    }

    public void reset(){
        this.gameContext.getChildren().clear();

        stats.setTargetAOIList(targetAOIList);
        gameContext.getChildren().removeAll(currentRoundDetails.molesList);
        currentRoundDetails.molesList.clear();
        gameContext.getChildren().removeAll(currentRoundDetails.molesObjList);
        currentRoundDetails.molesObjList.clear();
        currentRoundDetails = null;

        nbMolesOut = new AtomicInteger(0);
        nbObjOut = new AtomicInteger(0);
        this.limitMoleEntity = 0;
        this.limitObjEntity = 0;
        this.nbMolesWhacked = 0;
        this.difficulty++;

        this.launch();
    }

    /* Select a mole not out for the moment and call "getOut()" */
    private void chooseMoleToOut() {
        if (this.currentRoundDetails == null) {
            return;
        }
        int indice;
        int nbHoles = 10;

        LinkedList<Integer> availableHoles = new LinkedList<>();
        for (int i = 0; i < nbHoles; i++) {
            if (currentRoundDetails.molesList.get(i).canGoOut && currentRoundDetails.molesObjList.get(i).canGoOut) {
                availableHoles.add(i);
            }
        }
        indice = availableHoles.get(randomGenerator.nextInt(availableHoles.size()));

        MolesChar m = currentRoundDetails.molesList.get(indice);
        final TargetAOI targetAOI = new TargetAOI(m.getPositionX(), m.getPositionY(), (int) moleRadius / 3,
            System.currentTimeMillis());
        targetAOIList.add(targetAOI);
        m.setTargetAOIListIndex(targetAOIList.size() - 1);
        m.getOut(randomGenerator);
        stats.incrementNumberOfGoalsToReach();
    }

    private void chooseObjToOut(){
        if (this.currentRoundDetails == null) {
            return;
        }
        int indice;
        int nbHoles = 10;

        LinkedList<Integer> availableHoles = new LinkedList<>();
        for (int i = 0; i < nbHoles; i++) {
            if (currentRoundDetails.molesList.get(i).canGoOut && currentRoundDetails.molesObjList.get(i).canGoOut) {
                availableHoles.add(i);
            }
        }
        indice = availableHoles.get(randomGenerator.nextInt(availableHoles.size()));

        MolesObj o = currentRoundDetails.molesObjList.get(indice);
        final TargetAOI targetAOI = new TargetAOI(o.getPositionX(), o.getPositionY(), (int) moleRadius / 3,
            System.currentTimeMillis());
        targetAOIList.add(targetAOI);
        o.setTargetAOIListIndex(targetAOIList.size() - 1);
        o.getOut(randomGenerator);
        stats.incrementNumberOfGoalsToReach();
    }

    private double[][] creationTableauPlacement(double width, double height, double distTrans) {
        double[][] tabPlacement = new double[10][2];

        tabPlacement[0][0] = 0.05 * width;
        tabPlacement[0][1] = 0.190 * height + distTrans;
        tabPlacement[1][0] = 0.382 * width;
        tabPlacement[1][1] = 0.185 * height + distTrans;
        tabPlacement[2][0] = 0.75 * width;
        tabPlacement[2][1] = 0.097 * height + distTrans;
        tabPlacement[3][0] = 0.22 * width;
        tabPlacement[3][1] = 0.345 * height + distTrans;
        tabPlacement[4][0] = 0.62 * width;
        tabPlacement[4][1] = 0.29 * height + distTrans;
        tabPlacement[5][0] = 0.468 * width;
        tabPlacement[5][1] = 0.465 * height + distTrans;
        tabPlacement[6][0] = 0.837 * width;
        tabPlacement[6][1] = 0.42 * height + distTrans;
        tabPlacement[7][0] = 0.059 * width;
        tabPlacement[7][1] = 0.531 * height + distTrans;
        tabPlacement[8][0] = 0.28 * width;
        tabPlacement[8][1] = 0.63 * height + distTrans;
        tabPlacement[9][0] = 0.67 * width;
        tabPlacement[9][1] = 0.59 * height + distTrans;

        return tabPlacement;
    }

    private List<MolesChar> initMoles(MolesGameVariant variant, ReplayablePseudoRandom randomGenerator) {
        javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ArrayList<MolesChar> result = new ArrayList<>();

        double moleHeight = computeMoleHeight(gameDimension2D);

        double moleWidth = computeMoleWidth(gameDimension2D);
        this.moleRadius = moleWidth;
        double height = gameDimension2D.getHeight();
        double width = gameDimension2D.getWidth();
        double distTrans = computeDistTransMole(gameDimension2D);

        double[][] place = creationTableauPlacement(width, height, distTrans);

        /* Creation and placement of moles in the field */
        for (double[] doubles : place) {
            result.add(new MolesChar(doubles[0], doubles[1], moleWidth, moleHeight, distTrans, gameContext,
                this, variant, randomGenerator));
        }

        return result;
    }

    private List<MolesObj> initObj(MolesGameVariant variant, ReplayablePseudoRandom randomGenerator) {
        javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ArrayList<MolesObj> result = new ArrayList<>();

        double moleHeight = computeMoleHeight(gameDimension2D);

        double moleWidth = computeMoleWidth(gameDimension2D);
        this.moleRadius = moleWidth;
        double height = gameDimension2D.getHeight();
        double width = gameDimension2D.getWidth();
        double distTrans = computeDistTransMole(gameDimension2D);

        double[][] place = creationTableauPlacement(width, height, distTrans);

        /* Creation and placement of moles in the field */
        for (double[] doubles : place) {
            result.add(new MolesObj(doubles[0], doubles[1], moleWidth, moleHeight, distTrans, gameContext,
                this, variant, randomGenerator));
        }

        return result;
    }

    private static double computeDistTransMole(Dimension2D gameDimension2D) {
        return gameDimension2D.getHeight() * 0.16;
    }

    private static double computeMoleHeight(Dimension2D gameDimension2D) {
        return gameDimension2D.getHeight() * 0.14;
    }

    private static double computeMoleWidth(Dimension2D gameDimension2D) {
        return gameDimension2D.getWidth() * 0.13;
    }

    void oneMoleWhacked() {

        nbMolesWhacked++;
        String s = "Score:" + nbMolesWhacked;
        stats.incrementNumberOfGoalsReached();
        EventHandler<ActionEvent> limiterEndEventHandler = e -> {
            minuteur.cancel();
            minuteur.purge();
        };
        gameContext.updateScore(stats, this, limiterEndEventHandler, limiterEndEventHandler);
        lab.setText(s);

    }

    public void createExcelFile(){
        String pathStats = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";
        this.stats.actualFile = this.pathStatsGame  + "/Stats_" + DateUtils.today() + ".xlsx";

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet(this.gameName);

        Object[][] bookData = new Object[this.computerTimestamp.size()+1][14];

        bookData[0][0] = "Recording timestamp";
        bookData[0][1] = "Computer timestamp";
        bookData[0][2] = "Recording start time";
        bookData[0][3] = "Recording duration";
        bookData[0][4] = "Step";
        bookData[0][5] = "Event Image";
        bookData[0][6] = "Fixation Length Image";
        bookData[0][7] = "Score";

        for (int i=0; i<this.computerTimestamp.size(); i++){
            bookData[i+1][0] = String.valueOf(this.computerTimestamp.get(i) - this.computerTimestamp.get(0));
            bookData[i+1][1] = String.valueOf(this.computerTimestamp.get(i));
            bookData[i+1][2] = String.valueOf(this.startTime);
            bookData[i+1][3] = String.valueOf(this.endTime);
            bookData[i+1][4] = String.valueOf(this.step.get(i));
            bookData[i+1][5] = String.valueOf(eventImage.get(i));
            bookData[i+1][6] = String.valueOf(fixationLengthImage.get(i));
            bookData[i+1][7] = String.valueOf(this.score.get(i));
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
