package net.gazeplay.commons.eval;

import net.gazeplay.commons.eval.EvalScreen.Cell;
import net.gazeplay.commons.eval.EvalScreen.MediaType;
import net.gazeplay.commons.eval.EvalScreen.SelectionRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reading of the evaluations exported by the GazePlay Eval website. The reference is "Tests sons et vidéos", an
 * evaluation exported by the website (evalData.json and evalInfo.json in the test resources, its media replaced by
 * empty files: only their presence is checked).
 */
class EvalReaderTest {

    @TempDir
    Path folder;

    @Test
    void shouldReadTheReferenceEvaluationOfTheWebsite() throws IOException {
        copyResource("evalData.json");
        copyResource("evalInfo.json");
        for (String image : List.of("oeuf de Pâques.png", "citrouille d'halloween.png", "sapin de Noël.png",
            "ballon de foot.png", "perroquet.png", "médecin(1).png", "colle.png", "balle de tennis.png",
            "raquette de tennis.png")) {
            media("images", image);
        }
        for (String sound : List.of("shut_up_ghost-witchy-cackle-140925.mp3", "alex-morgan-jingle-bells-545481.wav",
            "foule match foot.wav", "soundreality-heart-beat-137135.wav")) {
            media("audio", sound);
        }
        for (String video : List.of("134944-760686132_medium.mp4", "191856-891315505_medium.mp4",
            "205193-926528071_medium.mp4", "49808-458438856_medium.mp4", "224237_tiny.mp4")) {
            media("videos", video);
        }

        EvalReader.Result result = EvalReader.read(folder);

        assertFalse(result.hasErrors(), result.problems().toString());
        assertTrue(result.problems().isEmpty(), result.problems().toString());
        EvalDefinition definition = result.definition();
        assertEquals("Tests sons et vidéos", definition.name());
        assertEquals(EvalDefinition.ResultFormat.CSV_AND_XLSX, definition.resultFormat());
        assertEquals(List.of("Prénom", "Nom"), definition.participantFields());
        assertEquals(17, definition.screens().size());

        EvalScreen.Instruction video = assertInstanceOf(EvalScreen.Instruction.class, definition.screens().get(0));
        assertEquals(MediaType.VIDEO, video.mediaType());
        assertEquals("134944-760686132_medium.mp4", video.content());
        assertEquals(8.0, video.autoAdvanceSeconds());
        assertNull(video.startButtonFixationSeconds());

        EvalScreen.Transition cross = assertInstanceOf(EvalScreen.Transition.class, definition.screens().get(1));
        assertTrue(cross.fixationCross());
        assertNull(cross.crossFixationSeconds());
        assertEquals(2.0, cross.autoAdvanceSeconds());

        EvalScreen.Stimuli grid = assertInstanceOf(EvalScreen.Stimuli.class, definition.screens().get(2));
        assertEquals(2, grid.rows());
        assertEquals(2, grid.columns());
        assertNull(grid.autoAdvanceSeconds());
        assertEquals(2.0, grid.fixationSeconds());
        assertEquals(SelectionRule.ALL, grid.rule());
        assertEquals(1, grid.toSelect());
        assertEquals(new Cell("citrouille d'halloween.png", "shut_up_ghost-witchy-cackle-140925.mp3", true, false),
            grid.cells().get(1));
        assertEquals(new Cell("oeuf de Pâques.png", null, false, false), grid.cells().get(0));

        EvalScreen.Stimuli timed = assertInstanceOf(EvalScreen.Stimuli.class, definition.screens().get(14));
        assertEquals(20.0, timed.autoAdvanceSeconds());
    }

    @Test
    void shouldReadDecimalDurationsAndTheStartButton() throws IOException {
        write("""
            [{"Type": "instruction", "Mettre un temps avant passage à l'écran suivant": true, "Combien de temps": 6.5,
              "Ajouter un media": true, "Type de media": "Texte", "Nom du fichier": "Regarde l'image",
              "Mettre un temps de fixation": true, "Combien de temps de fixation": 1.2},
             {"Type": "transition", "Mettre un temps avant passage à l'écran suivant": false, "Combien de temps": 0,
              "Mettre une croix de fixation": true, "Mettre un temps de fixation": true,
              "Combien de temps de fixation": 0.5}]
            """);

        EvalReader.Result result = EvalReader.read(folder);

        assertFalse(result.hasErrors(), result.problems().toString());
        EvalScreen.Instruction text = (EvalScreen.Instruction) result.definition().screens().get(0);
        assertEquals(6.5, text.autoAdvanceSeconds());
        assertEquals(MediaType.TEXT, text.mediaType());
        assertEquals("Regarde l'image", text.content());
        // "Mettre un temps de fixation" of an instruction: the start button
        assertEquals(1.2, text.startButtonFixationSeconds());
        EvalScreen.Transition transition = (EvalScreen.Transition) result.definition().screens().get(1);
        assertNull(transition.autoAdvanceSeconds());
        assertEquals(0.5, transition.crossFixationSeconds());
    }

    @Test
    void shouldOrderTheCellsAsNumbersAndKeepTheEmptySlots() throws IOException {
        StringBuilder cells = new StringBuilder();
        // 12 cells written in a mixed order; "10" and "11" must come after "9"
        int[] order = {10, 2, 11, 0, 1, 3, 4, 5, 6, 7, 8, 9};
        for (int index : order) {
            if (cells.length() > 0) {
                cells.append(',');
            }
            if (index == 5) {
                // Empty slot of a recent website
                cells.append("\"5\": {\"imageName\": \"\", \"soundName\": \"\", \"goodAnswer\": false, \"hidden\": true}");
            } else if (index == 6) {
                // Cell without media, as the empty slots in the .zip of the website
                cells.append("\"6\": {\"imageName\": \"\", \"soundName\": \"\", \"goodAnswer\": false}");
            } else {
                cells.append('"').append(index).append("\": {\"imageName\": \"i").append(index)
                    .append(".png\", \"soundName\": \"\", \"goodAnswer\": ").append(index == 11).append('}');
                media("images", "i" + index + ".png");
            }
        }
        write("[" + stimuli(3, 4, "\"BonneReponse\"", 1, cells.toString()) + "]");

        EvalReader.Result result = EvalReader.read(folder);

        assertFalse(result.hasErrors(), result.problems().toString());
        EvalScreen.Stimuli grid = (EvalScreen.Stimuli) result.definition().screens().get(0);
        assertEquals(12, grid.cells().size());
        assertEquals("i2.png", grid.cells().get(2).image());
        assertEquals("i9.png", grid.cells().get(9).image());
        assertEquals("i10.png", grid.cells().get(10).image());
        assertTrue(grid.cells().get(11).correct());
        assertTrue(grid.cells().get(5).emptySlot());
        assertTrue(grid.cells().get(6).emptySlot());
        assertEquals(SelectionRule.CORRECT_ONLY, grid.rule());
        assertEquals(1.5, grid.fixationSeconds());
    }

    @Test
    void shouldFillTheMissingCellsWithEmptySlots() throws IOException {
        media("images", "a.png");
        write("[" + stimuli(2, 2, "\"Tout\"", 1,
            "\"0\": {\"imageName\": \"a.png\", \"soundName\": \"\", \"goodAnswer\": true}") + "]");

        EvalReader.Result result = EvalReader.read(folder);

        assertFalse(result.hasErrors(), result.problems().toString());
        EvalScreen.Stimuli grid = (EvalScreen.Stimuli) result.definition().screens().get(0);
        assertEquals(4, grid.cells().size());
        assertTrue(grid.cells().get(3).emptySlot());
        assertTrue(result.warnings().stream().anyMatch(p -> p.key().equals("EvalCellCount")), result.problems().toString());
    }

    @Test
    void shouldRefuseAMissingOrUnreadableMedia() throws IOException {
        media("images", "photo.webp");
        write("[" + stimuli(1, 2, "\"Tout\"", 1,
            "\"0\": {\"imageName\": \"absente.png\", \"soundName\": \"\", \"goodAnswer\": true},"
                + "\"1\": {\"imageName\": \"photo.webp\", \"soundName\": \"\", \"goodAnswer\": false}") + "]");

        EvalReader.Result result = EvalReader.read(folder);

        assertTrue(result.hasErrors());
        assertNull(result.definition());
        assertTrue(result.errors().stream().anyMatch(p -> p.key().equals("EvalMissingMedia")
            && p.args().get(1).equals("images/absente.png")), result.problems().toString());
        assertTrue(result.errors().stream().anyMatch(p -> p.key().equals("EvalUnsupportedMedia")),
            result.problems().toString());
    }

    @Test
    void shouldReportAMissingKeyAndAnUnknownType() throws IOException {
        write("""
            [{"Type": "transition", "Combien de temps": 2},
             {"Type": "carte"}]
            """);

        EvalReader.Result result = EvalReader.read(folder);

        assertTrue(result.hasErrors());
        assertTrue(result.errors().stream().anyMatch(p -> p.key().equals("EvalMissingKey")
            && p.args().equals(List.of("1", "Mettre un temps avant passage à l'écran suivant"))), result.problems().toString());
        assertTrue(result.errors().stream().anyMatch(p -> p.key().equals("EvalUnknownScreenType")),
            result.problems().toString());
    }

    @Test
    void shouldWarnAboutScreensOnlyTheExaminerCanEnd() throws IOException {
        media("images", "a.png");
        write("""
            [{"Type": "transition", "Mettre un temps avant passage à l'écran suivant": false, "Combien de temps": 0,
              "Mettre une croix de fixation": false, "Mettre un temps de fixation": false,
              "Combien de temps de fixation": 0},
            """ + stimuli(1, 1, "\"BonneReponse\"", 1,
            "\"0\": {\"imageName\": \"a.png\", \"soundName\": \"\", \"goodAnswer\": false}") + "]");

        EvalReader.Result result = EvalReader.read(folder);

        assertNotNull(result.definition());
        assertTrue(result.warnings().stream().anyMatch(p -> p.key().equals("EvalScreenWithoutEnd")));
        assertTrue(result.warnings().stream().anyMatch(p -> p.key().equals("EvalGridCannotBeValidated")));
    }

    @Test
    void shouldRefuseAFolderWithoutEvalData() {
        EvalReader.Result result = EvalReader.read(folder);

        assertTrue(result.hasErrors());
        assertEquals("EvalNoData", result.errors().get(0).key());
    }

    @Test
    void shouldFormatAProblemWithItsArguments() {
        EvalProblem problem = EvalProblem.error("EvalMissingMedia", 3, "images/a.png");

        assertEquals("Écran 3 : images/a.png", problem.format("Écran {0} : {1}"));
    }

    private static String stimuli(int rows, int columns, String rule, int toSelect, String cells) {
        return """
            {"Type": "stimuli", "Nombre de lignes": %d, "Nombre de colonnes": %d,
             "Mettre un temps avant passage à l'écran suivant": false, "Combien de temps": 10,
             "Combien de temps de fixation": 1.5, "Choix de sélection": %s, "Combien à sélectionner": %d,
             "Position stimuli aléatoire": false, "Caché stimuli après selection": false, "Mettre un son": false,
             "Nom du fichier": "", "Liste des stimuli": {%s}}
            """.formatted(rows, columns, rule, toSelect, cells);
    }

    private void write(String evalData) throws IOException {
        Files.writeString(folder.resolve("evalData.json"), evalData, StandardCharsets.UTF_8);
    }

    private void media(String mediaFolder, String name) throws IOException {
        Files.createDirectories(folder.resolve(mediaFolder));
        Files.write(folder.resolve(mediaFolder).resolve(name), new byte[0]);
    }

    private void copyResource(String name) throws IOException {
        try (InputStream input = Objects.requireNonNull(getClass().getResourceAsStream("/eval/reference/" + name))) {
            Files.copy(input, folder.resolve(name));
        }
    }
}
