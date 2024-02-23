package net.gazeplay.games.gazeplayEval.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.games.gazeplayEval.GameState;

import java.io.File;
import java.util.Arrays;

import static net.gazeplay.games.gazeplayEval.config.Const.*;

@Slf4j
public class ItemConfig {
    private final int rows;
    private final int cols;
    private final File[][] grid;
    private final Boolean randomize;  // Whether to randomize the grid at runtime or not
    private final int selections;  // Number of selections required
    private final QuestionType questionType;
    private final String question;  // Contains the string of the question's text, or the filename of the audio file
    private final File audioFile;  // File object for the audio file
    private final QuestionScheduleType questionSchedule;
    private final int gazeTime;  // Game-level parameter of the time needed gazing at objects to select them
    private final int timeLimit;

    public ItemConfig(JsonObject itemObject) throws Exception {
        this.rows = itemObject.get(ITEM_ROWS).getAsInt();
        this.cols = itemObject.get(ITEM_COLS).getAsInt();
        this.randomize = itemObject.get(ITEM_RANDOMIZE_GRID).getAsBoolean();
        this.selections = itemObject.get(ITEM_SELECTIONS).getAsInt();
        this.questionType = QuestionType.valueOf(itemObject.get(ITEM_QUESTION_TYPE).getAsString().trim().toUpperCase());
        this.question = itemObject.get(ITEM_QUESTION).getAsString();
        this.audioFile = new File(GameState.getPathFor(AUDIO_LOCATION + this.question));  // May not exist
        this.questionSchedule = QuestionScheduleType.valueOf(itemObject.get(ITEM_QUESTION_SCHEDULE).getAsString().trim().toUpperCase());
        this.gazeTime = itemObject.get(ITEM_GAZE_TIME).getAsInt();
        this.timeLimit = itemObject.get(ITEM_TIME_LIMIT).getAsInt();
        this.grid = new File[rows][cols];
        JsonArray gridArray = itemObject.get(ITEM_GRID).getAsJsonArray();
        for (int i = 0; i < rows; i++) {
            JsonArray gridRow = gridArray.get(i).getAsJsonArray();
            for (int j = 0; j < cols; j++)
                this.grid[i][j] = new File(GameState.getPathFor(IMAGES_LOCATION + gridRow.get(j).getAsString()));
        }
        log.info(
            "New instance: " + "\n" +
            "    rows: " + rows + "\n" +
            "    cols: " + cols + "\n" +
            "    grid: " + Arrays.deepToString(grid) + "\n" +
            "    randomize: " + randomize + "\n" +
            "    selections: " + selections + "\n" +
            "    question (" + questionType + "): " + question + "\n" +
            "    scheduleType: " + questionSchedule + "\n" +
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

    public File getGrid(int i, int j) {
        return grid[i][j];
    }

    public Boolean isGridRandomized() {
        return randomize;
    }

    public int getSelectionsRequired() {
        return selections;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public String getQuestionText() {
        return question;
    }

    public File getQuestionAudioFile() {
        return audioFile;
    }

    public QuestionScheduleType getQuestionSchedule() {
        return questionSchedule;
    }

    public int getGazeTime() {
        return gazeTime;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
}
