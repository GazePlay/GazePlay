package net.gazeplay.games.gazeplayEval;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EvalReportTest {

    private static final String[] PROFILE = {"Dupont", "Marie", "F", "7", "01/02/2019", "Grenoble"};

    /**
     * Two items played, two scores. The second item has no result (the evaluation stopped), which is exported as
     * "null".
     */
    private static EvalReport report(boolean anonymous) {
        return new EvalReport("Eval test", "20 octobre 2026 à 14:37:05", 12345, 4, 2, 1, anonymous, PROFILE,
            2, 2,
            new String[][]{{"chat.png", "chien.png"}, {"pomme.png", "poire.png"}},
            new String[][]{{"un chat", "un chien"}, {"une pomme", "une poire"}},
            new String[]{"chat.mp3", "pomme.mp3"},
            new String[]{"miaou", "croque"},
            new String[]{"correct", null},
            new String[]{"Animaux", "Fruits"},
            new int[]{1, 0},
            new int[]{1, 1});
    }

    @Test
    void shouldExportAnonymousCsv() {
        String expected = "\r\n"
            + "Nom de l'évaluation : Eval test\r\n"
            + "Fait le 20 octobre 2026 à 14:37:05\r\n"
            + "Temps de l'évaluation : 12.345 secondes \r\n"
            + "Nombre d'images: 4\r\n"
            + "Number de sons : 2\r\n"
            + "Nombre d'items ajoutés manuellement : 1\r\n"
            + "\r\n"
            + "Item 1\r\n"
            + "- Nom de l'image -> chat.png\r\n"
            + "- Description de l'image -> un chat\r\n"
            + "- Nom de l'image -> chien.png\r\n"
            + "- Description de l'image -> un chien\r\n"
            + "- Son utilisé -> chat.mp3\r\n"
            + "- Description du son -> miaou\r\n"
            + "- Résultat -> correct\r\n"
            + "\r\n"
            + "Item 2\r\n"
            + "- Nom de l'image -> pomme.png\r\n"
            + "- Description de l'image -> une pomme\r\n"
            + "- Nom de l'image -> poire.png\r\n"
            + "- Description de l'image -> une poire\r\n"
            + "- Son utilisé -> pomme.mp3\r\n"
            + "- Description du son -> croque\r\n"
            + "- Résultat -> null\r\n"
            + "\r\n"
            + "Scores \r\n"
            + "- Animaux -> 1 / 1\r\n"
            + "- Fruits -> 0 / 1\r\n";

        assertEquals(expected, report(true).toCsv());
    }

    @Test
    void shouldExportTheProfileInNominativeCsv() {
        String csv = report(false).toCsv();

        assertEquals(report(true).toCsv().replace("Nombre d'items ajoutés manuellement : 1\r\n\r\n",
            "Nombre d'items ajoutés manuellement : 1\r\n\r\n"
                + "Profil de la personne : \r\n"
                + "Nom : Dupont\r\n"
                + "Prénom : Marie\r\n"
                + "Genre : F\r\n"
                + "Age : 7\r\n"
                + "Date de naissance : 01/02/2019\r\n"
                + "Lieu de naissance : Grenoble\r\n"
                + "\r\n"), csv);
    }

    /**
     * Regression test for R8: the duration, measured in milliseconds, was divided by 100 while labelled in seconds.
     */
    @Test
    void shouldExportTheDurationInSeconds() {
        assertEquals("12.345", EvalReport.formatDurationInSeconds(12345));
        assertEquals("0.5", EvalReport.formatDurationInSeconds(500));
        assertEquals("Temps de l'évaluation: ", report(true).toExcelRows().get(2)[0]);
        assertEquals("12.345 secondes", report(true).toExcelRows().get(2)[1]);
    }

    /**
     * Same layout as the former Excel export, which only worked for anonymous evaluations.
     */
    @Test
    void shouldExportAnonymousExcelRows() {
        List<String[]> rows = report(true).toExcelRows();

        // 6 header rows, 9 rows per item, a blank row, the title and one row per score
        assertEquals(6 + 2 * 9 + 2 + 2, rows.size());
        assertArrayEquals(new String[]{"Nom de l'évaluation : ", "Eval test"}, rows.get(0));
        assertArrayEquals(new String[]{"", ""}, rows.get(6));
        assertArrayEquals(new String[]{"Item 1", ""}, rows.get(7));
        assertArrayEquals(new String[]{"- Nom de l'image -> ", "chat.png"}, rows.get(8));
        assertArrayEquals(new String[]{"- Description de l'image -> ", "un chien"}, rows.get(11));
        assertArrayEquals(new String[]{"- Résultat -> ", "correct"}, rows.get(14));
        assertArrayEquals(new String[]{"- Résultat -> ", null}, rows.get(23));
        assertArrayEquals(new String[]{"Scores"}, rows.get(25));
        assertArrayEquals(new String[]{"- Fruits -> ", "0 / 1"}, rows.get(27));
    }

    /**
     * Regression test for R10: the rows were written into an array sized for the anonymous layout, so the export of
     * a nominative evaluation failed with an ArrayIndexOutOfBoundsException and no Excel file was produced.
     */
    @Test
    void shouldWriteNominativeExcelFile(@TempDir File tempDir) throws IOException {
        List<String[]> rows = report(false).toExcelRows();
        assertEquals(6 + 8 + 2 * 9 + 2 + 2, rows.size());
        assertArrayEquals(new String[]{"Nom : ", "Dupont"}, rows.get(8));
        assertArrayEquals(new String[]{"Lieu de naissance : ", "Grenoble"}, rows.get(13));

        File file = new File(tempDir, "eval.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            XSSFSheet sheet = workbook.createSheet("Eval test");
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < rows.get(r).length; c++) {
                    if (rows.get(r)[c] != null) {
                        row.createCell(c).setCellValue(rows.get(r)[c]);
                    }
                }
            }
            workbook.write(out);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
            assertEquals("Nom : ", workbook.getSheetAt(0).getRow(8).getCell(0).getStringCellValue());
            assertEquals("Dupont", workbook.getSheetAt(0).getRow(8).getCell(1).getStringCellValue());
        }
    }
}
