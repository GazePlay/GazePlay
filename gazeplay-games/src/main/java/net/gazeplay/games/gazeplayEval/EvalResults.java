package net.gazeplay.games.gazeplayEval;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.games.gazeplayEval.config.*;
import net.gazeplay.games.gazeplayEval.round.RoundResults;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static net.gazeplay.games.gazeplayEval.config.QuestionType.*;
import static net.gazeplay.games.gazeplayEval.config.ResultsOutput.*;
import static net.gazeplay.games.gazeplayEval.config.ResultsOutputType.*;

@Slf4j
class EvalResults {
    private final List<RoundResults> roundResultsList = new ArrayList<>();
    public long startTime;

    private final HashMap<String, String> computedMainResults = new HashMap<>();
    private final List<HashMap<String, String>> computedItemsResults = new ArrayList<>();

    public void clear() {
        roundResultsList.clear();
        computedMainResults.clear();
        computedItemsResults.clear();
    }

    public void add(RoundResults roundResults) {
        roundResultsList.add(roundResults);
    }

    public void autoExport() {
        final String formattedName = GameState.stats.getGameStatsOfTheDayDirectory() + "\\" +
            GameState.eval.getConfig().getName() + "-" + DateUtils.dateTimeNow();
        this.computeResults();
        try {
            switch (GameState.eval.getConfig().getOutputType()) {
                case CSV -> this.exportToCSV(new File(formattedName + CSV.getExtension()));
                case XLS -> this.exportToExcel(new File(formattedName + XLS.getExtension()));
                case ALL -> {
                    this.exportToCSV(new File(formattedName + CSV.getExtension()));
                    this.exportToExcel(new File(formattedName + XLS.getExtension()));
                    // Excel last, to be the one chosen when user clicks to get stats
                }
                default -> log.warn("No Output set or wrong statement");
            }
        } catch (Exception e) {
            log.error("Exception while exporting the results: ", e);
        }
    }

    private void computeResults() {
        EvalConfig config = GameState.eval.getConfig();

        computedMainResults.put(EVAL_NAME, config.getName());
        computedMainResults.put(EVAL_PATIENT_ID, config.getPatientId());
        computedMainResults.put(EVAL_DATE, new SimpleDateFormat("dd/MM/yyyy").format(new Date(startTime)));
        computedMainResults.put(EVAL_TIME, new SimpleDateFormat("HH:mm").format(new Date(startTime)));
        computedMainResults.put(EVAL_DURATION, Math.round(GameState.stats.timeGame / 10f) / 100f + "s");
        computedMainResults.put(EVAL_ITEMS_COUNT, String.valueOf(config.getItemsCount()));
        computedMainResults.put(EVAL_PICTURES_COUNT, String.valueOf(roundResultsList.stream().mapToInt(RoundResults::getPicturesCount).sum()));
        computedMainResults.put(EVAL_SOUNDS_COUNT, String.valueOf(config.getItems().mapToInt(it -> it.getQuestionType() == AUDIO ? 1 : 0).sum()));

        for (int i = 0; i < config.getItemsCount(); i++) {
            HashMap<String, String> itemComputedResults = new HashMap<>();
            final ItemConfig iConfig = config.getItem(i);

            itemComputedResults.put(ITEM_NUMBER, String.valueOf(i + 1)); // Numéro d'item
            itemComputedResults.put(ITEM_PICTURES_COUNT, String.valueOf(roundResultsList.get(i).getPicturesCount())); // Nombre d'images
            itemComputedResults.put(ITEM_PICTURES_TO_SELECT_COUNT, String.valueOf(iConfig.getSelectionsRequired())); // Nombres d'images à sélectionner
            itemComputedResults.put(ITEM_QUESTION, config.getItem(i).getQuestionText()); // Question posée
            if (config.getItem(i).getQuestionType() == QuestionType.TEXT)
                itemComputedResults.put(ITEM_QUESTION, "\"" + itemComputedResults.get(ITEM_QUESTION) + "\"");
            itemComputedResults.put(ITEM_LIMIT_TIME, Math.round(iConfig.getTimeLimit() / 10f) / 100f + "s"); // Temps limite, 2 chiffres après la virgule
            itemComputedResults.put(ITEM_RESPONSE_TIME, Math.round(roundResultsList.get(i).getTimeRound() / 10f) / 100f + "s"); // Durée de réponse
            itemComputedResults.put(ITEM_PICTURES_SELECTED, roundResultsList.get(i).getSelectedPictures().stream().map(
                pictureCoord -> iConfig.getGrid(pictureCoord.getKey(), pictureCoord.getValue()).getName()
            ).toList().toString()); // Images sélectionnées

            computedItemsResults.add(itemComputedResults);
        }
    }

    private void exportToExcel(File outputPath) throws Exception {
        GameState.stats.actualFile = outputPath.getPath();

        try (Workbook book = new HSSFWorkbook()) {
            Sheet mainSheet = book.createSheet("Résultats de l'évaluation");
            Sheet itemsSheet = book.createSheet("Détail des items");

            Row mainResultsHeader = mainSheet.createRow(0);
            Row mainResultsRow = mainSheet.createRow(1);
            int i = 0;
            for (String name : ResultsOutput.eval_values()) {
                mainResultsHeader.createCell(i).setCellValue(name);
                mainResultsRow.createCell(i).setCellValue(computedMainResults.get(name));
                mainSheet.autoSizeColumn(i++);
            }

            Row itemsResultsHeader = itemsSheet.createRow(0);
            i = 0;
            for (String name : ResultsOutput.item_values())
                itemsResultsHeader.createCell(i++).setCellValue(name);

            int j = 1;
            for (HashMap<String, String> itemResults : computedItemsResults) {
                Row itemResultsRow = itemsSheet.createRow(j++);
                i = 0;
                for (String name : ResultsOutput.item_values()) {
                    itemResultsRow.createCell(i).setCellValue(itemResults.get(name));
                    // Maybe change dynamically the type of the cell?
                    itemsSheet.autoSizeColumn(i++);
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                book.write(fileOut);
            }
        }
    }

    private void exportToCSV(File outputPath) throws Exception {
        GameState.stats.actualFile = outputPath.getPath();

        // Setting up export.line(), where each argument is a string to be written in a different column
        PrintWriter out = new PrintWriter(outputPath, StandardCharsets.UTF_16);
        interface CSVexporter {
            void line(String... values);
        } CSVexporter export = (String... values) -> {
            for (int i = 0; i < values.length; i++) {
                if (i > 0)
                    out.append(", ");
                out.append("\"").append(values[i].translateEscapes()).append("\"");
            }
            out.append("\r\n");
        };

        export.line();
        for (String name : ResultsOutput.eval_values())
            export.line(name, computedMainResults.get(name));

        export.line();
        export.line(Stream.of(ResultsOutput.item_values()).toArray(String[]::new));
        for (HashMap<String, String> itemResults : computedItemsResults)
            export.line(Stream.of(ResultsOutput.item_values()).map(itemResults::get).toArray(String[]::new));
        out.close();
    }
}
