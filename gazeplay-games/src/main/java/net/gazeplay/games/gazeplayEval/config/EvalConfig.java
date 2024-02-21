package net.gazeplay.games.gazeplayEval.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.stream.Stream;

import static net.gazeplay.games.gazeplayEval.config.Const.*;

@Slf4j
public class EvalConfig {
    private final String name;
    private final ResultsOutputType outputType;
    private final String patientId;
    private final ArrayList<ItemConfig> items;

    public EvalConfig(String name, ResultsOutputType outputType, String patientId, ArrayList<ItemConfig> items) {
        this.name = name;
        this.outputType = outputType;
        this.patientId = patientId;
        this.items = items;
    }

    public EvalConfig(String evalDirName) throws Exception {
        JsonParser parser = new JsonParser();
        JsonObject config = parser.parse(new FileReader(ROOT_DIRECTORY + evalDirName + CONFIG_LOCATION)).getAsJsonObject();

        this.name = config.get(EVAL_NAME).getAsString();
        this.outputType = ResultsOutputType.valueOf(config.get(EVAL_OUTPUT_TYPE).getAsString().trim().toUpperCase());
        this.patientId = config.get(EVAL_PATIENT_ID).getAsString();
        this.items = new ArrayList<>();
        for (JsonElement item : config.get(EVAL_ITEMS).getAsJsonArray())
            this.items.add(new ItemConfig(item.getAsJsonObject()));
        log.debug(
            "New instance: " + "\n" +
            "    name: " + name + "\n" +
            "    outputType: " + outputType + "\n" +
            "    patientId: " + patientId + "\n" +
            "    items: " + items.size()
        );
    }


    public String getName() {
        return name;
    }

    public ResultsOutputType getOutputType() {
        return outputType;
    }

    public String getPatientId() {
        return patientId;
    }

    public ItemConfig getItem(int index) throws IndexOutOfBoundsException {
        return items.get(index);
    }

    public Stream<ItemConfig> getItems() {
        return items.stream();
    }
}
