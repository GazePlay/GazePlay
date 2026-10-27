package net.gazeplay.games.gazeplayEval;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gazeplay.commons.eval.EvalDefinition.ResultFormat;
import net.gazeplay.games.gazeplayEval.EvalSessionRecorder.EndReason;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Files of results of a session of GazePlay Eval (format version 1): their columns are a format read by researchers,
 * pinned here.
 */
class EvalResultsWriterTest {

    @TempDir
    Path folder;

    private final AtomicLong clock = new AtomicLong(1_700_000_000_000L);

    private EvalSessionRecorder session() {
        EvalSessionRecorder recorder = new EvalSessionRecorder("Couleurs", clock::get);
        recorder.getParticipant().put("Prénom", "Léa");
        recorder.getParticipant().put("Âge", "7");
        recorder.getContext().put("modePassation", "Classique");

        clock.addAndGet(100);
        recorder.beginScreen(1, 1, "transition");
        clock.addAndGet(2000);
        recorder.endScreen(EndReason.DELAY_ELAPSED);

        recorder.beginScreen(2, 1, "stimuli");
        recorder.describeGrid(1, 2, "Tout", 1, List.of("L1C1:rouge.png", "L1C2:bleu.png"), List.of("L1C2:bleu.png"));
        clock.addAndGet(300);
        recorder.eventAt(recorder.elapsed(), "entree", EvalSessionRecorder.GAZE, "L1C1:rouge.png", 120.4, 80.0, "");
        recorder.sample(recorder.elapsed(), EvalSessionRecorder.GAZE, 120.4, 80.0, "L1C1:rouge.png");
        clock.addAndGet(1500);
        recorder.select("L1C1", "rouge.png", false, EvalSessionRecorder.GAZE, true, 120, 80);
        recorder.endScreen(EndReason.VALIDATED);
        clock.addAndGet(10);
        recorder.finish(true);
        return recorder;
    }

    @Test
    void shouldWriteTheResultsInCsvAndXlsx() throws IOException {
        EvalResultsWriter.write(session(), ResultFormat.CSV_AND_XLSX, folder);

        List<String> results = Files.readAllLines(folder.resolve("resultats.csv"), StandardCharsets.UTF_8);
        assertTrue(results.get(0).startsWith("﻿Écran;Essai;Type;Début (ms);Fin (ms);Durée (ms);Fin de l'écran"),
            results.get(0));
        assertEquals(3, results.size());
        assertEquals("1;1;transition;100;2100;2000;délai écoulé;;;;;;;;;;;;;", results.get(1));
        assertEquals("2;1;stimuli;2100;3900;1800;validé;1;2;Tout;1;L1C1:rouge.png | L1C2:bleu.png;L1C2:bleu.png;"
            + "L1C1:rouge.png;regard;1800;0;1;oui;1800", results.get(2));

        List<String> events = Files.readAllLines(folder.resolve("evenements.csv"), StandardCharsets.UTF_8);
        assertTrue(events.contains("2400;2;1;stimuli;entree;regard;L1C1:rouge.png;120;80;"), events.toString());
        assertTrue(events.contains("3900;2;1;stimuli;selection;regard;L1C1:rouge.png;120;80;mauvaise réponse"),
            events.toString());
        assertTrue(events.get(events.size() - 1).startsWith("3910;2;1;stimuli;fin_session;systeme"));

        List<String> samples = Files.readAllLines(folder.resolve("echantillons.csv"), StandardCharsets.UTF_8);
        assertEquals("2400;2;1;regard;120;80;L1C1:rouge.png", samples.get(1));

        JsonObject session;
        try (var reader = Files.newBufferedReader(folder.resolve("session.json"), StandardCharsets.UTF_8)) {
            session = JsonParser.parseReader(reader).getAsJsonObject();
        }
        assertEquals(1, session.get("formatVersion").getAsInt());
        assertEquals("Couleurs", session.get("evaluation").getAsString());
        assertTrue(session.get("terminee").getAsBoolean());
        assertEquals("Léa", session.getAsJsonObject("participant").get("Prénom").getAsString());
        assertEquals("Classique", session.get("modePassation").getAsString());

        try (InputStream input = Files.newInputStream(folder.resolve("resultats.xlsx"));
             Workbook workbook = new XSSFWorkbook(input)) {
            assertEquals(List.of("Session", "Résultats", "Événements"),
                List.of(workbook.getSheetName(0), workbook.getSheetName(1), workbook.getSheetName(2)));
            Sheet results1 = workbook.getSheet("Résultats");
            assertEquals("Écran", results1.getRow(0).getCell(0).getStringCellValue());
            assertEquals(2, results1.getRow(2).getCell(0).getNumericCellValue());
            assertEquals("validé", results1.getRow(2).getCell(6).getStringCellValue());
        }
    }

    @Test
    void shouldWriteOnlyTheChosenFormat() throws IOException {
        EvalResultsWriter.write(session(), ResultFormat.XLSX, folder);

        assertTrue(Files.exists(folder.resolve("resultats.xlsx")));
        assertFalse(Files.exists(folder.resolve("resultats.csv")));
        assertFalse(Files.exists(folder.resolve("evenements.csv")));
        // The raw positions are always written in CSV
        assertTrue(Files.exists(folder.resolve("echantillons.csv")));
        assertTrue(Files.exists(folder.resolve("session.json")));
    }

    @Test
    void shouldMarkAnInterruptedSession() throws IOException {
        EvalSessionRecorder recorder = new EvalSessionRecorder("Couleurs", clock::get);
        recorder.beginScreen(1, 1, "instruction");
        clock.addAndGet(500);
        recorder.finish(false);
        EvalResultsWriter.write(recorder, ResultFormat.CSV, folder);

        List<String> results = Files.readAllLines(folder.resolve("resultats.csv"), StandardCharsets.UTF_8);
        assertEquals("1;1;instruction;0;500;500;interrompu;;;;;;;;;;;;;", results.get(1));
        assertTrue(Files.readString(folder.resolve("session.json")).contains("\"terminee\": false"));
    }

    @Test
    void shouldQuoteTheCellsWithSeparators() {
        assertEquals("\"a;b\"", EvalResultsWriter.csvCell("a;b"));
        assertEquals("\"dit \"\"oui\"\"\"", EvalResultsWriter.csvCell("dit \"oui\""));
        assertEquals("simple", EvalResultsWriter.csvCell("simple"));
    }
}
