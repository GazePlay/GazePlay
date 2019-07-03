package net.gazeplay.commons.configuration;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.utils.games.Utils;

import java.awt.print.Book;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import static net.gazeplay.commons.themes.BuiltInUiTheme.DEFAULT_THEME;

@Slf4j
public class Configuration implements Cloneable {

    private static final String PROPERTY_NAME_GAZEMODE = "GAZEMODE";
    private static final String PROPERTY_NAME_EYETRACKER = "EYETRACKER";
    private static final String PROPERTY_NAME_LANGUAGE = "LANGUAGE";
    private static final String PROPERTY_NAME_FILEDIR = "FILEDIR";
    private static final String PROPERTY_NAME_FIXATIONLENGTH = "FIXATIONLENGTH";
    private static final String PROPERTY_NAME_CSSFILE = "CSSFILE";
    private static final String PROPERTY_NAME_WHEREISIT_DIR = "WHEREISITDIR";
    private static final String PROPERTY_NAME_QUESTION_LENGTH = "QUESTIONLENGTH";
    private static final String PROPERTY_NAME_ENABLE_REWARD_SOUND = "ENABLE_REWARD_SOUND";
    private static final String PROPERTY_NAME_MENU_BUTTONS_ORIENTATION = "MENU_BUTTONS_ORIENTATION";
    private static final String PROPERTY_NAME_HEATMAP_DISABLED = "HEATMAP_DISABLED";
    private static final String PROPERTY_NAME_AREA_OF_INTEREST_DISABLED = "AREA_OF_INTEREST_DISABLED";
    private static final String PROPERTY_NAME_CONVEX_HULL_DISABLED = "CONVEX_HULL_DISABLED";
    private static final String PROPERTY_NAME_VIDEO_RECORDING_DISABLED = "VIDEO_RECORDING_DISABLED";
    private static final String PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED = "FIXATIONSEQUENCE_DISABLED";
    private static final String PROPERTY_NAME_MUSIC_VOLUME = "MUSIC_VOLUME";
    private static final String PROPERTY_NAME_MUSIC_FOLDER = "MUSIC_FOLDER";
    private static final String PROPERTY_NAME_EFFECTS_VOLUME = "EFFECTS_VOLUME";
    private static final String PROPERTY_NAME_GAZE_MENU = "GAZE_MENU";
    private static final String PROPERTY_NAME_GAZE_MOUSE = "GAZE_MOUSE";
    private static final String PROPERTY_NAME_WHITE_BCKGRD = "WHITE_BACKGROUND";
    private static final String PROPERTY_NAME_SPEED_EFFECTS = "SPEED_EFFECTS";
    private static final String PROPERTY_NAME_USER_NAME = "USER_NAME";
    private static final String PROPERTY_NAME_USER_PICTURE = "USER_PICTURE";
    private static final String PROPERTY_NAME_QUIT_KEY = "QUIT_KEY";
    /**
     * Game Categories Properties
     */
    private static final String PROPERTY_NAME_SELECTION_GAMES = "Selection games";
    private static final String PROPERTY_NAME_ACTION_REACTION_GAMES = "Action-Reaction games";
    private static final String PROPERTY_NAME_MEMORIZATION_GAMES = "Memorization games";
    private static final String PROPERTY_NAME_LOGIC_GAMES = "Logic games";
    private static final String PROPERTY_NAME_NO_CATEGORY_GAMES = "No category games";
    /**
     * Favourite Games Property
     */
    private static final String PROPERTY_NAME_FAVOURITE_POTIONS = "POTIONS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MATH101 = "MATH101 Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MATH102 = "MATH102 Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MATH103 = "MATH103 Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MATH104 = "MATH104 Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MATH201 = "MATH201 Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_CREAMPIE = "CREAMPIE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_NINJA = "NINJA Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_PUZZLE = "PUZZLE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MAGICCARDS = "MAGICCARDS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_BLOCKS = "BLOCKS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_LETTERS = "LETTERS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_SCRATCHCARD = "SCRATCHCARD Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_COLOREDBUBBLES = "COLOREDBUBBLES Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_PORTRAITBUBBLES = "PORTRAITBUBBLES Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_WHEREISTHEANIMAL = "WHEREISTHEANIMAL Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_WHEREISTHECOLOR = "WHEREISTHECOLOR Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_FINDODD = "FINDODD Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_WHEREISTHELETTER = "WHEREISTHELETTER Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_WHEREISTHENUMBER = "WHEREISTHENUMBER Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_FLAGS = "FLAGS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_WHEREISIT = "WHEREISIT Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_BIBOULES = "BIBOULE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_ROBOTS = "ROBOTS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_DIVISOR = "DIVISOR Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_LAPINS = "RABBITS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MEMORY = "MEMORY Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MEMORYLETTERS = "MEMORYLETTERS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MEMORYNUMBERS = "MEMORYNUMBERS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_OPENMEMORY = "OPENMEMORY Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_OPENMEMORYLETTERS = "OOPENMEMORYLETTERS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_OPENMEMORYNUMBERS = "OPENMEMORYNUMBERS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_VIDEOPLAYER = "VIDEOPLAYER Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_SCRIBBLE = "SCRIBBLE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_CUPSBALLS = "CUPSBALLS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_ORDER = "ORDER Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_ROOM = "ROOM Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_PIANO = "PIANO Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_WHACAMOLE = "WHACAMOLE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_PET = "PET Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_MEDIAPLAYER = "MEDIAPLAYER Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_RUSHHOUR = "RUSHHOUR Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_COLORSSS = "COLORSSS Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_CAKES = "CAKES Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_LABYRINTH = "LABYRINTH Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_FROGSRACE = "FROGSRACE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_BIBJUMP = "BIBJUMP Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_SPOTDIFFERENCE = "SPOTDIFFERENCE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_DICE = "DICE Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_SPACEGAME = "SPACEGAME Game fav";
    private static final String PROPERTY_NAME_FAVOURITE_GOOSEGAME = "GOOSEGAME Game fav";

    @Getter
    @Setter
    private static String CONFIGPATH = Utils.getGazePlayFolder() + "GazePlay.properties";
    private static final KeyCode DEFAULT_VALUE_QUIT_KEY = KeyCode.Q;
    private static final boolean DEFAULT_VALUE_GAZEMODE = true;
    private static final String DEFAULT_VALUE_EYETRACKER = EyeTracker.mouse_control.toString();
    private static final String DEFAULT_VALUE_LANGUAGE = "fra";
    private static final int DEFAULT_VALUE_FIXATION_LENGTH = 500;
    private static final String DEFAULT_VALUE_CSS_FILE = DEFAULT_THEME.getPreferredConfigPropertyValue();
    public static final String DEFAULT_VALUE_WHEREISIT_DIR = "";
    private static final int DEFAULT_VALUE_QUESTION_LENGTH = 5000;
    private static final boolean DEFAULT_VALUE_ENABLE_REWARD_SOUND = true;
    private static final String DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION = "HORIZONTAL";
    private static final boolean DEFAULT_VALUE_HEATMAP_DISABLED = false;
    private static final boolean DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED = false;
    private static final boolean DEFAULT_VALUE_CONVEX_HULL_DISABLED = false;
    private static final boolean DEFAULT_VALUE_VIDEO_RECORDING = false;
    private static final boolean DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED = false;
    public static final double DEFAULT_VALUE_MUSIC_VOLUME = 0.25;
    public static final String DEFAULT_VALUE_MUSIC_FOLDER = "";
    private static final Double DEFAULT_VALUE_EFFECTS_VOLUME = DEFAULT_VALUE_MUSIC_VOLUME;
    private static final boolean DEFAULT_VALUE_SELECTION_GAMES = true;
    private static final boolean DEFAULT_VALUE_MEMORIZATION_GAMES = true;
    private static final boolean DEFAULT_VALUE_ACTION_REACTION_GAMES = true;
    private static final boolean DEFAULT_VALUE_NO_CATEGORY_GAMES = true;
    private static final boolean DEFAULT_VALUE_FAVOURITE_GAMES = false;

    private static final boolean DEFAULT_VALUE_LOGIC_GAMES = true;
    // next thing to do
    // private static final String DEFAULT_EXIT_SHORTCUT_KEY = "SPACE";

    @Setter
    @Getter
    public static String DEFAULT_VALUE_FILE_DIR = getFileDirectoryDefaultValue();
    public static final boolean DEFAULT_VALUE_GAZE_MENU = false;
    public static final boolean DEFAULT_VALUE_GAZE_MOUSE = false;
    public static final boolean DEFAULT_VALUE_WHITE_BCKGRD = false;
    public static final double DEFAULT_VALUE_SPEED_EFFECTS = 4;
    private static final String DEFAULT_VALUE_USER_NAME = "";
    public static final String DEFAULT_VALUE_USER_PICTURE = "";

    public static boolean isMouseFree = false;

    private static String getFileDirectoryDefaultValue() {
        return Utils.getGazePlayFolder() + "files" /* + Utils.FILESEPARATOR */;
    }

    public static String getFileDirectoryUserValue(String user) {
        return Utils.getGazePlayFolder() + "profiles/" + user + Utils.FILESEPARATOR
                + "files" /* + Utils.FILESEPARATOR */;
    }

    private static Properties loadProperties(String propertiesFilePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(propertiesFilePath)) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }

    public static Configuration createFromPropertiesResource() {
        Properties properties;
        try {
            log.info("loading new properties from ={}", CONFIGPATH);
            properties = loadProperties(CONFIGPATH);
        } catch (FileNotFoundException e) {
            log.warn("Config file not found : {}", CONFIGPATH);
            properties = null;
        } catch (IOException e) {
            log.error("Failure while loading config file {}", CONFIGPATH, e);
            properties = null;
        }
        final Configuration config = new Configuration();
        if (properties != null) {
            log.info("Properties loaded : {}", properties);
            config.populateFromProperties(properties);
        }
        return config;
    }

    public Configuration reset() {
        return Configuration.createFromPropertiesResource();
    }

    /*
     * public static final Configuration getInstance() { return Configuration.createFromPropertiesResource(); }
     */

    @Getter
    @Setter
    private static Configuration instance = Configuration.createFromPropertiesResource();

    // @Getter
    // protected final ListProperty<BooleanProperty> FavouriteGamesProperty = new
    // SimpleListProperty<BooleanProperty>(this, PROPERTY_NAME_FAVOURITE_GAMES,
    // DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    // // @Setter
    private List<BooleanProperty> favouriteGameProperties = new ArrayList<BooleanProperty>();
    ////

    @Getter
    protected final StringProperty QuitKeyProperty = new SimpleStringProperty(this, PROPERTY_NAME_QUIT_KEY,
            DEFAULT_VALUE_QUIT_KEY.toString());
    @Getter
    protected final BooleanProperty gazeModeProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZEMODE,
            DEFAULT_VALUE_GAZEMODE);

    @Getter
    protected final BooleanProperty gazeMenuProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZE_MENU,
            DEFAULT_VALUE_GAZE_MENU);

    @Getter
    protected final StringProperty eyetrackerProperty = new SimpleStringProperty(this, PROPERTY_NAME_EYETRACKER,
            DEFAULT_VALUE_EYETRACKER);

    @Getter
    protected final StringProperty languageProperty = new SimpleStringProperty(this, PROPERTY_NAME_LANGUAGE,
            DEFAULT_VALUE_LANGUAGE);

    @Getter
    protected final StringProperty filedirProperty = new SimpleStringProperty(this, PROPERTY_NAME_FILEDIR,
            DEFAULT_VALUE_FILE_DIR);

    @Getter
    protected final IntegerProperty fixationlengthProperty = new SimpleIntegerProperty(this,
            PROPERTY_NAME_FIXATIONLENGTH, DEFAULT_VALUE_FIXATION_LENGTH);

    @Getter
    protected final StringProperty cssfileProperty = new SimpleStringProperty(this, PROPERTY_NAME_CSSFILE,
            DEFAULT_VALUE_CSS_FILE);

    @Getter
    protected final StringProperty whereIsItDirProperty = new SimpleStringProperty(this, PROPERTY_NAME_WHEREISIT_DIR,
            DEFAULT_VALUE_WHEREISIT_DIR);

    @Getter
    protected final IntegerProperty questionLengthProperty = new SimpleIntegerProperty(this,
            PROPERTY_NAME_QUESTION_LENGTH, DEFAULT_VALUE_QUESTION_LENGTH);

    @Getter
    protected final BooleanProperty enableRewardSoundProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_ENABLE_REWARD_SOUND, DEFAULT_VALUE_ENABLE_REWARD_SOUND);

    @Getter
    protected final StringProperty menuButtonsOrientationProperty = new SimpleStringProperty(this,
            PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION);

    @Getter
    protected final BooleanProperty heatMapDisabledProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_HEATMAP_DISABLED, DEFAULT_VALUE_HEATMAP_DISABLED);
    @Getter
    protected final BooleanProperty areaOfInterestDisabledProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_AREA_OF_INTEREST_DISABLED, DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED);
    @Getter
    protected final BooleanProperty convexHullDisabledProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_CONVEX_HULL_DISABLED, DEFAULT_VALUE_CONVEX_HULL_DISABLED);
    @Getter
    protected final BooleanProperty videoRecordingDisabledProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_VIDEO_RECORDING_DISABLED, DEFAULT_VALUE_VIDEO_RECORDING);
    @Getter
    protected final BooleanProperty fixationSequenceDisabledProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED, DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED);
    @Getter
    protected final BooleanProperty gazeMouseProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZE_MOUSE,
            DEFAULT_VALUE_GAZE_MOUSE);

    @Getter
    protected final BooleanProperty whiteBackgroundProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_WHITE_BCKGRD, DEFAULT_VALUE_WHITE_BCKGRD);

    @Getter
    protected final DoubleProperty musicVolumeProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_MUSIC_VOLUME,
            DEFAULT_VALUE_MUSIC_VOLUME);

    @Getter
    protected final StringProperty musicFolderProperty = new SimpleStringProperty(this, PROPERTY_NAME_MUSIC_FOLDER,
            DEFAULT_VALUE_MUSIC_FOLDER);

    @Getter
    protected final DoubleProperty effectsVolumeProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_EFFECTS_VOLUME,
            DEFAULT_VALUE_EFFECTS_VOLUME);

    @Getter
    protected final DoubleProperty speedEffectsProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_SPEED_EFFECTS,
            DEFAULT_VALUE_SPEED_EFFECTS);

    @Getter
    protected final StringProperty userNameProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_NAME,
            DEFAULT_VALUE_USER_NAME);
    @Getter
    protected final StringProperty userPictureProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_PICTURE,
            DEFAULT_VALUE_USER_PICTURE);

    @Getter
    protected final BooleanProperty selectionCategoryProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_SELECTION_GAMES, DEFAULT_VALUE_SELECTION_GAMES);

    @Getter
    protected final BooleanProperty memorizationCategoryProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_MEMORIZATION_GAMES, DEFAULT_VALUE_MEMORIZATION_GAMES);

    @Getter
    protected final BooleanProperty actionReactionCategoryProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_ACTION_REACTION_GAMES, DEFAULT_VALUE_ACTION_REACTION_GAMES);

    @Getter
    protected final BooleanProperty noCategoryProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_NO_CATEGORY_GAMES, DEFAULT_VALUE_NO_CATEGORY_GAMES);

    @Getter
    protected final BooleanProperty logicCategoryProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_LOGIC_GAMES,
            DEFAULT_VALUE_LOGIC_GAMES);

    @Getter
    protected final BooleanProperty potionsFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_POTIONS, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty math101FavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MATH101, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty math102FavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MATH102, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty math103FavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MATH103, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty math104FavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MATH104, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty math201FavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MATH201, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty creamPieFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_CREAMPIE, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty ninjaFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_NINJA,
            DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty puzzleFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_PUZZLE,
            DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty magicCardsFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MAGICCARDS, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty blocksFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_BLOCKS,
            DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty lettersFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_LETTERS, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty scratchCardFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_SCRATCHCARD, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty coloredBubblesFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_COLOREDBUBBLES, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty portraitBubblesFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_PORTRAITBUBBLES, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty whereAnimalFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_WHEREISTHEANIMAL, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty whereColorFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_WHEREISTHECOLOR, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty whereLetterFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_WHEREISTHELETTER, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty whereNumberFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_WHEREISTHENUMBER, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty findOddFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_FINDODD, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty flagsFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_FLAGS,
            DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty whereItFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_WHEREISIT, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty biboulesFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_BIBOULES, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty robotsFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_ROBOTS,
            DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty divisorFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_DIVISOR, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty rabbitsFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_LAPINS,
            DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty memoryFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_MEMORY,
            DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty memoryLettersFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MEMORYLETTERS, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty memoryNumbersFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MEMORYNUMBERS, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty openMemoryFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_OPENMEMORY, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty openMemoryLettersFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_OPENMEMORYLETTERS, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty openMemoryNumbersFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_OPENMEMORYNUMBERS, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty videoPlayerFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_VIDEOPLAYER, DEFAULT_VALUE_FAVOURITE_GAMES);

    @Getter
    protected final BooleanProperty scribbleFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_SCRIBBLE, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty cupsBallsFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_CUPSBALLS, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty orderFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_ORDER,
            DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty roomFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_ROOM,
            DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty pianoFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_PIANO,
            DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty whacamoleFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_WHACAMOLE, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty petFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_PET,
            DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty mediaPlayerFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_MEDIAPLAYER, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty rushHourFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_RUSHHOUR, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty colorsFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_COLORSSS, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty cakesFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_CAKES,
            DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty labyrinthFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_LABYRINTH, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty frogsRaceFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_FROGSRACE, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty bibouleJumpFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_BIBJUMP, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty spotDifferenceFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_SPOTDIFFERENCE, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty diceFavProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FAVOURITE_DICE,
            DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    private final BooleanProperty spaceGameFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_SPACEGAME, DEFAULT_VALUE_FAVOURITE_GAMES);
    @Getter
    protected final BooleanProperty gooseGameFavProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FAVOURITE_GOOSEGAME, DEFAULT_VALUE_FAVOURITE_GAMES);

    public void FavProperties() {

        favouriteGameProperties.add(bibouleJumpFavProperty);
        favouriteGameProperties.add(biboulesFavProperty);
        favouriteGameProperties.add(blocksFavProperty);
        favouriteGameProperties.add(cakesFavProperty);
        favouriteGameProperties.add(coloredBubblesFavProperty);
        favouriteGameProperties.add(colorsFavProperty);
        favouriteGameProperties.add(creamPieFavProperty);
        favouriteGameProperties.add(cupsBallsFavProperty);
        favouriteGameProperties.add(diceFavProperty);
        favouriteGameProperties.add(divisorFavProperty);
        favouriteGameProperties.add(findOddFavProperty);
        favouriteGameProperties.add(flagsFavProperty);
        favouriteGameProperties.add(frogsRaceFavProperty);
        favouriteGameProperties.add(gooseGameFavProperty);
        favouriteGameProperties.add(lettersFavProperty);
        favouriteGameProperties.add(labyrinthFavProperty);
        favouriteGameProperties.add(rabbitsFavProperty);
        favouriteGameProperties.add(math101FavProperty);
        favouriteGameProperties.add(math102FavProperty);
        favouriteGameProperties.add(math103FavProperty);
        favouriteGameProperties.add(math104FavProperty);
        favouriteGameProperties.add(math201FavProperty);
        favouriteGameProperties.add(magicCardsFavProperty);
        favouriteGameProperties.add(mediaPlayerFavProperty);
        favouriteGameProperties.add(memoryFavProperty);
        favouriteGameProperties.add(memoryLettersFavProperty);
        favouriteGameProperties.add(memoryNumbersFavProperty);
        favouriteGameProperties.add(ninjaFavProperty);
        favouriteGameProperties.add(openMemoryFavProperty);
        favouriteGameProperties.add(openMemoryLettersFavProperty);
        favouriteGameProperties.add(openMemoryNumbersFavProperty);
        favouriteGameProperties.add(orderFavProperty);
        favouriteGameProperties.add(petFavProperty);
        favouriteGameProperties.add(pianoFavProperty);
        favouriteGameProperties.add(portraitBubblesFavProperty);
        favouriteGameProperties.add(potionsFavProperty);
        favouriteGameProperties.add(puzzleFavProperty);
        favouriteGameProperties.add(robotsFavProperty);
        favouriteGameProperties.add(roomFavProperty);
        favouriteGameProperties.add(rushHourFavProperty);
        favouriteGameProperties.add(scratchCardFavProperty);
        favouriteGameProperties.add(scribbleFavProperty);
        favouriteGameProperties.add(spaceGameFavProperty);
        favouriteGameProperties.add(spotDifferenceFavProperty);
        favouriteGameProperties.add(videoPlayerFavProperty);
        favouriteGameProperties.add(whacamoleFavProperty);
        favouriteGameProperties.add(whereItFavProperty);
        favouriteGameProperties.add(whereAnimalFavProperty);
        favouriteGameProperties.add(whereColorFavProperty);
        favouriteGameProperties.add(whereLetterFavProperty);
        favouriteGameProperties.add(whereNumberFavProperty);
        // return favouriteGameProperties;
    }

    protected Configuration() {

        FavProperties();
        // Listeners
        musicVolumeProperty.addListener((observable) -> {
            double musicVolume = getMusicVolume();
            if (musicVolume > 1) {
                log.warn("Invalid msuic volume value set : {}. 1 set instead", musicVolume);
                musicVolumeProperty.setValue(1);
            } else if (musicVolume < 0) {
                log.warn("Invalid msuic volume value set : {}. 0 set instead", musicVolume);
                musicVolumeProperty.setValue(0);
            }
        });

        effectsVolumeProperty.addListener((observable) -> {
            double musicVolume = getMusicVolume();
            if (musicVolume > 1) {
                log.warn("Invalid effects volume value set : {}. 1 set instead", musicVolume);
                effectsVolumeProperty.setValue(1);
            } else if (musicVolume < 0) {
                log.warn("Invalid effects volume value set : {}. 0 set instead", musicVolume);
                effectsVolumeProperty.setValue(0);
            }
        });
    }

    public void populateFromProperties(Properties prop) {
        String buffer;

        buffer = prop.getProperty(PROPERTY_NAME_QUIT_KEY);
        if (buffer != null) {
            QuitKeyProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_GAZEMODE);
        if (buffer != null) {
            gazeModeProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_EYETRACKER);
        if (buffer != null) {
            eyetrackerProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_LANGUAGE);
        if (buffer != null) {
            languageProperty.setValue(buffer.toLowerCase());
        }

        buffer = prop.getProperty(PROPERTY_NAME_FILEDIR);
        if (buffer != null) {
            filedirProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_FIXATIONLENGTH);
        if (buffer != null) {
            try {
                fixationlengthProperty.setValue(Integer.parseInt(buffer));
            } catch (NumberFormatException e) {
                log.warn("NumberFormatException while parsing value '{}' for property {}", buffer,
                        PROPERTY_NAME_FIXATIONLENGTH);
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_CSSFILE);
        if (buffer != null) {
            cssfileProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_WHEREISIT_DIR);
        if (buffer != null) {
            whereIsItDirProperty.setValue(buffer.toLowerCase());
        }

        buffer = prop.getProperty(PROPERTY_NAME_QUESTION_LENGTH);
        if (buffer != null) {
            try {
                questionLengthProperty.setValue(Integer.parseInt(buffer));
            } catch (NumberFormatException e) {
                log.warn("NumberFormatException while parsing value '{}' for property {}", buffer,
                        PROPERTY_NAME_QUESTION_LENGTH);
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_ENABLE_REWARD_SOUND);
        if (buffer != null) {
            enableRewardSoundProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_MENU_BUTTONS_ORIENTATION);
        if (buffer != null) {
            menuButtonsOrientationProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_HEATMAP_DISABLED);
        if (buffer != null) {
            heatMapDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_AREA_OF_INTEREST_DISABLED);
        if (buffer != null) {
            areaOfInterestDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_CONVEX_HULL_DISABLED);
        if (buffer != null) {
            convexHullDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_VIDEO_RECORDING_DISABLED);
        if (buffer != null) {
            videoRecordingDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED);
        if (buffer != null) {
            fixationSequenceDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_MUSIC_VOLUME);
        if (buffer != null) {
            musicVolumeProperty.setValue(Double.parseDouble(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_MUSIC_FOLDER);
        if (buffer != null) {
            musicFolderProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_EFFECTS_VOLUME);
        if (buffer != null) {
            try {
                effectsVolumeProperty.setValue(Double.parseDouble(buffer));
            } catch (NumberFormatException e) {
                log.warn("Malformed property");
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_SPEED_EFFECTS);
        if (buffer != null) {
            try {
                speedEffectsProperty.setValue(Double.parseDouble(buffer));
            } catch (NumberFormatException e) {
                log.warn("Malformed property");
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_GAZE_MENU);
        if (buffer != null) {
            gazeMenuProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_GAZE_MOUSE);
        if (buffer != null) {
            gazeMouseProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_WHITE_BCKGRD);
        if (buffer != null) {
            whiteBackgroundProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_USER_NAME);
        if (buffer != null) {
            userNameProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_USER_PICTURE);
        if (buffer != null) {
            userPictureProperty.setValue(buffer);
        }
        buffer = prop.getProperty(PROPERTY_NAME_SELECTION_GAMES);
        if (buffer != null) {
            selectionCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_ACTION_REACTION_GAMES);
        if (buffer != null) {
            actionReactionCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_MEMORIZATION_GAMES);
        if (buffer != null) {
            memorizationCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_NO_CATEGORY_GAMES);
        if (buffer != null) {
            noCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_LOGIC_GAMES);
        if (buffer != null) {
            logicCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_BIBJUMP);
        if (buffer != null) {
            bibouleJumpFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_BIBOULES);
        if (buffer != null) {
            biboulesFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_BLOCKS);
        if (buffer != null) {
            blocksFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_CAKES);
        if (buffer != null) {
            cakesFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_COLOREDBUBBLES);
        if (buffer != null) {
            coloredBubblesFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_COLORSSS);
        if (buffer != null) {
            colorsFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_CREAMPIE);
        if (buffer != null) {
            creamPieFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_CUPSBALLS);
        if (buffer != null) {
            cupsBallsFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_DICE);
        if (buffer != null) {
            diceFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_DIVISOR);
        if (buffer != null) {
            divisorFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_FINDODD);
        if (buffer != null) {
            findOddFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_FLAGS);
        if (buffer != null) {
            flagsFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_FROGSRACE);
        if (buffer != null) {
            frogsRaceFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_GOOSEGAME);
        if (buffer != null) {
            gooseGameFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_LABYRINTH);
        if (buffer != null) {
            labyrinthFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_LAPINS);
        if (buffer != null) {
            rabbitsFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_LETTERS);
        if (buffer != null) {
            lettersFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MAGICCARDS);
        if (buffer != null) {
            magicCardsFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MATH101);
        if (buffer != null) {
            math101FavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MATH102);
        if (buffer != null) {
            math102FavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MATH103);
        if (buffer != null) {
            math102FavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MATH104);
        if (buffer != null) {
            math104FavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MATH201);
        if (buffer != null) {
            math201FavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MEDIAPLAYER);
        if (buffer != null) {
            mediaPlayerFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MEMORY);
        if (buffer != null) {
            memoryFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MEMORYLETTERS);
        if (buffer != null) {
            memoryLettersFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_MEMORYNUMBERS);
        if (buffer != null) {
            memoryNumbersFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_NINJA);
        if (buffer != null) {
            ninjaFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_OPENMEMORY);
        if (buffer != null) {
            openMemoryFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_OPENMEMORYLETTERS);
        if (buffer != null) {
            openMemoryLettersFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_OPENMEMORYNUMBERS);
        if (buffer != null) {
            openMemoryNumbersFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_ORDER);
        if (buffer != null) {
            orderFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_PET);
        if (buffer != null) {
            petFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_PIANO);
        if (buffer != null) {
            pianoFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_PORTRAITBUBBLES);
        if (buffer != null) {
            portraitBubblesFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_POTIONS);
        if (buffer != null) {
            potionsFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_PUZZLE);
        if (buffer != null) {
            puzzleFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_ROBOTS);
        if (buffer != null) {
            robotsFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_ROOM);
        if (buffer != null) {
            roomFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_RUSHHOUR);
        if (buffer != null) {
            rushHourFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_SCRATCHCARD);
        if (buffer != null) {
            scratchCardFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_SCRIBBLE);
        if (buffer != null) {
            scribbleFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_SPACEGAME);
        if (buffer != null) {
            spaceGameFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_SPOTDIFFERENCE);
        if (buffer != null) {
            spotDifferenceFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_VIDEOPLAYER);
        if (buffer != null) {
            videoPlayerFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_WHACAMOLE);
        if (buffer != null) {
            whacamoleFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_WHEREISIT);
        if (buffer != null) {
            whereItFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHEANIMAL);
        if (buffer != null) {
            whereAnimalFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHECOLOR);
        if (buffer != null) {
            whereColorFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHELETTER);
        if (buffer != null) {
            whereLetterFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHENUMBER);
        if (buffer != null) {
            whereNumberFavProperty.setValue(Boolean.parseBoolean(buffer));
        }
    }

    public Properties toProperties() {
        Properties properties = new Properties() {

            @Override
            public Object setProperty(String key, String value) {
                if (value == null) {
                    return this.remove(key);
                }
                return super.setProperty(key, value);
            }

        };

        // FIXME why is this not saved to file ? -> Certainly no longer usefull (see issue #102)
        // properties.setProperty(PROPERTY_NAME_GAZEMODE, this.gazeMode);

        properties.setProperty(PROPERTY_NAME_EYETRACKER, this.eyetrackerProperty.getValue());
        properties.setProperty(PROPERTY_NAME_LANGUAGE, this.languageProperty.getValue());
        properties.setProperty(PROPERTY_NAME_QUIT_KEY, this.QuitKeyProperty.getValue());
        properties.setProperty(PROPERTY_NAME_FILEDIR, this.filedirProperty.getValue());
        properties.setProperty(PROPERTY_NAME_FIXATIONLENGTH, Integer.toString(this.fixationlengthProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_CSSFILE, this.cssfileProperty.getValue());
        properties.setProperty(PROPERTY_NAME_WHEREISIT_DIR, this.whereIsItDirProperty.getValue());
        properties.setProperty(PROPERTY_NAME_QUESTION_LENGTH, Integer.toString(this.questionLengthProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_ENABLE_REWARD_SOUND,
                Boolean.toString(this.enableRewardSoundProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, this.menuButtonsOrientationProperty.getValue());
        properties.setProperty(PROPERTY_NAME_HEATMAP_DISABLED,
                Boolean.toString(this.heatMapDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_AREA_OF_INTEREST_DISABLED,
                Boolean.toString(this.areaOfInterestDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_CONVEX_HULL_DISABLED,
                Boolean.toString(this.convexHullDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_VIDEO_RECORDING_DISABLED,
                Boolean.toString(this.videoRecordingDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED,
                Boolean.toString(this.fixationSequenceDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_VOLUME, Double.toString(this.musicVolumeProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_FOLDER, this.musicFolderProperty.getValue());
        properties.setProperty(PROPERTY_NAME_EFFECTS_VOLUME, Double.toString(effectsVolumeProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_SPEED_EFFECTS, Double.toString(speedEffectsProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_WHITE_BCKGRD, Boolean.toString(whiteBackgroundProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_USER_NAME, this.userNameProperty.getValue());
        properties.setProperty(PROPERTY_NAME_USER_PICTURE, this.userPictureProperty.getValue());
        /*
         * properties.setProperty(PROPERTY_NAME_GAZE_MENU, Boolean.toString(this.gazeMenuProperty.getValue()));
         */
        properties.setProperty(PROPERTY_NAME_SELECTION_GAMES, Boolean.toString(selectionCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MEMORIZATION_GAMES,
                Boolean.toString(memorizationCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_ACTION_REACTION_GAMES,
                Boolean.toString(actionReactionCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_NO_CATEGORY_GAMES, Boolean.toString(noCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_LOGIC_GAMES, Boolean.toString(logicCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_BIBJUMP, Boolean.toString(bibouleJumpFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_BIBOULES, Boolean.toString(biboulesFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_BLOCKS, Boolean.toString(blocksFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_CAKES, Boolean.toString(cakesFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_COLOREDBUBBLES,
                Boolean.toString(coloredBubblesFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_COLORSSS, Boolean.toString(colorsFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_CREAMPIE, Boolean.toString(creamPieFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_CUPSBALLS, Boolean.toString(cupsBallsFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_DICE, Boolean.toString(diceFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_DIVISOR, Boolean.toString(divisorFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_FINDODD, Boolean.toString(findOddFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_FLAGS, Boolean.toString(flagsFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_FROGSRACE, Boolean.toString(frogsRaceFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_GOOSEGAME, Boolean.toString(gooseGameFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_LABYRINTH, Boolean.toString(labyrinthFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_LAPINS, Boolean.toString(rabbitsFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_LETTERS, Boolean.toString(lettersFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MAGICCARDS, Boolean.toString(magicCardsFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MATH101, Boolean.toString(math101FavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MATH102, Boolean.toString(math102FavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MATH103, Boolean.toString(math103FavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MATH104, Boolean.toString(math104FavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MATH201, Boolean.toString(math201FavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MEDIAPLAYER,
                Boolean.toString(mediaPlayerFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MEMORY, Boolean.toString(memoryFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MEMORYLETTERS,
                Boolean.toString(memoryLettersFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_MEMORYNUMBERS,
                Boolean.toString(memoryNumbersFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_NINJA, Boolean.toString(ninjaFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_OPENMEMORY, Boolean.toString(openMemoryFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_OPENMEMORYLETTERS,
                Boolean.toString(openMemoryLettersFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_OPENMEMORYNUMBERS,
                Boolean.toString(openMemoryNumbersFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_ORDER, Boolean.toString(orderFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_PET, Boolean.toString(petFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_PORTRAITBUBBLES,
                Boolean.toString(portraitBubblesFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_PIANO, Boolean.toString(pianoFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_POTIONS, Boolean.toString(potionsFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_PUZZLE, Boolean.toString(puzzleFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_ROBOTS, Boolean.toString(robotsFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_ROOM, Boolean.toString(roomFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_RUSHHOUR, Boolean.toString(rushHourFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_SCRATCHCARD,
                Boolean.toString(scratchCardFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_SCRIBBLE, Boolean.toString(scribbleFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_SPACEGAME, Boolean.toString(spaceGameFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_SPOTDIFFERENCE,
                Boolean.toString(spotDifferenceFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_VIDEOPLAYER,
                Boolean.toString(videoPlayerFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_WHACAMOLE, Boolean.toString(whacamoleFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_WHEREISIT, Boolean.toString(whereItFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHEANIMAL,
                Boolean.toString(whereAnimalFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHECOLOR,
                Boolean.toString(whereColorFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHELETTER,
                Boolean.toString(whereLetterFavProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FAVOURITE_WHEREISTHENUMBER,
                Boolean.toString(whereNumberFavProperty.getValue()));

        return properties;
    }

    public void saveConfig() throws IOException {
        Properties properties = toProperties();
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(CONFIGPATH))) {
            String fileComment = "Automatically generated by GazePlay";
            properties.store(fileOutputStream, fileComment);
        }
        // log.info("Properties saved : {}", properties);
    }

    public void saveConfigIgnoringExceptions() {
        try {
            saveConfig();
        } catch (IOException e) {
            log.error("Exception while writing configuration to file {}", CONFIGPATH, e);
        }
    }

    // Simpler getter

    public Boolean getGazeMode() {
        return gazeModeProperty.getValue();
    }

    public String getEyeTracker() {
        return eyetrackerProperty.getValue();
    }

    public String getQuitKey() {
        // System.out.println(QuitKeyProperty.getValue());
        return QuitKeyProperty.getValue();
    }

    public String getLanguage() {
        return languageProperty.getValue();
    }

    public String getFileDir() {
        return filedirProperty.getValue();
    }

    public void setFileDir(String s) {
        filedirProperty.setValue(s);
    }

    public Integer getFixationLength() {
        return fixationlengthProperty.getValue();
    }

    public String getCssFile() {
        return cssfileProperty.getValue();
    }

    public String getWhereIsItDir() {
        return whereIsItDirProperty.getValue();
    }

    public Integer getQuestionLength() {
        return questionLengthProperty.getValue();
    }

    public Boolean isEnableRewardSound() {
        return enableRewardSoundProperty.getValue();
    }

    public String getMenuButtonsOrientation() {
        return menuButtonsOrientationProperty.getValue();
    }

    public Boolean isHeatMapDisabled() {
        return heatMapDisabledProperty.getValue();
    }

    public Boolean isAreaOfInterestEnabled() {
        return areaOfInterestDisabledProperty.getValue();
    }

    public Boolean isConvexHullEnabled() {
        return convexHullDisabledProperty.getValue();
    }

    public Boolean isVideoRecordingEnabled() {
        return videoRecordingDisabledProperty.getValue();
    }

    public Boolean isFixationSequenceDisabled() {
        return fixationSequenceDisabledProperty.getValue();
    }

    public Double getMusicVolume() {
        return musicVolumeProperty.getValue();
    }

    public String getMusicFolder() {
        return musicFolderProperty.getValue();
    }

    public Double getEffectsVolume() {
        return effectsVolumeProperty.getValue();
    }

    public Double getSpeedEffects() {
        double modifVal = speedEffectsProperty.getValue();
        if (modifVal < 4) {
            modifVal = 1 / (5 - modifVal);
        } else {
            modifVal = modifVal - 3;
        }
        return 1 / modifVal;
    }

    public Boolean isGazeMenuEnable() {
        return gazeMenuProperty.getValue();
    }

    public Boolean isGazeMouseEnable() {
        return gazeMouseProperty.getValue();
    }

    public Boolean isBackgroundWhite() {
        return whiteBackgroundProperty.getValue();
    }

    public String getUserName() {
        return userNameProperty.getValue();
    }

    public String getUserPicture() {
        return userPictureProperty.getValue();
    }

    public void setUserName(String newName) {
        userNameProperty.setValue(newName);
    }

    public void setUserPicture(String newPicture) {
        userPictureProperty.setValue(newPicture);
    }

    public Boolean selectionCategory() {
        return selectionCategoryProperty.getValue();
    }

    public Boolean memorizationCategory() {
        return memorizationCategoryProperty.getValue();
    }

    public Boolean actionReactionCategory() {
        return actionReactionCategoryProperty.getValue();
    }

    public Boolean noCategory() {
        return noCategoryProperty.getValue();
    }

    public Boolean logicCategory() {
        return logicCategoryProperty.getValue();
    }

}
