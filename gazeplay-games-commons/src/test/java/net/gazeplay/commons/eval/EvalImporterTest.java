package net.gazeplay.commons.eval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Import of the .zip exported by the GazePlay Eval website: nothing is put in place before {@link EvalImporter#commit},
 * and an archive cannot write outside of the folder of the evaluation.
 */
class EvalImporterTest {

    private static final String EVAL_DATA = """
        [{"Type": "stimuli", "Nombre de lignes": 1, "Nombre de colonnes": 1,
          "Mettre un temps avant passage à l'écran suivant": true, "Combien de temps": 5,
          "Combien de temps de fixation": 1, "Choix de sélection": "Tout", "Combien à sélectionner": 1,
          "Position stimuli aléatoire": false, "Caché stimuli après selection": false, "Mettre un son": false,
          "Nom du fichier": "",
          "Liste des stimuli": {"0": {"imageName": "œuf de Pâques.png", "soundName": "", "goodAnswer": true}}}]
        """;

    private static final String EVAL_INFO = """
        {"Nom de l'évaluation": "Évaluation été", "Format choisi": "Csv", "Informations participant": ["Âge"]}
        """;

    @TempDir
    Path temp;

    @Test
    void shouldImportTheZipOfTheWebsite() throws IOException {
        Path evaluations = temp.resolve("evaluations");
        Path zip = zip("Évaluation été-gazeplayEval.zip", Map.of(
            "Évaluation été/evalData.json", EVAL_DATA,
            "Évaluation été/evalInfo.json", EVAL_INFO,
            "Évaluation été/images/œuf de Pâques.png", "png"));

        EvalImporter.Prepared prepared = EvalImporter.prepare(zip, evaluations);

        assertFalse(prepared.hasErrors(), prepared.problems().toString());
        assertEquals("Évaluation été", prepared.name());
        assertFalse(prepared.replacesExisting());
        // Not in place yet: only the temporary folder exists
        assertEquals(java.util.List.of(), EvalLibrary.names(evaluations));

        Path imported = EvalImporter.commit(prepared);

        assertEquals(java.util.List.of("Évaluation été"), EvalLibrary.names(evaluations));
        assertTrue(Files.isRegularFile(imported.resolve("images").resolve("œuf de Pâques.png")));
        EvalReader.Result result = EvalReader.read(imported);
        assertEquals(EvalDefinition.ResultFormat.CSV, result.definition().resultFormat());
        assertEquals(java.util.List.of("Âge"), result.definition().participantFields());
    }

    @Test
    void shouldReplaceAnEvaluationOfTheSameNameOnlyWhenCommitted() throws IOException {
        Path evaluations = temp.resolve("evaluations");
        Path old = evaluations.resolve("Évaluation été");
        Files.createDirectories(old);
        Files.writeString(old.resolve("evalData.json"), "[]");
        Files.writeString(old.resolve("ancien.txt"), "x");
        Path zip = zip("eval.zip", Map.of(
            "Évaluation été/evalData.json", EVAL_DATA,
            "Évaluation été/images/œuf de Pâques.png", "png"));

        EvalImporter.Prepared prepared = EvalImporter.prepare(zip, evaluations);
        assertTrue(prepared.replacesExisting());
        EvalImporter.discard(prepared);
        assertTrue(Files.exists(old.resolve("ancien.txt")), "Cancelled: the former evaluation is kept");

        EvalImporter.commit(EvalImporter.prepare(zip, evaluations));
        assertFalse(Files.exists(old.resolve("ancien.txt")));
        assertTrue(Files.isRegularFile(old.resolve("images").resolve("œuf de Pâques.png")));
        try (var children = Files.list(evaluations)) {
            assertEquals(1, children.count(), "No temporary folder left");
        }
    }

    @Test
    void shouldReportTheErrorsOfTheArchive() throws IOException {
        Path evaluations = temp.resolve("evaluations");
        Path zip = zip("eval.zip", Map.of("Eval/evalData.json", EVAL_DATA));

        EvalImporter.Prepared prepared = EvalImporter.prepare(zip, evaluations);

        assertTrue(prepared.hasErrors());
        assertTrue(prepared.problems().stream().anyMatch(p -> p.key().equals("EvalMissingMedia")));
        assertThrows(IOException.class, () -> EvalImporter.commit(prepared));
        EvalImporter.discard(prepared);
        try (var children = Files.list(evaluations)) {
            assertEquals(0, children.count());
        }
    }

    @Test
    void shouldReadAnArchiveWithoutRootFolder() throws IOException {
        Path zip = zip("Mon évaluation-gazeplayEval.zip", Map.of(
            "evalData.json", EVAL_DATA,
            "evalInfo.json", EVAL_INFO,
            "images/œuf de Pâques.png", "png",
            "__MACOSX/._evalData.json", "x"));

        EvalImporter.Prepared prepared = EvalImporter.prepare(zip, temp.resolve("evaluations"));

        assertFalse(prepared.hasErrors(), prepared.problems().toString());
        assertEquals("Évaluation été", prepared.name());
    }

    @Test
    void shouldRefuseAnArchiveWithoutEvaluation() throws IOException {
        Path zip = zip("other.zip", Map.of("readme.txt", "x"));

        assertThrows(IOException.class, () -> EvalImporter.prepare(zip, temp.resolve("evaluations")));
    }

    @Test
    void shouldRefuseAnEntryOutsideOfTheEvaluation() throws IOException {
        Path evaluations = temp.resolve("evaluations");
        Path zip = zip("evil.zip", Map.of(
            "Eval/evalData.json", EVAL_DATA,
            "Eval/../../evil.txt", "x"));

        assertThrows(IOException.class, () -> EvalImporter.prepare(zip, evaluations));
        assertFalse(Files.exists(temp.resolve("evil.txt")));
        try (var children = Files.list(evaluations)) {
            assertEquals(0, children.count(), "The temporary folder is deleted");
        }
    }

    @Test
    void shouldMakeNamesValidOnAllSystems() {
        assertEquals("Eval 1", EvalImporter.sanitize("..Eval: 1?"));
        assertEquals("", EvalImporter.sanitize("..."));
    }

    private Path zip(String name, Map<String, String> entries) throws IOException {
        Path zip = temp.resolve(name);
        try (OutputStream output = Files.newOutputStream(zip);
             ZipOutputStream zipOutput = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> entry : new LinkedHashMap<>(entries).entrySet()) {
                zipOutput.putNextEntry(new ZipEntry(entry.getKey()));
                zipOutput.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zipOutput.closeEntry();
            }
        }
        return zip;
    }
}
