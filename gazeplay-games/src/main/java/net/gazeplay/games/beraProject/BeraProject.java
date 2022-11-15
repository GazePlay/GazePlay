package net.gazeplay.games.beraProject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class BeraProject implements GameLifeCycle {

    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;
    private final IGameContext gameContext;
    private final boolean fourThree;
    private final Stats stats;
    private final ArrayList<TargetAOI> targetAOIList;
    private final ReplayablePseudoRandom randomGenerator;
    private String imagesDirectoryPath = "";
    private static final String BIP_SOUND = "data/common/sounds/bip.wav";
    private static final String SEE_TWO_IMAGES_SOUND = "data/common/sounds/seeTwoImages.wav";
    private String gameName = "GazePlayEval2";
    private String IMAGE_SOUND = "";
    private String[][] listImages;
    private String[][] listOrder;
    private String[][] listValues;
    private String[] listSounds;
    private final int nbLines = 1;
    private final int nbColumns = 2;
    private int nbImages = 0;
    public int indexFileImage = 0;
    public int indexEndGame = 0;
    private boolean canRemoveItemManually = true;
    private RoundDetails currentRoundDetails;
    private Long currentRoundStartTime;
    private final Timeline timelineTransition = waitForTransition();
    private final Timeline timelineQuestion = waitForQuestion();
    private final Timeline timelineInput = waitForInput();
    public ImageView whiteCrossPicture;
    private Text questionText;
    public CustomInputEventHandlerKeyboard customInputEventHandlerKeyboard = new CustomInputEventHandlerKeyboard();
    public boolean reEntered = false;
    public boolean goNext = false;
    public int nbCountError = 0;
    public int nbCountErrorSave = 0;
    private int totalItemsAddedManually = 0;

    public BeraProject(final boolean fourThree, final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
        this.imagesDirectoryPath = System.getProperties().getProperty("user.home") + "/GazePlay/files/game/images";

        this.loadGame();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);
    }

    public BeraProject(final boolean fourThree, final IGameContext gameContext, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
        this.imagesDirectoryPath = System.getProperties().getProperty("user.home") + "/GazePlay/files/game/images";

        this.loadGame();
        this.gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, customInputEventHandlerKeyboard);

    }

    public void loadGame(){
        File gameDirectory = new File(GazePlayDirectories.getDefaultFileDirectoryDefaultValue(), "game/game.json");
        JsonParser jsonParser = new JsonParser();
        try  (FileReader reader = new FileReader(gameDirectory)) {
            JsonObject obj = jsonParser.parse(reader).getAsJsonObject();
            this.gameName = obj.get("GameName").getAsString();
            JsonArray listImages = obj.get("GameImages").getAsJsonArray();
            JsonArray listOrder = obj.get("ImagesOrder").getAsJsonArray();
            JsonArray listValues = obj.get("ImagesValue").getAsJsonArray();
            JsonArray listSounds = obj.get("GameSounds").getAsJsonArray().get(0).getAsJsonArray();
            this.nbImages = obj.get("NbImages").getAsInt();
            this.generateTabFromJson(listImages, listOrder, listValues, listSounds);
            this.setSound();
            this.indexEndGame = this.nbImages / 2;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateTabFromJson(JsonArray images, JsonArray order, JsonArray values, JsonArray sounds){
        this.listImages = new String[images.size()][2];
        for (int i=0; i<images.size(); i++){
            String[] tmpImages = new String[2];
            tmpImages[0] = images.get(i).getAsJsonArray().get(0).getAsString();
            tmpImages[1] = images.get(i).getAsJsonArray().get(1).getAsString();
            this.listImages[i] = tmpImages;
        }

        this.listOrder = new String[order.size()][2];
        for (int i=0; i<order.size(); i++){
            String[] tmpOrder = new String[2];
            tmpOrder[0] = order.get(i).getAsJsonArray().get(0).getAsString();
            tmpOrder[1] = order.get(i).getAsJsonArray().get(1).getAsString();
            this.listOrder[i] = tmpOrder;
        }

        this.listValues = new String[values.size()][2];
        for (int i=0; i<values.size(); i++){
            String[] tmpValues = new String[2];
            tmpValues[0] = values.get(i).getAsJsonArray().get(0).getAsString();
            tmpValues[1] = values.get(i).getAsJsonArray().get(1).getAsString();
            this.listValues[i] = tmpValues;
        }

        this.listSounds = new String[sounds.size()];
        for (int i=0; i<sounds.size(); i++){
            this.listSounds[i] = sounds.get(i).getAsString();
        }
    }

    public void setSound(){
        if (this.indexFileImage < this.indexEndGame){
            final String directorySounds = GazePlayDirectories.getDefaultFileDirectoryDefaultValue() + "/game/sounds/";
            this.IMAGE_SOUND = directorySounds + this.listSounds[this.indexFileImage];
        }
    }

    public void playSound(String soundPath){
        Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isSoundEnabled()){
            gameContext.getSoundManager().add(soundPath);
        }
    }

    @Override
    public void launch() {
        this.startTimer();

        this.canRemoveItemManually = true;

        gameContext.setLimiterAvailable();

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        final int winnerImageIndexAmongDisplayedImages = 0;
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        currentRoundDetails = pickAndBuildRandomPictures(numberOfImagesToDisplayPerRound, winnerImageIndexAmongDisplayedImages);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        this.startGame();
    }

    public void startGame() {
        final List<Rectangle> pictogramesList = new ArrayList<>(20); // storage of actual Pictogramm nodes in order to delete

        final List<Image> listOfPictos = currentRoundDetails.getPictos();

        if (listOfPictos != null && !listOfPictos.isEmpty() && listOfPictos.size() <= NBMAXPICTO) {

            final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();
            final double screenWidth = screenDimension.getWidth();

            final double nbPicto = listOfPictos.size();

            double pictoSize = screenWidth / (nbPicto + 1);

            log.debug("screenWidth/(nbPicto) : {}", pictoSize);

            pictoSize = Math.min(pictoSize, MAXSIZEPICTO);

            log.debug("Picto Size: {}", pictoSize);

            int i = 0;
            final double shift = screenWidth / 2 - ((nbPicto / 2) * pictoSize * 1.1);

            log.debug("shift Size: {}", shift);

            final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            final double positionY = gamePaneDimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

            for (final Image picto : listOfPictos) {

                final Rectangle pictoRectangle = new Rectangle(pictoSize, pictoSize);
                pictoRectangle.setFill(new ImagePattern(picto));
                pictoRectangle.setY(positionY + 100);
                pictoRectangle.setX(shift + (i++ * pictoSize * 1.1));
                pictogramesList.add(pictoRectangle);
            }

            gameContext.getChildren().addAll(pictogramesList);
        }

        gameContext.getChildren().removeAll(pictogramesList);

        gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            //  log.debug("p = {}", p);
            p.toFront();
            p.setOpacity(1);
        }

        stats.notifyNewRoundReady();

        gameContext.onGameStarted(2000);

        customInputEventHandlerKeyboard.ignoreAnyInput = false;

        this.playSound(SEE_TWO_IMAGES_SOUND);
    }

    public void checkAllPictureCardChecked() {
        boolean check = true;
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            if (!p.isAlreadySee()) {
                check = false;
                break;
            }
        }
        if (check) {
            this.timelineTransition.playFromStart();
        }
    }

    public Timeline waitForInput(){
        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("INPUT TIME : {}", config.getDelayBeforeSelectionTime());

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getDelayBeforeSelectionTime())));
        transition.setOnFinished(event -> {
            for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
                p.setVisibleProgressIndicator();
            }
        });
        return transition;
    }

    public Timeline waitForTransition(){

        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("TRANSITION TIME : {}", config.getTransitionTime());

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getTransitionTime())));
        transition.setOnFinished(event -> {
            returnOnPictureCards();
        });
        return transition;
    }

    public void returnOnPictureCards(){

        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("QUESTION TIME ENABLED : {}", config.isQuestionTimeEnabled());

        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            p.setVisibleProgressIndicator();
            p.setVisibleImagePicture(false);
            p.setNotifImageRectangle(false);
            this.reEntered = true;
        }
        this.createWhiteCross();

        if (config.isQuestionTimeEnabled()){
            this.timelineQuestion.playFromStart();
            this.playSound(IMAGE_SOUND);
        }else {
            this.playSound(IMAGE_SOUND);
        }
    }

    public Timeline waitForQuestion(){

        Configuration config = ActiveConfigurationContext.getInstance();

        log.info("QUESTION TIME : {}", config.getQuestionTime());

        Timeline question = new Timeline();
        question.getKeyFrames().add(new KeyFrame(new Duration(config.getQuestionTime())));
        question.setOnFinished(event -> {
            this.choicePicturePair();
        });
        return question;
    }

    public void nextRound(){

        Configuration config = ActiveConfigurationContext.getInstance();

        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(config.getTransitionTime())));
        transition.setOnFinished(event -> {
            this.launch();
        });
    }
    RoundDetails pickAndBuildRandomPictures(final int numberOfImagesToDisplayPerRound, final int winnerImageIndexAmongDisplayedImages) {

        final Configuration config = gameContext.getConfiguration();
        Configuration configActive = ActiveConfigurationContext.getInstance();

        int posX = 0;
        int posY = 0;

        boolean winnerP1;
        boolean winnerP2;

        String imageP1 = "";
        String imageP2 = "";

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        final List<PictureCard> pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

        String imageTemp1 = this.listImages[this.indexFileImage][0];
        String imageTemp2 = this.listImages[this.indexFileImage][1];

        if (Objects.equals(this.listOrder[this.indexFileImage][0], "First")) {
            imageP1 = imageTemp1;
            imageP2 = imageTemp2;
        } else {
            imageP1 = imageTemp2;
            imageP2 = imageTemp1;
        }

        if (Objects.equals(this.listValues[this.indexFileImage][0], "True")) {
            winnerP1 = true;
            winnerP2 = false;
        } else {
            winnerP1 = false;
            winnerP2 = true;
        }

        double gap = 0;
        double widthImg = 0;
        double heightImg = 0;
        double posYImage = gameSizing.height * posY;

        if (configActive.isColumnarImagesEnabled()){
            gap = gameSizing.shift + 750;
            widthImg = (gameSizing.width - 50) / 2.0;
            heightImg = gameSizing.height / 2.0;
            posYImage = gameSizing.height * posY + 50;
        }else {
            gap = gameSizing.shift + 25;
            widthImg = gameSizing.width - 50;
            heightImg = gameSizing.height;
        }

        final PictureCard pictureCard1 = new PictureCard(
            gameSizing.width * posX + gap,
            posYImage, widthImg, heightImg, gameContext,
            winnerP1, imageP1 + "", stats, this);

        pictureCardList.add(pictureCard1);

        final TargetAOI targetAOI1 = new TargetAOI(
            gameSizing.width * (posX + 0.25),
            gameSizing.height * (posY + 1),
            (int) gameSizing.height,
            System.currentTimeMillis());

        targetAOIList.add(targetAOI1);

        if (configActive.isColumnarImagesEnabled()){
            posYImage = gameSizing.height / 1.5 - 100;
        }else {
            posX++;
        }

        final PictureCard pictureCard2 = new PictureCard(
            gameSizing.width * posX + gap,
            posYImage, widthImg, heightImg, gameContext,
            winnerP2, imageP2 + "", stats, this);

        pictureCardList.add(pictureCard2);

        final TargetAOI targetAOI2 = new TargetAOI(
            gameSizing.width * (posX + 0.25),
            gameSizing.height * (posY + 1),
            (int) gameSizing.height,
            System.currentTimeMillis());

        targetAOIList.add(targetAOI2);

        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question,
            pictograms);
    }

    private void error(final String language) {

        gameContext.clear();
        // HomeUtils.home(scene, group, choiceBox, null);

        final Multilinguism multilinguism = MultilinguismFactory.getSingleton();

        final Text error = new Text(multilinguism.getTranslation("WII-error", language));
        final Region root = gameContext.getRoot();
        error.setX(root.getWidth() / 2. -100);
        error.setY(root.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
    }

    public void createWhiteCross(){

        final Image whiteSquare = new Image("data/common/images/whiteCross.png");
        this.whiteCrossPicture = new ImageView(whiteSquare);

        final Region root = gameContext.getRoot();

        this.whiteCrossPicture.setX((root.getWidth() / 2) - (whiteSquare.getWidth() / 2));
        this.whiteCrossPicture.setY((root.getHeight() / 2) - (whiteSquare.getHeight() / 2));
        this.whiteCrossPicture.setId("item");
        this.whiteCrossPicture.setOpacity(1);
        this.whiteCrossPicture.setVisible(true);

        gameContext.getChildren().addAll(this.whiteCrossPicture);

        this.goNext = true;
    }

    public void startTimer(){
        if (this.indexFileImage == 0){
            currentRoundStartTime = System.currentTimeMillis();
        }
    }

    public void increaseIndexFileImage(boolean correctAnswer) {
        //this.calculateStats(this.indexFileImage, correctAnswer);
        this.indexFileImage = this.indexFileImage + 1;
        this.setSound();
    }

    private void next(boolean value) {
        if (value) {
            this.nbCountErrorSave = this.nbCountError;
            this.totalItemsAddedManually += 1;
            currentRoundDetails.getPictureCardList().get(0).onCorrectCardSelected();
        } else {
            currentRoundDetails.getPictureCardList().get(0).onWrongCardSelected();
        }
    }

    private void removeItemAddedManually() {
        if (this.totalItemsAddedManually != 0 && this.canRemoveItemManually) {
            this.nbCountError = this.nbCountErrorSave + 1;
            this.totalItemsAddedManually -= 1;
            this.canRemoveItemManually = false;
        }
    }

    public void removeEventHandlerPictureCard(){
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            p.removeEventHandler();
        }
    }

    public void choicePicturePair(){
        this.whiteCrossPicture.setVisible(false);
        this.playSound(BIP_SOUND);
        for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
            p.hideProgressIndicator();
            p.setVisibleImagePicture(true);
            timelineInput.playFromStart();
        }
    }

    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.getPictureCardList() != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.getPictureCardList());
            }
            currentRoundDetails = null;
        }
        stats.setTargetAOIList(targetAOIList);
    }

    public void resetFromReplay(){
        this.totalItemsAddedManually = 0;
        this.indexFileImage = 0;
        this.nbCountError = 0;
        this.nbCountErrorSave = 0;
    }

    public void finalStats() {

        stats.timeGame = System.currentTimeMillis() - this.currentRoundStartTime;
        stats.totalItemsAddedManually = this.totalItemsAddedManually;
        stats.total = this.totalItemsAddedManually;

        createFile();
        createExcel();
    }

    public void createFile(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = pathDirectory + "\\" + this.gameName + "-" + DateUtils.dateTimeNow() + ".csv";
        File statsFile = new File(pathDirectory, pathFile);

        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        try {
            PrintWriter out = new PrintWriter(statsFile, StandardCharsets.UTF_16);
            out.append("\r\n");
            out.append("Fait le ").append(formatDate.format(now)).append("\r\n");
            out.append("\r\n");
            out.append("Temps de jeu : ").append(String.valueOf(stats.timeGame / 100.)).append(" secondes \r\n");
            out.append("\r\n");
            out.append(" - Total items ajoutés manuellement : ").append(String.valueOf(this.totalItemsAddedManually)).append("/20 \r\n");
            out.close();
        } catch (Exception e) {
            log.info("Error creation csv for GazePlayEval2 stats game !");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("PMD")
    public void createExcel(){

        File pathDirectory = stats.getGameStatsOfTheDayDirectory();
        String pathFile = pathDirectory + "\\" + this.gameName + "-" + DateUtils.dateTimeNow() + ".xlsx";
        this.stats.actualFile = pathFile;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Statistiques");

        Object[][] bookData = {
            {"Temps de jeu : ", String.valueOf(stats.timeGame / 100.), "secondes"},
            {""},
            {" - Total items ajoutés manuellement : ", String.valueOf(this.totalItemsAddedManually), "/20"},
        };

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

        try (FileOutputStream outputStream = new FileOutputStream(pathFile)) {
            workbook.write(outputStream);
        } catch (Exception e){
            log.info("Creation of xlsx file don't work", e);
        }
    }

    private class CustomInputEventHandlerKeyboard implements EventHandler<KeyEvent> {

        public boolean ignoreAnyInput = false;

        @Override
        public void handle(KeyEvent key) {

            if (key.getCode().isArrowKey() && goNext){
                timelineQuestion.stop();
                goNext = false;
                choicePicturePair();
            }

            if (ignoreAnyInput) {
                return;
            }

            if (key.getCode().getChar().equals("X")) {
                ignoreAnyInput = true;
                timelineTransition.stop();
                timelineQuestion.stop();
                next(true);
            } else if (key.getCode().getChar().equals("C")) {
                ignoreAnyInput = true;
                timelineTransition.stop();
                timelineQuestion.stop();
                next(false);
            } else if (key.getCode().getChar().equals("V")) {
                timelineTransition.stop();
                timelineQuestion.stop();
                removeItemAddedManually();
            }
        }
    }
}
