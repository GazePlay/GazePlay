package net.gazeplay.commons.eval;

import java.nio.file.Path;
import java.util.List;

/**
 * An evaluation imported from the GazePlay Eval website: its information (evalInfo.json), its screens (evalData.json)
 * and the folder holding them with its media (images/, audio/, videos/). Read by {@link EvalReader}.
 *
 * @param participantFields labels of the information to fill in about the participant before the evaluation
 */
public record EvalDefinition(Path folder, String name, ResultFormat resultFormat, List<String> participantFields,
                             List<EvalScreen> screens) {

    /**
     * @return a media file of the evaluation
     */
    public Path media(MediaFolder mediaFolder, String fileName) {
        return folder.resolve(mediaFolder.folderName()).resolve(fileName);
    }

    /**
     * The folders of the media in an evaluation.
     */
    public enum MediaFolder {
        IMAGES("images"), AUDIO("audio"), VIDEOS("videos");

        private final String folderName;

        MediaFolder(String folderName) {
            this.folderName = folderName;
        }

        public String folderName() {
            return folderName;
        }
    }

    /**
     * Format of the result files chosen on the website ("Format choisi").
     */
    public enum ResultFormat {
        CSV_AND_XLSX("Csv&Xlsx"), CSV("Csv"), XLSX("Xlsx");

        private final String siteValue;

        ResultFormat(String siteValue) {
            this.siteValue = siteValue;
        }

        public boolean writesCsv() {
            return this != XLSX;
        }

        public boolean writesXlsx() {
            return this != CSV;
        }

        /**
         * @return the format of a value of evalInfo.json; both formats when unknown (also for the former "Xsl")
         */
        public static ResultFormat fromSiteValue(String value) {
            for (ResultFormat format : values()) {
                if (format.siteValue.equalsIgnoreCase(value)) {
                    return format;
                }
            }
            return CSV_AND_XLSX;
        }
    }
}
