package net.gazeplay.games.whereisit;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.difficulty.SourceSet;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.games.WhereIsItVaildator;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.games.frog.Nenuphar;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.gazeplay.games.whereisit.WhereIsItGameType.*;

/**
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt implements GameLifeCycle {
    private static final int NBMAXPICTO = 10;
    private static final double MAXSIZEPICTO = 250;

    private Text questionText;

    @Getter
    private final WhereIsItGameType gameType;

    private final int nbLines;
    private final int nbColumns;
    private final boolean fourThree;

    private int level = 1;
    private int lvlReplays = 1;
    int iteration = 1;
    private int rightDecision = 0;
    private int wrongDecision = 0;
    private boolean firstWrong = false;

    private final IGameContext gameContext;
    private final Stats stats;
    private RoundDetails currentRoundDetails;
    private final ReplayablePseudoRandom randomGenerator;

    private final ArrayList<TargetAOI> targetAOIList;
    int winnerImageIndexAmongDisplayedImages;
    List<PictureCard> pictureCardList;
    private String gameName = "Animals";
    String pathStatsGame;
    SimpleDateFormat sdf;
    ArrayList<Long> computerTimestamp = new ArrayList<>();
    ArrayList<Integer> step = new ArrayList<>();
    ArrayList<Integer> goodAnswer = new ArrayList<>();
    String startTime;
    String endTime;
    public Timestamp timestamp;
    Boolean firstTime = true;

    ArrayList<String> eventImage1 = new ArrayList<>();
    ArrayList<String> fixationLengthImage1 = new ArrayList<>();
    ArrayList<String> eventImage2 = new ArrayList<>();
    ArrayList<String> fixationLengthImage2 = new ArrayList<>();
    ArrayList<String> eventImage3 = new ArrayList<>();
    ArrayList<String> fixationLengthImage3 = new ArrayList<>();
    ArrayList<String> eventImage4 = new ArrayList<>();
    ArrayList<String> fixationLengthImage4 = new ArrayList<>();

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
                     final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        this.generateStatsFolder();
    }

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns, final boolean fourThree,
                     final IGameContext gameContext, final Stats stats, double gameSeed) {
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.fourThree = fourThree;
        this.stats = stats;
        this.targetAOIList = new ArrayList<>();
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
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
        if (firstTime){
            firstTime = false;

            this.sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            this.timestamp = new Timestamp(System.currentTimeMillis());

            this.computerTimestamp.add(this.timestamp.getTime());
            this.startTime = this.sdf.format(this.timestamp);
            this.step.add(this.iteration-1);
            this.goodAnswer.add(this.winnerImageIndexAmongDisplayedImages);

            eventImage1.add("");
            eventImage2.add("");
            eventImage3.add("");
            eventImage4.add("");
            fixationLengthImage1.add("");
            fixationLengthImage2.add("");
            fixationLengthImage3.add("");
            fixationLengthImage4.add("");
        }
    }

    public void updateStats(int indexImg){
        this.computerTimestamp.add(new Timestamp(System.currentTimeMillis()).getTime());
        this.step.add(this.iteration);
        this.goodAnswer.add(this.winnerImageIndexAmongDisplayedImages);
    }

    public void updatePictureCard(int index, String event, String value){
        switch (index){
            case 0 :
                eventImage1.add(event);
                eventImage2.add("");
                eventImage3.add("");
                eventImage4.add("");
                fixationLengthImage1.add(value);
                fixationLengthImage2.add("");
                fixationLengthImage3.add("");
                fixationLengthImage4.add("");
                break;

            case 1 :
                eventImage1.add("");
                eventImage2.add(event);
                eventImage3.add("");
                eventImage4.add("");
                fixationLengthImage1.add("");
                fixationLengthImage2.add(value);
                fixationLengthImage3.add("");
                fixationLengthImage4.add("");
                break;

            case 2 :
                eventImage1.add("");
                eventImage2.add("");
                eventImage3.add(event);
                eventImage4.add("");
                fixationLengthImage1.add("");
                fixationLengthImage2.add("");
                fixationLengthImage3.add(value);
                fixationLengthImage4.add("");
                break;

            case 3 :
                eventImage1.add("");
                eventImage2.add("");
                eventImage3.add("");
                eventImage4.add(event);
                fixationLengthImage1.add("");
                fixationLengthImage2.add("");
                fixationLengthImage3.add("");
                fixationLengthImage4.add(value);
                break;

            default:
                eventImage1.add("");
                eventImage2.add("");
                eventImage3.add("");
                eventImage4.add("");
                fixationLengthImage1.add("");
                fixationLengthImage2.add("");
                fixationLengthImage3.add("");
                fixationLengthImage4.add("");
        }
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();

        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        winnerImageIndexAmongDisplayedImages = randomGenerator.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        currentRoundDetails = pickAndBuildRandomPictures(numberOfImagesToDisplayPerRound, randomGenerator,
            winnerImageIndexAmongDisplayedImages);

        if (currentRoundDetails != null) {
            final Transition animation = createQuestionTransition(currentRoundDetails.getQuestion(), currentRoundDetails.getPictos());
            animation.play();
            if (currentRoundDetails.getQuestionSoundPath() != null) {
                playQuestionSound();
            }
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
        this.firstStat();

        if (gameType.toString().contains("SOUNDS")) {
            final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
            backgroundMusicManager.pause();
        }
    }

    private Transition createQuestionTransition(final String question, final List<Image> listOfPictos) {
        questionText = new Text(question);
        questionText.setTranslateY(0);

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

        questionText.setId(color);

        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double positionX = gamePaneDimension2D.getWidth() / 2 - questionText.getBoundsInParent().getWidth() * 2;
        final double positionY = gamePaneDimension2D.getHeight() / 2 - questionText.getBoundsInParent().getHeight() / 2;

        questionText.setX(positionX);
        questionText.setY(positionY);
        questionText.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(questionText, Pos.CENTER);

        gameContext.getChildren().add(questionText);
        final long timeStarted = System.currentTimeMillis();
        final TargetAOI targetAOI = new TargetAOI(gamePaneDimension2D.getWidth() / 2, gamePaneDimension2D.getHeight() / 2, (int) questionText.getBoundsInParent().getWidth(),
            timeStarted);
        targetAOI.setTimeEnded(timeStarted + gameContext.getConfiguration().getQuestionLength());
        targetAOIList.add(targetAOI);

        final List<Rectangle> pictogramsList = new ArrayList<>(20); // storage of actual Pictogram nodes in order to delete

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

            for (final Image picto : listOfPictos) {
                final Rectangle pictoRectangle = new Rectangle(pictoSize, pictoSize);
                pictoRectangle.setFill(new ImagePattern(picto));
                pictoRectangle.setY(positionY + 100);
                pictoRectangle.setX(shift + (i++ * pictoSize * 1.1));
                pictogramsList.add(pictoRectangle);
            }

            gameContext.getChildren().addAll(pictogramsList);
        }

        final TranslateTransition fullAnimation = new TranslateTransition(
            Duration.millis(gameContext.getConfiguration().getQuestionLength() / 2.0), questionText);

        fullAnimation.setDelay(Duration.millis(gameContext.getConfiguration().getQuestionLength()));

        final double bottomCenter = (0.9 * gamePaneDimension2D.getHeight()) - questionText.getY()
            + questionText.getBoundsInParent().getHeight() * 3;
        fullAnimation.setToY(bottomCenter);

        fullAnimation.setOnFinished(actionEvent -> {
            // gameContext.getChildren().remove(questionText);
            gameContext.getChildren().removeAll(pictogramsList);

            //log.debug("Adding {} pictures", currentRoundDetails.getPictureCardList().size());
            if (currentRoundDetails != null) {
                gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

                for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
                    //  log.debug("p = {}", p);
                    p.toFront();
                    p.setOpacity(1);
                }
            }
            questionText.toFront();

            stats.notifyNewRoundReady();

            gameContext.onGameStarted(2000);
        });

        return fullAnimation;
    }

    void playQuestionSound() {
        String soundResource = currentRoundDetails.getQuestionSoundPath();
        gameContext.getSoundManager().add(soundResource);
    }

    /**
     * this method should be called when exiting the game, or before starting a new round, in order to clean up all
     * resources in both UI and memory
     */
    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.getPictureCardList() != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.getPictureCardList());
            }
            currentRoundDetails = null;
        }
        stats.setTargetAOIList(targetAOIList);
        this.iteration++;
    }

    public void endGame(){
        this.endTime = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime() - this.timestamp.getTime());
        this.gameContext.getChildren().clear();
        this.createExcelFile();
        this.gameContext.showRoundStats(stats, this);
    }

    void removeAllIncorrectPictureCards() {
        //set the target AOI end time for this round

        final long endTime = System.currentTimeMillis();
        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;

        for (int i = 1; i <= numberOfImagesToDisplayPerRound; i++) {
            targetAOIList.get(targetAOIList.size() - i).setTimeEnded(endTime);
        }

        if (this.currentRoundDetails == null) {
            return;
        }

        // Collect all items to be removed from the User Interface
        final List<PictureCard> pictureCardsToHide = new ArrayList<>();
        for (final PictureCard pictureCard : this.currentRoundDetails.getPictureCardList()) {
            if (!pictureCard.isWinner()) {
                pictureCardsToHide.add(pictureCard);
            }
        }
        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(pictureCardsToHide);
    }

    static boolean fileIsImageFile(File file) {
        try {
            String mimetype = Files.probeContentType(file.toPath());
            if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                return true;
            }
        } catch (IOException ignored) {

        }
        return false;
    }

    RoundDetails pickAndBuildRandomPictures(final int numberOfImagesToDisplayPerRound, final ReplayablePseudoRandom random,
                                            final int winnerImageIndexAmongDisplayedImages) {
        final Configuration config = gameContext.getConfiguration();

        int directoriesCount;
        final String directoryName;
        List<File> imagesFolders = new LinkedList<>();
        List<String> resourcesFolders = new LinkedList<>();
        List<String> winnerFolders = new LinkedList<>();

            final String resourcesDirectory = "data/" + this.gameType.getResourcesDirectoryName();
            final String imagesDirectory = resourcesDirectory + "/images/";
            directoryName = imagesDirectory;

            // Here we filter out any unwanted resource folders, based on the variants JSON file
            Set<String> variantSet;
            try {
                SourceSet sourceSet = new SourceSet(resourcesDirectory + "/variants.json");
                variantSet = (sourceSet.getResources(this.gameType.getVariant()));
            } catch (FileNotFoundException fe) {
                log.info("No difficulty file found; Reading from all directories");
                variantSet = Collections.emptySet();
            }

            Set<String> tempResourcesFolders = ResourceFileManager.getResourceFolders(imagesDirectory);

            // If nothing can be found we take the entire folder contents.
            if (!variantSet.isEmpty()) {
                Set<String> finalVariantSet = variantSet;
                tempResourcesFolders = tempResourcesFolders
                    .parallelStream()
                    .filter(s -> finalVariantSet.parallelStream().anyMatch(s::contains))
                    .collect(Collectors.toSet());
            }

            resourcesFolders.addAll(tempResourcesFolders);
            directoriesCount = resourcesFolders.size();

        final String language = config.getLanguage();

        if (directoriesCount == 0) {
            log.warn("No images found in Directory " + directoryName);
            error(language);
            return null;
        }

        int posX = 0;
        int posY = 0;

        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, fourThree)
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());

        pictureCardList = new ArrayList<>();
        String questionSoundPath = null;
        String question = null;
        List<Image> pictograms = null;

            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {
                int index = random.nextInt(resourcesFolders.size());

                final String folder = resourcesFolders.remove((index) % directoriesCount);
                final String folderName = (new File(folder)).getName();

                final Set<String> files = ResourceFileManager.getResourcePaths(folder);
                final int numFile = random.nextInt(files.size());
                final String randomImageFile = (String) files.toArray()[numFile];

                if (winnerImageIndexAmongDisplayedImages == i) {
                    questionSoundPath = getPathSound(folderName, language);
                    question = getQuestionText(folderName, language);
                    pictograms = getPictograms(folderName);
                }

                final PictureCard pictureCard = new PictureCard(gameSizing.width * posX + gameSizing.shift,
                    gameSizing.height * posY, gameSizing.width, gameSizing.height, gameContext,
                    winnerImageIndexAmongDisplayedImages == i, randomImageFile + "", stats, this, i);

                pictureCardList.add(pictureCard);

                final TargetAOI targetAOI = new TargetAOI(gameSizing.width * (posX + 0.25), gameSizing.height * (posY + 1), (int) gameSizing.height,
                    System.currentTimeMillis());
                targetAOIList.add(targetAOI);


                if ((i + 1) % nbColumns != 0) {
                    posX++;
                } else {
                    posY++;
                    posX = 0;
                }
            }
        return new RoundDetails(pictureCardList, winnerImageIndexAmongDisplayedImages, questionSoundPath, question, pictograms);
    }

    /**
     * Return all files which don't start with a point
     */
    private File[] getFiles(final File folder) {
        return folder.listFiles(file -> !file.getName().startsWith("."));
    }

    private void error(final String language) {
        gameContext.clear();
        // HomeUtils.home(scene, group, choiceBox, null);

        final Multilinguism multilinguism = MultilinguismFactory.getSingleton();

        final Text error = new Text(multilinguism.getTranslation("WII-error", language));
        final Region root = gameContext.getRoot();
        error.setX(root.getWidth() / 2. - 100);
        error.setY(root.getHeight() / 2.);
        error.setId("item");
        gameContext.getChildren().addAll(error);
    }

    private String getPathSound(final String folder, final String language) {
        if (this.gameType == CUSTOMIZED) {
            final Configuration config = gameContext.getConfiguration();
            try {
                log.debug("CUSTOMIZED");
                final String path = config.getWhereIsItDir() + "/sounds/";
                final File soundsDirectory = new File(path);
                final String[] soundsDirectoryFiles = soundsDirectory.list();
                if (soundsDirectoryFiles != null) {
                    for (final String file : soundsDirectoryFiles) {
                        log.debug("file " + file);
                        log.debug("folder " + folder);
                        if (file.contains(folder)) {
                            final File f = new File(path + file);
                            log.debug("file " + f.getAbsolutePath());
                            return f.getAbsolutePath();
                        }
                    }
                }
            } catch (final Exception e) {
                log.debug("Problem with customized folder");
                error(config.getLanguage());
            }
            return "";
        }

        if (!(language.equals("fra") || language.equals("eng") || language.equals("chn"))) {
            // sound is only for English, French and Chinese erase when translation is complete
            return null;
        }

        log.debug("language is " + language);

        final String voice = randomGenerator.nextDouble() > 0.5 ? "m" : "w";

        if (gameType.toString().contains("SOUNDS")) {
            return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + folder + ".mp3";
        }
        return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + language + "/" + folder + "." + voice
            + "." + language + ".mp3";
    }

    private String getQuestionText(final String folder, final String language) {
        log.debug("folder: {}", folder);
        log.debug("language: {}", language);

        if (this.gameType == CUSTOMIZED) {
            final Configuration config = gameContext.getConfiguration();
            final File questionFile = new File(config.getWhereIsItDir() + "/questions.csv");
            final Multilinguism localMultilinguism = MultilinguismFactory.getForResource(questionFile.toString());

            return localMultilinguism.getTranslation(folder, language);
        }

        final Multilinguism localMultilinguism = MultilinguismFactory.getForResource(gameType.getLanguageResourceLocation());

        if (gameType.toString().contains("SOUNDS")) {
            return localMultilinguism.getTranslation("listen", language);
        }
        return localMultilinguism.getTranslation(folder, language);
    }

    private List<Image> getPictograms(final String folder) {
        final String language = "pictos";

        if (this.gameType != CUSTOMIZED) {
            return null;
        }

        final Configuration config = gameContext.getConfiguration();
        final File questionFile = new File(config.getWhereIsItDir(), "questions.csv");
        final Multilinguism localMultilinguism = MultilinguismFactory.getForResource(questionFile.toString());
        final String traduction = localMultilinguism.getTranslation(folder, language);
        log.debug("traduction: {}", traduction);

        final StringTokenizer st = new StringTokenizer(traduction, ";");
        final List<Image> imageList = new ArrayList<>(20);

        while (st.hasMoreTokens()) {
            final String token = config.getWhereIsItDir() + "/pictos/" + st.nextToken().replace('\u00A0', ' ').trim();
            log.debug("token \"{}\"", token);
            final File tokenFile = new File(token);
            log.debug("Exists {}", tokenFile.exists());
            if (tokenFile.exists()) {
                imageList.add(new Image(tokenFile.toURI().toString(), 500, 500, true, false));
            }
        }

        log.debug("imageList: {}", imageList);
        return imageList;
    }

    public void updateRight() {
        rightDecision++;
    }

    public void updateWrong() {
        wrongDecision++;
    }

    public boolean getFirstWrong() {
        return firstWrong;
    }

    public void firstWrongCardSelected() {
        firstWrong = true;
    }

    public void firstRightCardSelected() {
        firstWrong = false;
    }

    public int factorial(int n) {
        return n == 0 ? 1 : (n * factorial(n - 1));
    }

    public float compute(int n, int k) {
        return (float) factorial(n) / (factorial(k) * factorial(n - k));
    }

    public double binomProba(int n, int k, double p) {
        return compute(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    public double chi2Obs(int tp, int fp) {
        double[] probas = {8 * lvlReplays * binomProba(1, 1, 0.5), 8 * lvlReplays * binomProba(1, 0, 0.5)};

        return Math.pow(tp - probas[0], 2) / probas[0] + Math.pow(fp - probas[1], 2) / probas[1];
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
        bookData[0][5] = "Good answer";
        bookData[0][6] = "Event Image 1";
        bookData[0][7] = "Fixation Length Image 1";
        bookData[0][8] = "Event Image 2";
        bookData[0][9] = "Fixation Length Image 2";
        bookData[0][10] = "Event Image 3";
        bookData[0][11] = "Fixation Length Image 3";
        bookData[0][12] = "Event Image 4";
        bookData[0][13] = "Fixation Length Image 4";

        for (int i=0; i<this.computerTimestamp.size(); i++){
            bookData[i+1][0] = String.valueOf(this.computerTimestamp.get(i) - this.computerTimestamp.get(0));
            bookData[i+1][1] = String.valueOf(this.computerTimestamp.get(i));
            bookData[i+1][2] = String.valueOf(this.startTime);
            bookData[i+1][3] = String.valueOf(this.endTime);
            bookData[i+1][4] = String.valueOf(this.step.get(i));
            bookData[i+1][5] = String.valueOf(this.goodAnswer.get(i)+1);
            bookData[i+1][6] = String.valueOf(eventImage1.get(i));
            bookData[i+1][7] = String.valueOf(fixationLengthImage1.get(i));
            bookData[i+1][8] = String.valueOf(eventImage2.get(i));
            bookData[i+1][9] = String.valueOf(fixationLengthImage2.get(i));
            bookData[i+1][10] = String.valueOf(eventImage3.get(i));
            bookData[i+1][11] = String.valueOf(fixationLengthImage3.get(i));
            bookData[i+1][12] = String.valueOf(eventImage4.get(i));
            bookData[i+1][13] = String.valueOf(fixationLengthImage4.get(i));
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
