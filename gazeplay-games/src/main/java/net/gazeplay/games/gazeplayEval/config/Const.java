package net.gazeplay.games.gazeplayEval.config;

import net.gazeplay.commons.configuration.ActiveConfigurationContext;

public class Const {
    public final static String ROOT_DIRECTORY = ActiveConfigurationContext.getInstance().getFileDir() + "\\evals\\";
    public final static String IMAGES_LOCATION = "images\\";
    public final static String AUDIO_LOCATION = "sounds\\";
    public final static String CONFIG_LOCATION = "config.json";

    public final static String EVAL_NAME = "eval_name";
    public final static String EVAL_OUTPUT_TYPE = "results_output";
    public final static String EVAL_PATIENT_ID = "patient_id";
    public final static String EVAL_ITEMS = "items";

    public final static String ITEM_ROWS = "nb_rows";
    public final static String ITEM_COLS = "nb_cols";
    public final static String ITEM_GRID = "image_grid";
    public final static String ITEM_RANDOMIZE_GRID = "randomize_grid";
    public final static String ITEM_SELECTIONS = "nb_selections_required";
    public final static String ITEM_AUDIO_FILENAME = "audio_file";
    public final static String ITEM_AUDIO_SCHEDULE = "audio_schedule";
    public final static String ITEM_GAZE_TIME = "fixation_length";
    public final static String ITEM_TIME_LIMIT = "item_time_limit";

    public final static int SELECTION_PROGRESS_MIN_WIDTH = 75;
    public final static int SELECTION_PROGRESS_MIN_HEIGHT = 75;
    public final static double SELECTION_PROGRESS_OPACITY = 0.5;
}
