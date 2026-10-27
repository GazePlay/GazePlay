package net.gazeplay.commons.eval;

import java.util.List;

/**
 * A screen of an evaluation created with the GazePlay Eval website, as read from its evalData.json. The durations are
 * in seconds, as on the website; null means "not set" (no automatic end, no fixation required...).
 */
public sealed interface EvalScreen permits EvalScreen.Transition, EvalScreen.Instruction, EvalScreen.Stimuli {

    /**
     * @return the name of the type, as written in evalData.json and in the results ("transition", "instruction",
     * "stimuli")
     */
    String typeName();

    /**
     * @return the delay after which the screen ends by itself, null when it does not
     */
    Double autoAdvanceSeconds();

    /**
     * A pause between two screens, possibly with a fixation cross in the centre.
     *
     * @param crossFixationSeconds fixation of the cross that ends the screen, null when none is required
     */
    record Transition(Double autoAdvanceSeconds, boolean fixationCross, Double crossFixationSeconds) implements EvalScreen {

        @Override
        public String typeName() {
            return "transition";
        }
    }

    /**
     * An instruction given to the participant: a text, an image, a video or a sound.
     *
     * @param mediaType               null when the screen shows no media
     * @param content                 the file name of the media, or the text of the instruction for {@link MediaType#TEXT}
     * @param startButtonFixationSeconds fixation of the start button that ends the screen, null when there is no button.
     *                                In evalData.json this is the key "Mettre un temps de fixation" of the instruction
     */
    record Instruction(Double autoAdvanceSeconds, MediaType mediaType, String content,
                       Double startButtonFixationSeconds) implements EvalScreen {

        @Override
        public String typeName() {
            return "instruction";
        }
    }

    /**
     * A grid of cells (images and/or sounds) that the participant selects by looking at them.
     *
     * @param autoAdvanceSeconds  maximum time on the screen, null when unlimited
     * @param fixationSeconds     fixation that selects a cell
     * @param toSelect            number of selections (counted by {@code rule}) that end the screen
     * @param screenSound         sound played when the screen appears, null when none
     * @param cells               rows x columns cells, row by row
     */
    record Stimuli(int rows, int columns, Double autoAdvanceSeconds, double fixationSeconds, SelectionRule rule,
                   int toSelect, boolean randomPositions, boolean hideAfterSelection, String screenSound,
                   List<Cell> cells) implements EvalScreen {

        @Override
        public String typeName() {
            return "stimuli";
        }
    }

    /**
     * A cell of a grid.
     *
     * @param image     file name in images/, null when none
     * @param sound     file name in audio/, played when the cell is selected, null when none
     * @param correct   whether it is a correct answer
     * @param emptySlot an empty slot of the grid: nothing is shown there (only exported by recent versions of the
     *                  website, as "hidden")
     */
    record Cell(String image, String sound, boolean correct, boolean emptySlot) {

        /**
         * @return the name of the cell in the results: its image, else its sound, else "-"
         */
        public String label() {
            if (image != null) {
                return image;
            }
            return sound != null ? sound : "-";
        }
    }

    /**
     * Type of the media of an instruction, with its value in evalData.json.
     */
    enum MediaType {
        IMAGE("Image"), VIDEO("Video"), TEXT("Texte"), SOUND("Son");

        private final String siteValue;

        MediaType(String siteValue) {
            this.siteValue = siteValue;
        }

        public String siteValue() {
            return siteValue;
        }

        /**
         * @return the type of a value of evalData.json, null when unknown
         */
        public static MediaType fromSiteValue(String value) {
            for (MediaType type : values()) {
                if (type.siteValue.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Which selections count to end a grid ("Choix de sélection").
     */
    enum SelectionRule {
        /** Any selected cell counts ("Tout"). */
        ALL("Tout"),
        /** Only the correct answers count ("BonneReponse"). */
        CORRECT_ONLY("BonneReponse");

        private final String siteValue;

        SelectionRule(String siteValue) {
            this.siteValue = siteValue;
        }

        public String siteValue() {
            return siteValue;
        }

        /**
         * @return the rule of a value of evalData.json, null when unknown
         */
        public static SelectionRule fromSiteValue(String value) {
            for (SelectionRule rule : values()) {
                if (rule.siteValue.equalsIgnoreCase(value)) {
                    return rule;
                }
            }
            return null;
        }
    }
}
