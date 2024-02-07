package net.gazeplay.games.gazeplayEval.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;

import static net.gazeplay.games.gazeplayEval.config.Constants.*;

@Slf4j
public class ItemConfig {
    private final int rows;
    private final int cols;
    private final File[][] grid;
    private final Boolean randomize;  // Whether to randomize the grid at runtime or not
    private final int selections;  // Number of selections required
    private final File audioFile;
    private final AudioScheduleType audioSchedule;
    private final int gazeTime;  // Game-level parameter of the time needed gazing at objects to select them
    private final int timeLimit;

    public ItemConfig(int rows, int cols, File[][] grid, Boolean randomize, int selections, File audioFile, AudioScheduleType audioSchedule, int gazeTime, int timeLimit) {
        this.rows = rows;
        this.cols = cols;
        this.grid = grid;
        this.randomize = randomize;
        this.selections = selections;
        this.audioFile = audioFile;
        this.audioSchedule = audioSchedule;
        this.gazeTime = gazeTime;
        this.timeLimit = timeLimit;
    }

    public ItemConfig(JsonObject itemObject) throws Exception {
        this.rows = itemObject.get(ITEM_ROWS).getAsInt();
        this.cols = itemObject.get(ITEM_COLS).getAsInt();
        this.randomize = itemObject.get(ITEM_RANDOMIZE_GRID).getAsBoolean();
        this.selections = itemObject.get(ITEM_SELECTIONS).getAsInt();
        this.audioFile = new File(itemObject.get(ITEM_AUDIO_FILEPATH).getAsString());
        this.audioSchedule = AudioScheduleType.valueOf(itemObject.get(ITEM_AUDIO_SCHEDULE).getAsString().trim().toUpperCase());
        this.gazeTime = itemObject.get(ITEM_GAZE_TIME).getAsInt();
        this.timeLimit = itemObject.get(ITEM_TIME_LIMIT).getAsInt();
        this.grid = new File[rows][cols];
        JsonArray gridArray = itemObject.get(ITEM_GRID).getAsJsonArray();
        for (int i = 0; i < rows; i++) {
            JsonArray gridRow = gridArray.get(i).getAsJsonArray();
            for (int j = 0; j < cols; j++)
                this.grid[i][j] = new File(gridRow.get(j).getAsString());
        }
        log.debug(
            "New instance: " + "\n" +
            "    rows: " + rows + "\n" +
            "    cols: " + cols + "\n" +
            "    grid: " + Arrays.deepToString(grid) + "\n" +
            "    randomize: " + randomize + "\n" +
            "    selections: " + selections + "\n" +
            "    audioFile: " + audioFile + "\n" +
            "    audioSchedule: " + audioSchedule + "\n" +
            "    gazeTime: " + gazeTime + "\n" +
            "    timeLimit: " + timeLimit
        );
    }


    public int getRowSize() {
        return rows;
    }

    public int getColumnSize() {
        return cols;
    }

    public File[][] getGrid() {
        return grid;
    }

    public Boolean isGridRandomized() {
        return randomize;
    }

    public int getSelectionsRequired() {
        return selections;
    }

    public File getAudioFile() {
        return audioFile;
    }

    public AudioScheduleType getAudioSchedule() {
        return audioSchedule;
    }

    public int getGazeTime() {
        return gazeTime;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
}
