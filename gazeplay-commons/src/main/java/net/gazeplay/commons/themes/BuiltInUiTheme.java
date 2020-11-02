package net.gazeplay.commons.themes;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public enum BuiltInUiTheme {

    SYSTEM("System", null),

    AFSRGREEN("AFSR-Green", "afsr-green.css"),

    AFSRPURPLE("AFSR-Purple", "afsr-purple.css"),

    ORANGE("Orange", "main-orange.css"),

    GREEN("Green", "main-green.css"),

    LIGHT_BLUE("Light Blue", "main-light-blue.css"),

    BLUE("Blue", "main-blue.css"),

    SILVER_AND_GOLD("Silver and Gold", "main-silver-gold.css"),

    PINK_AND_PURPLE("Pink and Purple", "main-pink-purple.css");

    private static final String BUILTIN_PREFIX = "builtin:";

    private static final String PRE_BUILD_STYLESHEETS_LOCATION = "data/stylesheets/";

    @Getter
    private final String label;

    // @Getter
    private final String stylesheetResourceName;

    @Getter
    private final Set<String> supportedConfigPropertyValues;

    BuiltInUiTheme(String label, String stylesheetResourceName) {
        this.label = label;
        this.stylesheetResourceName = stylesheetResourceName;
        this.supportedConfigPropertyValues = Sets.newHashSet(BUILTIN_PREFIX + name(), getStyleSheetPath());
    }

    public String getPreferredConfigPropertyValue() {
        return BUILTIN_PREFIX + name();
    }

    public String getStyleSheetPath() {
        if (stylesheetResourceName == null) {
            return null;
        }
        return PRE_BUILD_STYLESHEETS_LOCATION + stylesheetResourceName;
    }

    public static final BuiltInUiTheme DEFAULT_THEME = SILVER_AND_GOLD;

    public static Optional<BuiltInUiTheme> findFromConfigPropertyValue(String configPropertyValue) {
        for (BuiltInUiTheme builtInUiTheme : BuiltInUiTheme.values()) {
            if (builtInUiTheme.getSupportedConfigPropertyValues().contains(configPropertyValue)) {
                return Optional.of(builtInUiTheme);
            }
        }

        return Optional.empty();
    }

}
