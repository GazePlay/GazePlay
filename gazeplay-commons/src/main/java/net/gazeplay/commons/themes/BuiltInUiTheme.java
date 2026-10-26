package net.gazeplay.commons.themes;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.Set;

/**
 * Color themes of the user interface (CSS_FILE setting), each one a stylesheet of data/stylesheets. The setting holds
 * "builtin:" followed by the name of the theme, or the path of its stylesheet (older versions).
 */
@AllArgsConstructor
public enum BuiltInUiTheme {

    SYSTEM("System", null),

    // AFSR-Green and AFSR-Purple were removed with the AFSR edition (October 2026): a saved "builtin:AFSRGREEN" or
    // "builtin:AFSRPURPLE" value is no longer found, and the default theme is used (CssUtil, configuration screen)

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

    private final String stylesheetResourceName;

    @Getter
    private final Set<String> supportedConfigPropertyValues;

    BuiltInUiTheme(String label, String stylesheetResourceName) {
        this.label = label;
        this.stylesheetResourceName = stylesheetResourceName;
        this.supportedConfigPropertyValues = Sets.newHashSet(BUILTIN_PREFIX + name(), getStyleSheetPath());
    }

    /**
     * @return the value saved in the settings for this theme
     */
    public String getPreferredConfigPropertyValue() {
        return BUILTIN_PREFIX + name();
    }

    /**
     * @return the resource path of the stylesheet, null for the system theme (no stylesheet)
     */
    public String getStyleSheetPath() {
        if (stylesheetResourceName == null) {
            return null;
        }
        return PRE_BUILD_STYLESHEETS_LOCATION + stylesheetResourceName;
    }

    public static final BuiltInUiTheme DEFAULT_THEME = SILVER_AND_GOLD;

    /**
     * @return the theme of a value of the setting, empty when it is no built-in theme (e.g. a removed one)
     */
    public static Optional<BuiltInUiTheme> findFromConfigPropertyValue(String configPropertyValue) {
        for (BuiltInUiTheme builtInUiTheme : BuiltInUiTheme.values()) {
            if (builtInUiTheme.getSupportedConfigPropertyValues().contains(configPropertyValue)) {
                return Optional.of(builtInUiTheme);
            }
        }

        return Optional.empty();
    }

}
