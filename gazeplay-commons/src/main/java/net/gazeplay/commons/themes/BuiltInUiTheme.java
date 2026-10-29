package net.gazeplay.commons.themes;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The main color of the user interface (CSS_FILE setting, "Main color" in the settings, REFONTE_GRAPHIQUE.md 3.3).
 * <p>
 * Each value gives the accent color of the menus (-gp-accent of menus.css, one shade for the light theme and one for
 * the dark theme) and keeps its former stylesheet of data/stylesheets, which still styles the buttons, lists and
 * spinners of the games. The setting holds "builtin:" followed by the name of the value, or the path of its stylesheet
 * (older versions): the names are a persistent format and do not change, even where they no longer match the color
 * (SILVER_AND_GOLD, the former default, is the coral of the menus).
 */
public enum BuiltInUiTheme {

    /**
     * Shown as "Coral": the default color, and the former default theme.
     */
    SILVER_AND_GOLD("AccentCoral", "main-silver-gold.css", "#F2643A", "#FF7E57"),

    ORANGE("AccentOrange", "main-orange.css", "#D96C00", "#FFA040"),

    GREEN("AccentGreen", "main-green.css", "#2E8B47", "#5FD37F"),

    LIGHT_BLUE("AccentSky", "main-light-blue.css", "#1A80CC", "#63B3F5"),

    BLUE("AccentBlue", "main-blue.css", "#3F5BD9", "#8C9EFF"),

    /**
     * Shown as "Pink". Before VIOLET, which shares its stylesheet: the path of the stylesheet, saved by older versions,
     * is found as this value
     */
    PINK_AND_PURPLE("AccentPink", "main-pink-purple.css", "#D13D7A", "#FF85B3"),

    /**
     * Added with the main color (October 2026): an older version reads it as its default theme.
     */
    VIOLET("AccentViolet", "main-pink-purple.css", "#8A63D2", "#B595F0"),

    /**
     * No stylesheet: the games keep the look of JavaFX. No longer offered in the settings, still read (coral menus).
     */
    SYSTEM(null, null, "#F2643A", "#FF7E57");

    // AFSR-Green and AFSR-Purple were removed with the AFSR edition (October 2026): a saved "builtin:AFSRGREEN" or
    // "builtin:AFSRPURPLE" value is no longer found, and the default theme is used (CssUtil, configuration screen)

    private static final String BUILTIN_PREFIX = "builtin:";

    private static final String PRE_BUILD_STYLESHEETS_LOCATION = "data/stylesheets/";

    /**
     * The key of the name of the color in the translations, null for a value no longer offered.
     */
    @Getter
    private final String labelKey;

    private final String stylesheetResourceName;

    private final String lightAccent;

    private final String darkAccent;

    @Getter
    private final Set<String> supportedConfigPropertyValues;

    BuiltInUiTheme(String labelKey, String stylesheetResourceName, String lightAccent, String darkAccent) {
        this.labelKey = labelKey;
        this.stylesheetResourceName = stylesheetResourceName;
        this.lightAccent = lightAccent;
        this.darkAccent = darkAccent;
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

    /**
     * @return the accent color of the menus (#RRGGBB) on the dark or the light theme
     */
    public String getAccentColor(boolean darkTheme) {
        return darkTheme ? darkAccent : lightAccent;
    }

    public static final BuiltInUiTheme DEFAULT_THEME = SILVER_AND_GOLD;

    /**
     * @return the colors offered in the settings, in their order
     */
    public static List<BuiltInUiTheme> offeredColors() {
        return Arrays.stream(values()).filter(theme -> theme.labelKey != null).toList();
    }

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
