package net.gazeplay.commons.configuration;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Properties;

/**
 * Keys of GazePlay.properties renamed in version 1.11.1 (commit bddc3f1e8, August 2022) without migration: the
 * settings of older installations were silently replaced by the default values.
 * <p>
 * Transition strategy: an old key is only read when its new key is missing, the new key always wins, and the old key
 * is left in the file so that an older version of GazePlay still finds its settings. All these settings kept the same
 * type and meaning.
 */
@Slf4j
final class LegacyConfigurationKeys {

    static final Map<String, String> NEW_KEY_BY_OLD_KEY = Map.ofEntries(
        Map.entry("BACKGROUND_DARK_THEME", "DARK_THEME_ENABLED"),
        Map.entry("CSSFILE", "CSS_FILE"),
        Map.entry("ENABLE_REWARD_SOUND", "REWARD_SOUND_ENABLED"),
        Map.entry("EYETRACKER", "EYE_TRACKER"),
        Map.entry("FILEDIR", "FILE_DIR"),
        Map.entry("FIXATIONLENGTH", "FIXATION_LENGTH"),
        Map.entry("FIXATIONSEQUENCE_DISABLED", "FIXATION_SEQUENCE_DISABLED"),
        Map.entry("FORCE_DISPLAY_NEWS", "DISPLAY_NEWS_FORCED"),
        Map.entry("LIMITERS", "LIMITER_SCORE_ENABLED"),
        Map.entry("LIMITERT", "LIMITER_TIME_ENABLED"),
        Map.entry("MUSIC_FOLDER", "MUSIC_DIR"),
        Map.entry("QUESTIONLENGTH", "QUESTION_LENGTH"),
        Map.entry("REASK_QUESTION_ON_FAIL", "QUESTION_REASKED_ON_FAIL"),
        Map.entry("SCREENSHOT", "MULTIPLE_SCREENSHOTS_ENABLED"),
        Map.entry("SHORTCUT_FOLDER", "SHORTCUT_DIR"),
        Map.entry("VIDEO_FOLDER", "VIDEO_DIR"),
        Map.entry("WHEREISITDIR", "WHERE_IS_IT_DIR")
    );

    private LegacyConfigurationKeys() {
    }

    /**
     * Copies the value of each old key to its new key, when the new key is missing.
     *
     * @return the number of migrated keys
     */
    static int migrate(final Properties properties) {
        int migrated = 0;
        for (final Map.Entry<String, String> keys : NEW_KEY_BY_OLD_KEY.entrySet()) {
            final String oldValue = properties.getProperty(keys.getKey());
            if (oldValue != null && !properties.containsKey(keys.getValue())) {
                properties.setProperty(keys.getValue(), oldValue);
                log.warn("Configuration key {} is deprecated since GazePlay 1.11.1, its value is used for {}",
                    keys.getKey(), keys.getValue());
                migrated++;
            }
        }
        return migrated;
    }
}
