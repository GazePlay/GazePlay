package net.gazeplay.games.gazeplayEval.config;

public class ResultsOutput {
    public final static String EVAL_NAME = "Nom de l'évaluation";
    public final static String EVAL_PATIENT_ID = "ID du patient";
    public final static String EVAL_DATE = "Fait le";
    public final static String EVAL_TIME = "Heure de l'évaluation";
    public final static String EVAL_DURATION = "Durée de l'évaluation";
    public final static String EVAL_ITEMS_COUNT = "Nombre d'items";
    public final static String EVAL_PICTURES_COUNT = "Nombre total d'images";
    public final static String EVAL_SOUNDS_COUNT = "Nombre total de sons";

    public final static String ITEM_NUMBER = "Numéro d'item";
    public final static String ITEM_PICTURES_COUNT = "Nombre d'images";
    public final static String ITEM_PICTURES_TO_SELECT_COUNT = "Nombres d'images sélectionnées";
    public final static String ITEM_QUESTION = "Question posée (audio ou texte)";
    public final static String ITEM_LIMIT_TIME = "Durée limite";
    public final static String ITEM_RESPONSE_TIME = "Durée de réponse";
    public final static String ITEM_PICTURES_SELECTED = "Images sélectionnées";


    // EVAL values in correct order
    public static String[] eval_values() {
        return new String[] {
            EVAL_NAME,
            EVAL_PATIENT_ID,
            EVAL_DATE,
            EVAL_TIME,
            EVAL_DURATION,
            EVAL_ITEMS_COUNT,
            EVAL_PICTURES_COUNT,
            EVAL_SOUNDS_COUNT
        };
    }

    // ITEM values in correct order
    public static String[] item_values() {
        return new String[] {
            ITEM_NUMBER,
            ITEM_PICTURES_COUNT,
            ITEM_PICTURES_TO_SELECT_COUNT,
            ITEM_QUESTION,
            ITEM_LIMIT_TIME,
            ITEM_RESPONSE_TIME,
            ITEM_PICTURES_SELECTED
        };
    }
}
