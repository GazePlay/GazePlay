package net.gazeplay.commons.utils.multilinguism;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MultilinguismFactory {

    private final static String mainFilePath = "data/multilinguism/multilinguism.csv";

    @Getter
    private static final Multilinguism singleton = new Multilinguism(new I18N(mainFilePath));

    private static final Map<String, Multilinguism> byResourceLocation = new HashMap<>();

    public static Multilinguism getForResource(final String resourceLocation) {
        Multilinguism result = byResourceLocation.get(resourceLocation);
        if (result == null) {
            result = new Multilinguism(new I18N(resourceLocation));
            byResourceLocation.put(resourceLocation, result);
        }
        return result;
    }
}
