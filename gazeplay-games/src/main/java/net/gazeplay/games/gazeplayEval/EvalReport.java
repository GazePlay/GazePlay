package net.gazeplay.games.gazeplayEval;

import java.util.ArrayList;
import java.util.List;

/**
 * Results of a GazePlay Eval session, and their two export formats (CSV and rows of the Excel sheet).
 * <p>
 * Kept free of JavaFX and of the game state so that the exports can be tested. The layout of both exports is a
 * persisted format read by the users of the evaluations: change it only deliberately.
 *
 * @param evaluationName      name of the evaluation (EvalName of config.json)
 * @param date                formatted date of the session
 * @param durationMillis      duration of the session, in milliseconds
 * @param nbImages            number of images of the evaluation
 * @param nbSounds            number of sounds of the evaluation
 * @param itemsAddedManually  number of items marked as correct with the keyboard
 * @param anonymous           when false, the profile of the person is exported
 * @param profile             last name, first name, gender, age, birth date, birth place
 * @param nbItemsPlayed       number of items played (the evaluation stops after 5 consecutive errors)
 * @param nbImagesPerItem     number of images displayed for each item
 * @param images              names of the images, by item
 * @param imagesDescription   descriptions of the images, by item
 * @param sounds              name of the sound of each item
 * @param soundsDescription   description of the sound of each item
 * @param results             result of each item
 * @param scoreNames          names of the scores
 * @param scores              value of each score
 * @param maxScores           maximum value of each score
 */
record EvalReport(
    String evaluationName,
    String date,
    long durationMillis,
    int nbImages,
    int nbSounds,
    int itemsAddedManually,
    boolean anonymous,
    String[] profile,
    int nbItemsPlayed,
    int nbImagesPerItem,
    String[][] images,
    String[][] imagesDescription,
    String[] sounds,
    String[] soundsDescription,
    String[] results,
    String[] scoreNames,
    int[] scores,
    int[] maxScores
) {

    private static final String NEW_LINE = "\r\n";

    /**
     * The duration used to be divided by 100 while labelled in seconds, i.e. exported in tenths of seconds (anomaly
     * R8): the values of the files exported before must be divided by 10.
     *
     * @return the duration in seconds, e.g. "12.345" for 12345 ms
     */
    static String formatDurationInSeconds(final long durationMillis) {
        return String.valueOf(durationMillis / 1000.);
    }

    String toCsv() {
        final StringBuilder out = new StringBuilder();
        out.append(NEW_LINE);
        out.append("Nom de l'évaluation : ").append(evaluationName).append(NEW_LINE);
        out.append("Fait le ").append(date).append(NEW_LINE);
        out.append("Temps de l'évaluation : ").append(formatDurationInSeconds(durationMillis)).append(" secondes ").append(NEW_LINE);
        out.append("Nombre d'images: ").append(nbImages).append(NEW_LINE);
        out.append("Number de sons : ").append(nbSounds).append(NEW_LINE);
        out.append("Nombre d'items ajoutés manuellement : ").append(itemsAddedManually).append(NEW_LINE);
        out.append(NEW_LINE);
        if (!anonymous) {
            out.append("Profil de la personne : ").append(NEW_LINE);
            out.append("Nom : ").append(profile[0]).append(NEW_LINE);
            out.append("Prénom : ").append(profile[1]).append(NEW_LINE);
            out.append("Genre : ").append(profile[2]).append(NEW_LINE);
            out.append("Age : ").append(profile[3]).append(NEW_LINE);
            out.append("Date de naissance : ").append(profile[4]).append(NEW_LINE);
            out.append("Lieu de naissance : ").append(profile[5]).append(NEW_LINE);
            out.append(NEW_LINE);
        }
        for (int i = 0; i < nbItemsPlayed; i++) {
            out.append("Item ").append(i + 1).append(NEW_LINE);
            for (int j = 0; j < nbImagesPerItem; j++) {
                out.append("- Nom de l'image -> ").append(images[i][j]).append(NEW_LINE);
                out.append("- Description de l'image -> ").append(imagesDescription[i][j]).append(NEW_LINE);
            }
            out.append("- Son utilisé -> ").append(sounds[i]).append(NEW_LINE);
            out.append("- Description du son -> ").append(soundsDescription[i]).append(NEW_LINE);
            out.append("- Résultat -> ").append(results[i]).append(NEW_LINE);
            out.append(NEW_LINE);
        }
        out.append("Scores ").append(NEW_LINE);
        for (int k = 0; k < scoreNames.length; k++) {
            out.append("- ").append(scoreNames[k]).append(" -> ").append(scores[k]).append(" / ").append(maxScores[k]).append(NEW_LINE);
        }
        return out.toString();
    }

    /**
     * Rows of the Excel sheet: a label and a value, except the "Scores" title. The rows used to be written into an
     * array sized for the anonymous layout, which made the export fail when the profile was exported (anomaly R10).
     *
     * @return the rows, in order
     */
    List<String[]> toExcelRows() {
        final List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Nom de l'évaluation : ", evaluationName});
        rows.add(new String[]{"Fait le : ", date});
        rows.add(new String[]{"Temps de l'évaluation: ", formatDurationInSeconds(durationMillis) + " secondes"});
        rows.add(new String[]{"Nombre d'images : ", String.valueOf(nbImages)});
        rows.add(new String[]{"Nombre de sons : ", String.valueOf(nbSounds)});
        rows.add(new String[]{"Nombre d'items ajoutés manuellement : ", String.valueOf(itemsAddedManually)});
        if (!anonymous) {
            rows.add(new String[]{"", ""});
            rows.add(new String[]{"Profil de la personne : ", ""});
            rows.add(new String[]{"Nom : ", profile[0]});
            rows.add(new String[]{"Prénom : ", profile[1]});
            rows.add(new String[]{"Genre : ", profile[2]});
            rows.add(new String[]{"Age : ", profile[3]});
            rows.add(new String[]{"Date de naissance : ", profile[4]});
            rows.add(new String[]{"Lieu de naissance : ", profile[5]});
        }
        for (int i = 0; i < nbItemsPlayed; i++) {
            rows.add(new String[]{"", ""});
            rows.add(new String[]{"Item " + (i + 1), ""});
            for (int k = 0; k < nbImagesPerItem; k++) {
                rows.add(new String[]{"- Nom de l'image -> ", images[i][k]});
                rows.add(new String[]{"- Description de l'image -> ", imagesDescription[i][k]});
            }
            rows.add(new String[]{"- Son utilisé -> ", sounds[i]});
            rows.add(new String[]{"- Description du son -> ", soundsDescription[i]});
            rows.add(new String[]{"- Résultat -> ", results[i]});
        }
        rows.add(new String[]{"", ""});
        rows.add(new String[]{"Scores"});
        for (int l = 0; l < scoreNames.length; l++) {
            rows.add(new String[]{"- " + scoreNames[l] + " -> ", scores[l] + " / " + maxScores[l]});
        }
        return rows;
    }
}
