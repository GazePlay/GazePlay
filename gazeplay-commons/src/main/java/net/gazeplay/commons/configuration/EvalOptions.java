package net.gazeplay.commons.configuration;

/**
 * How the evaluations of GazePlay Eval are taken (EVAL_OPTIONS setting): after each grid, a screen can offer to do it
 * again or to continue. The names are the values of the setting (those of the GazePlayLearning branch).
 */
public enum EvalOptions {

    /** The screens follow each other. */
    Classique,
    /** After each grid, the examiner chooses to do it again or to continue (mouse or keyboard). */
    Autonomie_Soutien_Oral,
    /** After each grid, the participant points out a choice with the gaze, the examiner confirms it. */
    Autonomie_Soutien_Visuel,
    /** After each grid, the participant chooses with the gaze. */
    Autonomie_Total;

    /**
     * @return the translation key of the name of the mode
     */
    public String translationKey() {
        return "EvalOptions_" + name();
    }

    /**
     * @return the mode of a value of the setting, {@link #Classique} when unknown
     */
    public static EvalOptions fromConfigValue(String value) {
        for (EvalOptions option : values()) {
            if (option.name().equals(value)) {
                return option;
            }
        }
        return Classique;
    }
}
