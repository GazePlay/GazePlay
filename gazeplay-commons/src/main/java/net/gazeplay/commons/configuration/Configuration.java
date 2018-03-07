package net.gazeplay.commons.configuration;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
@Data
public class Configuration {

    private final boolean gazeMode;

    private final String eyetracker;

    private final String language;

    private final String filedir;

    private final Integer fixationlength;

    private final String cssfile;

    private final String whereIsItDir;

    private final int questionLength;

    private final boolean enableRewardSound;

    private final String menuButtonsOrientation;

    protected Configuration(ConfigurationBuilder builder) {
        this.gazeMode = builder.gazeMode;
        this.eyetracker = builder.eyetracker;
        this.language = builder.language;
        this.filedir = builder.filedir;
        this.fixationlength = builder.fixationlength;
        this.cssfile = builder.cssfile;
        this.whereIsItDir = builder.whereIsItDir;
        this.questionLength = builder.questionLength;
        this.enableRewardSound = builder.enableRewardSound;
        this.menuButtonsOrientation = builder.menuButtonsOrientation;
    }

}
