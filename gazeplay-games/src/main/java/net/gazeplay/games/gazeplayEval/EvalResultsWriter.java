package net.gazeplay.games.gazeplayEval;

import com.google.gson.GsonBuilder;
import net.gazeplay.commons.eval.EvalDefinition.ResultFormat;
import net.gazeplay.games.gazeplayEval.EvalSessionRecorder.Event;
import net.gazeplay.games.gazeplayEval.EvalSessionRecorder.Sample;
import net.gazeplay.games.gazeplayEval.EvalSessionRecorder.ScreenRecord;
import net.gazeplay.games.gazeplayEval.EvalSessionRecorder.Selection;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes the results of a session of an evaluation in its folder (format version
 * {@link EvalSessionRecorder#FORMAT_VERSION}):
 * <ul>
 *     <li>session.json: the evaluation, the participant, the settings, the start and end, whether it was completed;</li>
 *     <li>resultats.csv and/or resultats.xlsx (format chosen on the website): one row per screen shown, with for
 *     the grids the cells shown, the expected and given answers;</li>
 *     <li>evenements.csv (and a sheet of resultats.xlsx): every event (entry in and exit from an image, a selection,
 *     a sound, a key of the examiner...);</li>
 *     <li>echantillons.csv: every position of the gaze and of the mouse, with the target under it (always CSV:
 *     there can be many).</li>
 * </ul>
 * The CSV files are in UTF-8 with a byte order mark and use ";" (opened directly by a French spreadsheet). Times in
 * milliseconds since the start of the session, positions in pixels of the game area.
 */
public final class EvalResultsWriter {

    static final String SESSION_FILE = "session.json";
    static final String RESULTS_CSV = "resultats.csv";
    static final String RESULTS_XLSX = "resultats.xlsx";
    static final String EVENTS_CSV = "evenements.csv";
    static final String SAMPLES_CSV = "echantillons.csv";

    static final List<String> RESULTS_HEADER = List.of("Écran", "Essai", "Type", "Début (ms)", "Fin (ms)",
        "Durée (ms)", "Fin de l'écran", "Lignes", "Colonnes", "Règle", "À sélectionner", "Stimuli affichés",
        "Réponses attendues", "Réponses données", "Sources des réponses", "Temps des réponses (ms)",
        "Bonnes réponses", "Mauvaises réponses", "Validé", "Première réponse (ms)");

    static final List<String> EVENTS_HEADER = List.of("Temps (ms)", "Écran", "Essai", "Type d'écran", "Événement",
        "Source", "Cible", "X", "Y", "Détail");

    static final List<String> SAMPLES_HEADER = List.of("Temps (ms)", "Écran", "Essai", "Source", "X", "Y", "Cible");

    private static final String LIST_SEPARATOR = " | ";

    private EvalResultsWriter() {
    }

    /**
     * Writes all the files of a session.
     */
    public static void write(EvalSessionRecorder recorder, ResultFormat format, Path folder) throws IOException {
        Files.createDirectories(folder);
        writeSession(recorder, folder.resolve(SESSION_FILE));
        List<List<String>> results = resultRows(recorder);
        List<List<String>> events = eventRows(recorder);
        if (format.writesCsv()) {
            writeCsv(folder.resolve(RESULTS_CSV), RESULTS_HEADER, results);
            writeCsv(folder.resolve(EVENTS_CSV), EVENTS_HEADER, events);
        }
        if (format.writesXlsx()) {
            writeXlsx(folder.resolve(RESULTS_XLSX), recorder, results, events);
        }
        writeCsv(folder.resolve(SAMPLES_CSV), SAMPLES_HEADER, sampleRows(recorder));
    }

    static void writeSession(EvalSessionRecorder recorder, Path file) throws IOException {
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("formatVersion", EvalSessionRecorder.FORMAT_VERSION);
        session.put("evaluation", recorder.getEvaluationName());
        session.put("debut", iso(recorder.getStartTime()));
        session.put("fin", recorder.getEndTime() >= 0 ? iso(recorder.getStartTime() + recorder.getEndTime()) : null);
        session.put("dureeMs", recorder.getEndTime());
        session.put("terminee", recorder.isCompleted());
        session.put("participant", recorder.getParticipant());
        session.putAll(recorder.getContext());
        session.put("ecransAffiches", recorder.getScreens().size());
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create().toJson(session, writer);
        }
    }

    static List<List<String>> resultRows(EvalSessionRecorder recorder) {
        List<List<String>> rows = new ArrayList<>();
        for (ScreenRecord screen : recorder.getScreens()) {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(screen.getScreen()));
            row.add(String.valueOf(screen.getAttempt()));
            row.add(screen.getType());
            row.add(String.valueOf(screen.getStart()));
            row.add(screen.getEnd() >= 0 ? String.valueOf(screen.getEnd()) : "");
            row.add(screen.getEnd() >= 0 ? String.valueOf(screen.getEnd() - screen.getStart()) : "");
            row.add(screen.getEndReason() == null ? "" : screen.getEndReason().getLabel());
            if (screen.isGrid()) {
                List<Selection> selections = screen.getSelections();
                row.add(String.valueOf(screen.getRows()));
                row.add(String.valueOf(screen.getColumns()));
                row.add(screen.getRule());
                row.add(String.valueOf(screen.getToSelect()));
                row.add(String.join(LIST_SEPARATOR, screen.getLayout()));
                row.add(String.join(LIST_SEPARATOR, screen.getExpected()));
                row.add(selections.stream().map(s -> s.position() + ":" + s.label())
                    .collect(Collectors.joining(LIST_SEPARATOR)));
                row.add(selections.stream().map(Selection::source).collect(Collectors.joining(LIST_SEPARATOR)));
                row.add(selections.stream().map(s -> String.valueOf(s.time() - screen.getStart()))
                    .collect(Collectors.joining(LIST_SEPARATOR)));
                row.add(String.valueOf(screen.correctCount()));
                row.add(String.valueOf(screen.wrongCount()));
                row.add(screen.getEndReason() == EvalSessionRecorder.EndReason.VALIDATED ? "oui" : "non");
                row.add(selections.isEmpty() ? "" : String.valueOf(selections.get(0).time() - screen.getStart()));
            } else {
                while (row.size() < RESULTS_HEADER.size()) {
                    row.add("");
                }
            }
            rows.add(row);
        }
        return rows;
    }

    static List<List<String>> eventRows(EvalSessionRecorder recorder) {
        List<List<String>> rows = new ArrayList<>();
        for (Event event : recorder.getEvents()) {
            rows.add(List.of(String.valueOf(event.time()), String.valueOf(event.screen()), String.valueOf(event.attempt()),
                event.screenType(), event.name(), event.source(), event.target(), position(event.x()),
                position(event.y()), event.detail()));
        }
        return rows;
    }

    static List<List<String>> sampleRows(EvalSessionRecorder recorder) {
        List<List<String>> rows = new ArrayList<>();
        for (Sample sample : recorder.getSamples()) {
            rows.add(List.of(String.valueOf(sample.time()), String.valueOf(sample.screen()),
                String.valueOf(sample.attempt()), sample.source(), position(sample.x()), position(sample.y()),
                sample.target()));
        }
        return rows;
    }

    private static String position(Double value) {
        return value == null ? "" : String.valueOf(Math.round(value));
    }

    private static String iso(long time) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()));
    }

    static void writeCsv(Path file, List<String> header, List<List<String>> rows) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write('﻿');
            writeCsvLine(writer, header);
            for (List<String> row : rows) {
                writeCsvLine(writer, row);
            }
        }
    }

    private static void writeCsvLine(Writer writer, List<String> values) throws IOException {
        List<String> cells = new ArrayList<>(values.size());
        for (String value : values) {
            cells.add(csvCell(value == null ? "" : value));
        }
        writer.write(String.join(";", cells));
        writer.write("\r\n");
    }

    static String csvCell(String value) {
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    static void writeXlsx(Path file, EvalSessionRecorder recorder, List<List<String>> results,
                          List<List<String>> events) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet session = workbook.createSheet("Session");
            int rowIndex = 0;
            rowIndex = addRow(session, rowIndex, List.of("Évaluation", recorder.getEvaluationName()));
            rowIndex = addRow(session, rowIndex, List.of("Début", iso(recorder.getStartTime())));
            rowIndex = addRow(session, rowIndex, List.of("Terminée", recorder.isCompleted() ? "oui" : "non"));
            rowIndex = addRow(session, rowIndex, List.of("Version du format",
                String.valueOf(EvalSessionRecorder.FORMAT_VERSION)));
            for (Map.Entry<String, String> field : recorder.getParticipant().entrySet()) {
                rowIndex = addRow(session, rowIndex, List.of(field.getKey(), field.getValue()));
            }
            for (Map.Entry<String, Object> entry : recorder.getContext().entrySet()) {
                rowIndex = addRow(session, rowIndex, List.of(entry.getKey(), String.valueOf(entry.getValue())));
            }
            addSheet(workbook, "Résultats", RESULTS_HEADER, results);
            addSheet(workbook, "Événements", EVENTS_HEADER, events);
            try (OutputStream output = Files.newOutputStream(file)) {
                workbook.write(output);
            }
        }
    }

    private static void addSheet(Workbook workbook, String name, List<String> header, List<List<String>> rows) {
        Sheet sheet = workbook.createSheet(name);
        int rowIndex = addRow(sheet, 0, header);
        for (List<String> row : rows) {
            rowIndex = addRow(sheet, rowIndex, row);
        }
    }

    /**
     * Adds a row; the integers are written as numbers (so that the spreadsheet can compute on them).
     */
    private static int addRow(Sheet sheet, int rowIndex, List<String> values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i) == null ? "" : values.get(i);
            if (value.matches("-?\\d{1,15}")) {
                row.createCell(i).setCellValue(Long.parseLong(value));
            } else {
                // Excel limits a cell to 32 767 characters
                row.createCell(i).setCellValue(value.length() > 32_000 ? value.substring(0, 32_000) : value);
            }
        }
        return rowIndex + 1;
    }

    /**
     * @return the name of the folder of a session: its date and time
     */
    static String sessionFolderName(long startTime) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.ROOT)
            .format(Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()));
    }
}
