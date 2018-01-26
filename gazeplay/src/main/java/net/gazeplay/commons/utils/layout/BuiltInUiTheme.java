package net.gazeplay.commons.utils.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BuiltInUiTheme {

    ORANGE("Orange", "main-orange.css"),

    GREEN("Green", "main-green.css"),

    LIGHT_BLUE("Light Blue", "main-light-blue.css"),

    BLUE("Blue", "main-blue.css");

    @Getter
    private final String label;

    // @Getter
    private final String stylesheetResourceName;

    public String getStyleSheetPath() {
        return PRE_BUILD_STYLESHEETS_LOCATION + stylesheetResourceName;
    }

    static final String PRE_BUILD_STYLESHEETS_LOCATION = "data/stylesheets/";

}
