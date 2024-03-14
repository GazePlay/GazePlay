package net.gazeplay.components;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.stats.Stats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
public class SaveData {

    Stats stats;
    String gameName;
    String variantName;
    String pathSaveFile = "";
    public int maxMouseMovements;
    public int maxTrackerMovements;
    ArrayList<Integer> nbMouseMovements;
    ArrayList<Integer> nbTrackerMovements;

    public SaveData(Stats stats, String variant){
        this.stats = stats;
        this.gameName = this.stats.gameName;
        this.variantName = variant;
        this.maxMouseMovements = 0;
        this.maxTrackerMovements = 0;
        this.nbMouseMovements = new ArrayList<>();
        this.nbTrackerMovements = new ArrayList<>();

        this.checkFolder();
    }

    public void checkFolder(){
        try{
            String userName = System.getProperty("user.name");
            this.pathSaveFile = "C:\\Users\\" + userName + "\\Documents\\Gazeplay\\Emmanuel\\" + this.gameName + "\\";
            File dir = new File(this.pathSaveFile);
            if (!dir.exists()){
                boolean createDir = dir.mkdirs();
                log.info("Dir saveData created ? -> " + createDir);
            }
        } catch (Exception e){
            log.info(String.valueOf(e));
        }
    }

    public void addMouseMovements(int nbMovements){
        this.nbMouseMovements.add(nbMovements);
        this.maxMouseMovements += nbMovements;
    }

    public void addTrackerMovements(int nbMovements){
        this.nbTrackerMovements.add(nbMovements);
        this.maxTrackerMovements += nbMovements;
    }

    public String getDate(){
        Date now = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm:ss");

        return formatDate.format(now);
    }

    public String getTimeGame(){
        Duration duration = Duration.ofMillis(this.stats.computeTotalElapsedDuration());

        // Extract the days from the duration, then take it away so we can format the rest of the string.
        long days = duration.toDaysPart();
        Duration durationLessDays = duration.minusDays(days);
        String result = "";

        if (days > 0) {
            result += String.format("%dd ", days);
        }

        return result + durationLessDays.toString()
            .substring(2)
            .replaceAll("(\\d[HMS])(?!$)", "$1 ")
            .toLowerCase();
    }

    public void createExcelSavFile(){
        String pathFile = this.pathSaveFile + this.gameName + "-" + DateUtils.dateTimeNow() + ".xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(this.gameName);

        Object[][] bookData = new Object[9 + this.nbMouseMovements.size() * 4][3];

        bookData[0] = new Object[]{"Nom de jeu", this.gameName, ""};
        bookData[1] = new Object[]{"Variant du jeu", this.variantName, ""};
        bookData[2] = new Object[]{"Fait le", this.getDate(), ""};
        bookData[3] = new Object[]{"Temps de l'évaluation", this.getTimeGame(), "secondes"};
        bookData[4] = new Object[]{"Nombre de tableau fait", this.stats.nbGoalsReached+1, ""};
        bookData[5] = new Object[]{"Nombre total d'erreur", this.stats.nbError, ""};
        bookData[6] = new Object[]{"Nombre de mouvements total avec la souris", this.maxMouseMovements, ""};
        bookData[7] = new Object[]{"Nombre de mouvements total avec l'eye tracker", this.maxTrackerMovements, ""};
        bookData[8] = new Object[]{"", "", ""};

        int startIndex = 9;
        for (int i = 0; i<this.nbMouseMovements.size(); i++){
            bookData[startIndex] = new Object[]{"Tableau", i+1, ""};
            bookData[startIndex+1] = new Object[]{"Nombre de mouvements avec la souris", this.nbMouseMovements.get(i), ""};
            bookData[startIndex+2] = new Object[]{"Nombre de mouvements avec l'eye tracker", this.nbTrackerMovements.get(i), ""};
            bookData[startIndex+3] = new Object[]{"", "", ""};
            startIndex += 4;
        }

        int rowCount = 0;

        for (Object[] aBook : bookData) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(columnCount++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(pathFile)) {
            workbook.write(outputStream);
        } catch (Exception e){
            log.info("Error creation xls file !");
            e.printStackTrace();
        }
    }
    public void createCsvSaveFile(){
        String pathFile = this.pathSaveFile + this.gameName + "-" + DateUtils.dateTimeNow() + ".csv";

        try {
            PrintWriter out = new PrintWriter(pathFile, StandardCharsets.UTF_16);
            out.append("Nom du jeu : ").append(this.gameName).append("\r\n");
            out.append("Variant du jeu : ").append(this.variantName).append("\r\n");
            out.append("Fait le ").append(this.getDate()).append("\r\n");
            out.append("Temps de l'évaluation : ").append(String.valueOf(this.getTimeGame())).append(" secondes \r\n");
            out.append("Nombre de tableau fait : ").append(String.valueOf(this.stats.nbGoalsReached + 1)).append("\r\n");
            out.append("Nombre de mouvements total avec la souris : ").append(String.valueOf(this.maxMouseMovements)).append("\r\n");
            out.append("Nombre de mouvements total avec l'eye tracker' : ").append(String.valueOf(this.maxTrackerMovements)).append("\r\n");
            out.append("\r\n");
            for (int i = 0; i<this.nbMouseMovements.size(); i++){
                out.append("Tableau ").append(String.valueOf(i+1)).append(" : ").append("\r\n");
                out.append("Nombre de mouvements avec la souris : ").append(String.valueOf(this.nbMouseMovements.get(i))).append("\r\n");
                out.append("Nombre de mouvements avec l'eye tracker' : ").append(String.valueOf(this.nbTrackerMovements.get(i))).append("\r\n");
                out.append("\r\n");
            }
            out.close();
        } catch (Exception e) {
            log.info("Error creation csv !");
            log.info(String.valueOf(e));
        }
    }
}
