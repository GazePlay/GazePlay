package net.gazeplay.commons.eval;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.gazeplay.commons.eval.EvalDefinition.MediaFolder;
import net.gazeplay.commons.eval.EvalScreen.Cell;
import net.gazeplay.commons.eval.EvalScreen.MediaType;
import net.gazeplay.commons.eval.EvalScreen.SelectionRule;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Reads an evaluation exported by the GazePlay Eval website (its folder: evalData.json, evalInfo.json and the media)
 * and checks that it can be played.
 * <p>
 * The format is the one of the website (services/download/download.service.ts): one JSON object per screen, whose keys
 * are French labels. All the durations are in seconds and may be decimal. The keys must not be renamed: they are the
 * contract with the website. Unknown keys are ignored, so that the website can add some.
 */
public final class EvalReader {

    public static final String EVAL_DATA = "evalData.json";
    public static final String EVAL_INFO = "evalInfo.json";

    // Keys of evalData.json
    static final String TYPE = "Type";
    static final String AUTO_ADVANCE = "Mettre un temps avant passage à l'écran suivant";
    static final String AUTO_ADVANCE_TIME = "Combien de temps";
    static final String FIXATION_CROSS = "Mettre une croix de fixation";
    // On a transition: the cross must be fixated. On an instruction: the start button is shown
    static final String FIXATION_REQUIRED = "Mettre un temps de fixation";
    static final String FIXATION_TIME = "Combien de temps de fixation";
    static final String ADD_MEDIA = "Ajouter un media";
    static final String MEDIA_TYPE = "Type de media";
    static final String FILE_NAME = "Nom du fichier";
    static final String ROWS = "Nombre de lignes";
    static final String COLUMNS = "Nombre de colonnes";
    static final String SELECTION_RULE = "Choix de sélection";
    static final String TO_SELECT = "Combien à sélectionner";
    static final String RANDOM_POSITIONS = "Position stimuli aléatoire";
    static final String HIDE_AFTER_SELECTION = "Caché stimuli après selection";
    static final String SCREEN_SOUND = "Mettre un son";
    static final String CELLS = "Liste des stimuli";
    static final String CELL_IMAGE = "imageName";
    static final String CELL_SOUND = "soundName";
    static final String CELL_CORRECT = "goodAnswer";
    static final String CELL_HIDDEN = "hidden";

    // Keys of evalInfo.json
    static final String INFO_NAME = "Nom de l'évaluation";
    static final String INFO_FORMAT = "Format choisi";
    static final String INFO_PARTICIPANT = "Informations participant";

    /**
     * Extensions of the media that JavaFX can play or show, by folder.
     */
    static final Map<MediaFolder, Set<String>> SUPPORTED_EXTENSIONS = Map.of(
        MediaFolder.IMAGES, Set.of("png", "jpg", "jpeg", "gif", "bmp"),
        MediaFolder.AUDIO, Set.of("mp3", "wav", "m4a", "aac", "aif", "aiff"),
        MediaFolder.VIDEOS, Set.of("mp4", "m4v", "flv", "fxm")
    );

    /**
     * What was read: the evaluation, null when an error prevents playing it, and the problems found.
     */
    public record Result(EvalDefinition definition, List<EvalProblem> problems) {

        public boolean hasErrors() {
            return problems.stream().anyMatch(EvalProblem::error);
        }

        public List<EvalProblem> errors() {
            return problems.stream().filter(EvalProblem::error).toList();
        }

        public List<EvalProblem> warnings() {
            return problems.stream().filter(problem -> !problem.error()).toList();
        }
    }

    private EvalReader() {
    }

    /**
     * Reads and checks the evaluation of a folder.
     */
    public static Result read(Path folder) {
        List<EvalProblem> problems = new ArrayList<>();
        Path dataFile = folder.resolve(EVAL_DATA);
        if (!Files.isRegularFile(dataFile)) {
            problems.add(EvalProblem.error("EvalNoData"));
            return new Result(null, problems);
        }

        JsonElement data = parse(dataFile, problems);
        if (data == null) {
            return new Result(null, problems);
        }
        if (!data.isJsonArray()) {
            problems.add(EvalProblem.error("EvalUnreadableFile", EVAL_DATA));
            return new Result(null, problems);
        }

        List<EvalScreen> screens = new ArrayList<>();
        int number = 0;
        for (JsonElement element : data.getAsJsonArray()) {
            number++;
            if (!element.isJsonObject()) {
                problems.add(EvalProblem.error("EvalUnknownScreenType", number, element));
                continue;
            }
            EvalScreen screen = readScreen(new ScreenReader(element.getAsJsonObject(), number, problems));
            if (screen != null) {
                screens.add(screen);
            }
        }
        if (number == 0) {
            problems.add(EvalProblem.error("EvalNoScreen"));
        }

        String name = EvalLibrary.fileName(folder);
        EvalDefinition.ResultFormat format = EvalDefinition.ResultFormat.CSV_AND_XLSX;
        List<String> participantFields = new ArrayList<>();
        Path infoFile = folder.resolve(EVAL_INFO);
        if (Files.isRegularFile(infoFile)) {
            JsonElement info = parse(infoFile, problems);
            if (info != null && info.isJsonObject()) {
                JsonObject infoObject = info.getAsJsonObject();
                String infoName = string(infoObject.get(INFO_NAME));
                if (infoName != null && !infoName.isBlank()) {
                    name = infoName;
                }
                String formatValue = string(infoObject.get(INFO_FORMAT));
                if (formatValue != null) {
                    format = EvalDefinition.ResultFormat.fromSiteValue(formatValue);
                }
                JsonElement fields = infoObject.get(INFO_PARTICIPANT);
                if (fields != null && fields.isJsonArray()) {
                    for (JsonElement field : fields.getAsJsonArray()) {
                        String label = string(field);
                        if (label != null && !label.isBlank()) {
                            participantFields.add(label.trim());
                        }
                    }
                }
            }
        } else {
            problems.add(EvalProblem.warning("EvalNoInfo"));
        }

        EvalDefinition definition = new EvalDefinition(folder, name, format, List.copyOf(participantFields),
            List.copyOf(screens));
        if (problems.stream().noneMatch(EvalProblem::error)) {
            checkPlayable(definition, problems);
        }
        boolean playable = problems.stream().noneMatch(EvalProblem::error);
        return new Result(playable ? definition : null, List.copyOf(problems));
    }

    private static JsonElement parse(Path file, List<EvalProblem> problems) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (IOException | JsonParseException e) {
            problems.add(EvalProblem.error("EvalUnreadableFile", EvalLibrary.fileName(file)));
            return null;
        }
    }

    private static EvalScreen readScreen(ScreenReader screen) {
        String type = screen.requiredString(TYPE);
        if (type == null) {
            return null;
        }
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "transition" -> readTransition(screen);
            case "instruction" -> readInstruction(screen);
            case "stimuli" -> readStimuli(screen);
            default -> {
                screen.error("EvalUnknownScreenType", screen.number, type);
                yield null;
            }
        };
    }

    private static EvalScreen.Transition readTransition(ScreenReader screen) {
        Double autoAdvance = screen.optionalDuration(AUTO_ADVANCE, AUTO_ADVANCE_TIME);
        boolean cross = screen.requiredBoolean(FIXATION_CROSS);
        Double crossFixation = cross ? screen.optionalDuration(FIXATION_REQUIRED, FIXATION_TIME) : null;
        if (screen.failed) {
            return null;
        }
        return new EvalScreen.Transition(autoAdvance, cross, crossFixation);
    }

    private static EvalScreen.Instruction readInstruction(ScreenReader screen) {
        Double autoAdvance = screen.optionalDuration(AUTO_ADVANCE, AUTO_ADVANCE_TIME);
        boolean addMedia = screen.requiredBoolean(ADD_MEDIA);
        MediaType mediaType = null;
        String content = null;
        if (addMedia) {
            String typeValue = screen.requiredString(MEDIA_TYPE);
            mediaType = MediaType.fromSiteValue(typeValue);
            if (typeValue != null && mediaType == null) {
                screen.error("EvalBadValue", screen.number, MEDIA_TYPE, typeValue);
            }
            content = screen.optionalString(FILE_NAME);
            if (mediaType != null && (content == null || content.isBlank())) {
                screen.warning("EvalEmptyInstruction", screen.number);
                mediaType = null;
                content = null;
            }
        }
        Double startButton = screen.optionalDuration(FIXATION_REQUIRED, FIXATION_TIME);
        if (screen.failed) {
            return null;
        }
        return new EvalScreen.Instruction(autoAdvance, mediaType, content, startButton);
    }

    private static EvalScreen.Stimuli readStimuli(ScreenReader screen) {
        int rows = screen.requiredCount(ROWS);
        int columns = screen.requiredCount(COLUMNS);
        Double autoAdvance = screen.optionalDuration(AUTO_ADVANCE, AUTO_ADVANCE_TIME);
        double fixation = screen.requiredDuration(FIXATION_TIME);
        String ruleValue = screen.requiredString(SELECTION_RULE);
        SelectionRule rule = SelectionRule.fromSiteValue(ruleValue);
        if (ruleValue != null && rule == null) {
            screen.error("EvalBadValue", screen.number, SELECTION_RULE, ruleValue);
        }
        int toSelect = screen.requiredCount(TO_SELECT);
        boolean random = screen.requiredBoolean(RANDOM_POSITIONS);
        boolean hide = screen.requiredBoolean(HIDE_AFTER_SELECTION);
        String sound = null;
        if (screen.requiredBoolean(SCREEN_SOUND)) {
            sound = screen.optionalString(FILE_NAME);
            if (sound != null && sound.isBlank()) {
                sound = null;
            }
        }
        List<Cell> cells = screen.failed ? List.of() : readCells(screen, rows * columns);
        if (screen.failed) {
            return null;
        }
        return new EvalScreen.Stimuli(rows, columns, autoAdvance, fixation, rule, toSelect, random, hide, sound,
            cells);
    }

    /**
     * The cells, in the order of their number (the keys "0", "1"... sorted as numbers, not as text: "10" comes after
     * "9"). Missing cells are empty slots; extra cells are ignored.
     */
    private static List<Cell> readCells(ScreenReader screen, int count) {
        JsonElement element = screen.object.get(CELLS);
        if (element == null || !element.isJsonObject()) {
            screen.error("EvalMissingKey", screen.number, CELLS);
            return List.of();
        }
        TreeMap<Integer, JsonObject> byNumber = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            try {
                int index = Integer.parseInt(entry.getKey().trim());
                if (entry.getValue().isJsonObject() && index >= 0) {
                    byNumber.put(index, entry.getValue().getAsJsonObject());
                    continue;
                }
            } catch (NumberFormatException ignored) {
                // reported below
            }
            screen.error("EvalBadValue", screen.number, CELLS, entry.getKey());
        }
        if (byNumber.size() != count || (!byNumber.isEmpty() && byNumber.lastKey() >= count)) {
            screen.warning("EvalCellCount", screen.number, byNumber.size(), count);
        }
        List<Cell> cells = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            JsonObject cell = byNumber.get(index);
            if (cell == null) {
                cells.add(new Cell(null, null, false, true));
                continue;
            }
            String image = blankToNull(string(cell.get(CELL_IMAGE)));
            String sound = blankToNull(string(cell.get(CELL_SOUND)));
            boolean correct = bool(cell.get(CELL_CORRECT)).orElse(false);
            boolean hidden = bool(cell.get(CELL_HIDDEN)).orElse(false);
            // A cell without image nor sound shows nothing: it is an empty slot, also in the .zip files that do not
            // export "hidden"
            boolean emptySlot = hidden || (image == null && sound == null);
            cells.add(new Cell(emptySlot ? null : image, emptySlot ? null : sound, !emptySlot && correct, emptySlot));
        }
        return List.copyOf(cells);
    }

    /**
     * Checks the media (present, in a format JavaFX can read) and the screens that only the examiner can end.
     */
    private static void checkPlayable(EvalDefinition definition, List<EvalProblem> problems) {
        int number = 0;
        for (EvalScreen screen : definition.screens()) {
            number++;
            switch (screen) {
                case EvalScreen.Transition transition -> {
                    if (transition.autoAdvanceSeconds() == null && transition.crossFixationSeconds() == null) {
                        problems.add(EvalProblem.warning("EvalScreenWithoutEnd", number));
                    }
                }
                case EvalScreen.Instruction instruction -> {
                    if (instruction.mediaType() != null && instruction.mediaType() != MediaType.TEXT) {
                        MediaFolder folder = switch (instruction.mediaType()) {
                            case IMAGE -> MediaFolder.IMAGES;
                            case VIDEO -> MediaFolder.VIDEOS;
                            default -> MediaFolder.AUDIO;
                        };
                        checkMedia(definition, folder, instruction.content(), number, problems);
                    }
                    if (instruction.autoAdvanceSeconds() == null && instruction.startButtonFixationSeconds() == null) {
                        problems.add(EvalProblem.warning("EvalScreenWithoutEnd", number));
                    }
                }
                case EvalScreen.Stimuli stimuli -> {
                    if (stimuli.screenSound() != null) {
                        checkMedia(definition, MediaFolder.AUDIO, stimuli.screenSound(), number, problems);
                    }
                    for (Cell cell : stimuli.cells()) {
                        if (cell.image() != null) {
                            checkMedia(definition, MediaFolder.IMAGES, cell.image(), number, problems);
                        }
                        if (cell.sound() != null) {
                            checkMedia(definition, MediaFolder.AUDIO, cell.sound(), number, problems);
                        }
                    }
                    long countable = stimuli.cells().stream()
                        .filter(cell -> !cell.emptySlot())
                        .filter(cell -> stimuli.rule() == SelectionRule.ALL || cell.correct())
                        .count();
                    if (stimuli.toSelect() > countable) {
                        problems.add(EvalProblem.warning(stimuli.autoAdvanceSeconds() == null
                            ? "EvalGridCannotBeValidated" : "EvalGridOnlyByTime", number, stimuli.toSelect(), countable));
                    }
                }
            }
        }
    }

    private static void checkMedia(EvalDefinition definition, MediaFolder folder, String fileName, int number,
                                   List<EvalProblem> problems) {
        if (!isSafeFileName(fileName) || !Files.isRegularFile(definition.media(folder, fileName))) {
            problems.add(EvalProblem.error("EvalMissingMedia", number, folder.folderName() + "/" + fileName));
        } else if (!SUPPORTED_EXTENSIONS.get(folder).contains(extension(fileName))) {
            problems.add(EvalProblem.error("EvalUnsupportedMedia", number, fileName));
        }
    }

    /**
     * @return whether a file name of evalData.json stays in its media folder (no path)
     */
    static boolean isSafeFileName(String fileName) {
        return !fileName.isBlank() && !fileName.contains("/") && !fileName.contains("\\") && !fileName.equals("..")
            && !fileName.equals(".");
    }

    static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String string(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        return element.getAsString();
    }

    /**
     * @return the boolean of a value (true/false, or their text), empty when it is not one
     */
    private static Optional<Boolean> bool(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return Optional.empty();
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return Optional.of(primitive.getAsBoolean());
        }
        String text = primitive.getAsString().trim();
        if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false")) {
            return Optional.of(Boolean.parseBoolean(text));
        }
        return Optional.empty();
    }

    /**
     * @return the number of a value (a number, or its text with a dot or a comma), null when it is not one
     */
    private static Double number(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        try {
            double value = primitive.isNumber() ? primitive.getAsDouble()
                : Double.parseDouble(primitive.getAsString().trim().replace(',', '.'));
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reads the values of one screen and records its problems, with the number of the screen (from 1).
     */
    private static final class ScreenReader {
        private final JsonObject object;
        private final int number;
        private final List<EvalProblem> problems;
        private boolean failed;

        ScreenReader(JsonObject object, int number, List<EvalProblem> problems) {
            this.object = object;
            this.number = number;
            this.problems = problems;
        }

        void error(String key, Object... args) {
            problems.add(EvalProblem.error(key, args));
            failed = true;
        }

        void warning(String key, Object... args) {
            problems.add(EvalProblem.warning(key, args));
        }

        String requiredString(String key) {
            JsonElement element = object.get(key);
            if (element == null) {
                error("EvalMissingKey", number, key);
                return null;
            }
            return string(element);
        }

        String optionalString(String key) {
            return string(object.get(key));
        }

        boolean requiredBoolean(String key) {
            JsonElement element = object.get(key);
            Optional<Boolean> value = bool(element);
            if (value.isEmpty()) {
                invalid(key, element);
                return false;
            }
            return value.get();
        }

        double requiredDuration(String key) {
            JsonElement element = object.get(key);
            Double value = number(element);
            if (value == null || value < 0) {
                invalid(key, element);
                return 0;
            }
            return value;
        }

        private void invalid(String key, JsonElement element) {
            if (element == null) {
                error("EvalMissingKey", number, key);
            } else {
                error("EvalBadValue", number, key, element);
            }
        }

        /**
         * @return the duration of {@code durationKey} when {@code flagKey} is true, else null
         */
        Double optionalDuration(String flagKey, String durationKey) {
            if (!requiredBoolean(flagKey)) {
                return null;
            }
            return requiredDuration(durationKey);
        }

        int requiredCount(String key) {
            JsonElement element = object.get(key);
            Double value = number(element);
            if (value == null || value < 1 || value != Math.rint(value)) {
                invalid(key, element);
                return 1;
            }
            return value.intValue();
        }
    }
}
