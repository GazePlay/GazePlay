package net.gazeplay.games.memory;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class Memory implements GameLifeCycle {

    private static final float cardRatio = 0.75f;

    private static int minHeight = 30;

    public enum MemoryGameType {

        LETTERS, NUMBERS, DEFAULT
    }

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        public final List<MemoryCard> cardList;
    }

    @Getter
    private int nbRemainingPeers;

    private final IGameContext gameContext;

    @Getter
    @Setter
    private int nbLines;
    @Getter
    @Setter
    private int nbColumns;
    int iteration = 1;
    private String gameName = "Memory";
    String pathStatsGame;
    private String difficulty;
    List<Image> listImgCards;
    String[] allCards;

    private final Stats stats;

    private ImageLibrary imageLibrary;

    /*
     * HashMap of images selected for this game and their associated id The id is the same for the 2 same images
     */
    public HashMap<Integer, Image> images;

    public RoundDetails currentRoundDetails;

    @Getter
    public int nbTurnedCards;

    private final boolean isOpen;

    private final ReplayablePseudoRandom randomGenerator;

    @Getter
    private int nbWrongCards;

    @Getter
    private int nbCorrectCards;

    @Getter
    private List<Integer> listOfResults = new ArrayList<>();

    @Getter
    @Setter
    private int level = 2;
    SimpleDateFormat sdf;
    ArrayList<Long> computerTimestamp = new ArrayList<>();
    ArrayList<Integer> step = new ArrayList<>();
    String startTime;
    String endTime;
    public Timestamp timestamp;
    Boolean firstTime = true;

    ArrayList<String> eventImage = new ArrayList<>();
    ArrayList<String> fixationLengthImage = new ArrayList<>();


    public Memory(final MemoryGameType gameType, final IGameContext gameContext, final int nbLines, final int nbColumns, final String difficulty, final Stats stats,
                  final boolean isOpen) {
        super();
        this.isOpen = isOpen;
        final int cardsCount = nbLines * nbColumns;
        if ((cardsCount & 1) != 0) {
            // nbLines * nbColumns must be a multiple of 2
            throw new IllegalArgumentException("Cards count must be an even number in this game");
        }
        this.nbRemainingPeers = (nbLines * nbColumns) / 2;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.difficulty = difficulty;
        this.stats = stats;
        this.nbCorrectCards = 0;
        this.nbWrongCards = 0;
        this.gameContext.startTimeLimiter();
        this.generateStatsFolder();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        this.imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("magiccards"), Utils.getImagesSubdirectory("default"), randomGenerator);
        this.getAllCards();
    }

    public Memory(final MemoryGameType gameType, final IGameContext gameContext, final int nbLines, final int nbColumns, final String difficulty, final Stats stats,
                  final boolean isOpen, double gameSeed) {
        super();
        this.isOpen = isOpen;
        final int cardsCount = nbLines * nbColumns;
        if ((cardsCount & 1) != 0) {
            // nbLines * nbColumns must be a multiple of 2
            throw new IllegalArgumentException("Cards count must be an even number in this game");
        }
        this.nbRemainingPeers = (nbLines * nbColumns) / 2;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.difficulty = difficulty;
        this.stats = stats;
        this.nbCorrectCards = 0;
        this.nbWrongCards = 0;
        this.gameContext.startTimeLimiter();
        this.generateStatsFolder();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("magiccards"), Utils.getImagesSubdirectory("default"), randomGenerator);
        this.getAllCards();

        gameContext.start();

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
            this.step.add(this.iteration-1);
            eventImage.add("");
            fixationLengthImage.add("");
        }
    }

    public void onWrong(){
        this.computerTimestamp.add(new Timestamp(System.currentTimeMillis()).getTime());
        this.step.add(this.iteration);
        eventImage.add("Wrong pair");
        fixationLengthImage.add("");
    }

    public void onCorrect(){
        this.computerTimestamp.add(new Timestamp(System.currentTimeMillis()).getTime());
        this.step.add(this.iteration);
        eventImage.add("Correct pair");
        fixationLengthImage.add("");
    }

    public void onSelectedImg(String event, String value){
        this.computerTimestamp.add(new Timestamp(System.currentTimeMillis()).getTime());
        this.step.add(this.iteration);
        eventImage.add(event);
        fixationLengthImage.add(value);
    }

    public void getAllCards(){
        this.allCards = new String[]{
            "bearCard.png",
            "crabCard.png",
            "dogCard.png",
            "elephantCard.png",
            "foxCard.png",
            "giraffeCard.png",
            "hedgehogCard.png",
            "hippoCard.png",
            "jellyfishCard.png",
            "lionCard.png",
            "octopusCard.png",
            "owlCard.png",
            "rabbitCard.png",
            "snakeCard.png",
            "squirrelCard.png"
        };
    }

    public void updateDifficulty(){
        if (this.iteration % 2 == 0){
            this.nbLines = 2;
            this.nbColumns += 1;
            this.nbRemainingPeers = (nbLines * nbColumns) / 2;
        }
        this.iteration++;
    }

    HashMap<Integer, Image> pickRandomImages() {
        final HashMap<Integer, Image> res = new HashMap<>();

        this.generateListCards();

        int i = 0;
        for (final Image image : this.listImgCards) {
            res.put(i, image);
            i++;
        }
        return res;
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Configuration config = gameContext.getConfiguration();
        final int cardsCount = nbColumns * nbLines;

        images = pickRandomImages();

        final List<MemoryCard> cardList = createCards(images, config);

        nbRemainingPeers = cardsCount / 2;

        currentRoundDetails = new RoundDetails(cardList);

        gameContext.getChildren().addAll(cardList);

        stats.notifyNewRoundReady();

        gameContext.getGazeDeviceManager().addStats(stats);

        gameContext.onGameStarted(3000);
        this.firstStat();
    }

    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.cardList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.cardList);
            }
            currentRoundDetails = null;
        }

    }

    public void endGame(){
        this.gameContext.getChildren().clear();
        this.createExcelFile();
        this.gameContext.showRoundStats(stats, this);
    }

    public void removeSelectedCards() {
        if (this.currentRoundDetails == null) {
            return;
        }
        final List<MemoryCard> cardsToHide = new ArrayList<>();
        for (final MemoryCard pictureCard : this.currentRoundDetails.cardList) {
            if (pictureCard.isTurned()) {
                cardsToHide.add(pictureCard);
            }
        }
        nbRemainingPeers = nbRemainingPeers - 1;
        // remove all turned cards
        gameContext.getChildren().removeAll(cardsToHide);
    }

    public void generateListCards(){
        this.listImgCards = new ArrayList<>();
        int iteration = (this.nbLines * nbColumns) / 2;
        Random random = new Random();
        int index;
        boolean cardAdded;

        for (int i=0; i<iteration; i++){
            cardAdded = false;
            do {
                index = random.nextInt(this.allCards.length);
                String nameCard = this.allCards[index];
                Image getCard = new Image("data/magiccards/cards/" + this.allCards[index]);
                if (!this.listImgCards.contains(getCard)){
                    this.listImgCards.add(new Image("data/magiccards/cards/" + nameCard));
                    cardAdded = true;
                }
            } while (!cardAdded);
        }
    }

    private List<MemoryCard> createCards(final HashMap<Integer, Image> im, final Configuration config) {
        final javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        log.debug("Width {} ; height {}", gameDimension2D.getWidth(), gameDimension2D.getHeight());

        final double cardHeight = computeCardHeight(gameDimension2D, nbLines);
        final double cardWidth = cardHeight * cardRatio;

        log.debug("cardWidth {} ; cardHeight {}", cardWidth, cardHeight);

        final double width = computeCardWidth(gameDimension2D, nbColumns) - cardWidth;

        log.debug("width {} ", width);

        final List<MemoryCard> result = new ArrayList<>();

        // HashMap <index, number of times the index was used >
        final HashMap<Integer, Integer> indUsed = new HashMap<>();
        indUsed.clear();

        final int fixationlength = config.getFixationLength();

        if (getNbLines() > 2)
            minHeight = 10;

        for (int currentLineIndex = 0; currentLineIndex < nbLines; currentLineIndex++) {
            for (int currentColumnIndex = 0; currentColumnIndex < nbColumns; currentColumnIndex++) {

                final double positionX = width / 2d + (width + cardWidth) * currentColumnIndex;
                final double positionY = minHeight / 2d + (minHeight + cardHeight) * currentLineIndex;

                log.debug("positionX : {} ; positionY : {}", positionX, positionY);

                final int id = getRandomValue(indUsed);

                if (indUsed.containsKey(id)) {
                    indUsed.replace(id, 1, 2);
                } else {
                    indUsed.put(id, 1);
                }

                final Image image = images.get(id);

                final MemoryCard card = new MemoryCard(positionX, positionY, cardWidth, cardHeight, image, id, gameContext,
                    stats, this, fixationlength, isOpen);

                result.add(card);
            }
        }
        return result;
    }

    private static double computeCardHeight(final Dimension2D gameDimension2D, final int nbLines) {
        return gameDimension2D.getHeight() * 0.9 / nbLines;
    }

    private static double computeCardWidth(final Dimension2D gameDimension2D, final int nbColumns) {
        return gameDimension2D.getWidth() / nbColumns;
    }

    private int getRandomValue(final HashMap<Integer, Integer> indUsed) {
        int value;
        do {
            value = randomGenerator.nextInt(images.size());
        } while ((!images.containsKey(value)) || (indUsed.containsKey(value) && (indUsed.get(value) == 2)));
        // While selected image is already used 2 times (if it appears )
        return value;
    }

    public void incNbWrongCards() {
        nbWrongCards++;
    }

    public void resetNbWrongCards() {
        nbWrongCards = 0;
    }

    public void incNbCorrectCards() {
        nbCorrectCards++;
    }

    public void resetNbCorrectCards() {
        nbCorrectCards = 0;
    }

    public int totalNbOfTries() {
        return getNbCorrectCards() + getNbWrongCards();
    }

    public void addRoundResult(int lastRoundResult) {
        listOfResults.add(lastRoundResult);
    }

    public void adaptLevel() {

        if (level == 6) {
            setNbColumns(4);
            setNbLines(3);
        } else if (level == 8) {
            setNbColumns(4);
            setNbLines(4);
        } else if (level == 9) {
            setNbColumns(6);
            setNbLines(3);
        } else if (level == 10) {
            setNbColumns(5);
            setNbLines(4);
        } else {
            setNbColumns(level);
            setNbLines(2);
        }

    }

    public String getDifficulty() {
        return difficulty;
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

        for (int i=0; i<this.computerTimestamp.size(); i++){
            bookData[i+1][0] = String.valueOf(this.computerTimestamp.get(i) - this.computerTimestamp.get(0));
            bookData[i+1][1] = String.valueOf(this.computerTimestamp.get(i));
            bookData[i+1][2] = String.valueOf(this.startTime);
            bookData[i+1][3] = String.valueOf(this.endTime);
            bookData[i+1][4] = String.valueOf(this.step.get(i));
            bookData[i+1][5] = String.valueOf(eventImage.get(i));
            bookData[i+1][6] = String.valueOf(fixationLengthImage.get(i));
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
