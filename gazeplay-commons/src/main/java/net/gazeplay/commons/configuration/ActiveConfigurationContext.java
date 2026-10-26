package net.gazeplay.commons.configuration;

import lombok.Getter;

/**
 * The configuration in use: the one of the selected user profile, or of the default profile.
 * <p>
 * A process-wide singleton, kept on purpose (decision of the maintainer): the games read it through {@code
 * IGameContext.getConfiguration()}.
 */
public class ActiveConfigurationContext {

    @Getter
    private static Configuration instance = ConfigurationSource.createFromDefaultProfile();

    /**
     * Makes the configuration of a user profile the active one (profiles/&lt;userId&gt;/GazePlay.properties).
     */
    public static void switchToUser(final String userId) {
        instance = ConfigurationSource.createFromProfile(userId);
    }

    /**
     * Makes the configuration of the default profile the active one (GazePlay.properties of the GazePlay folder).
     */
    public static void switchToDefaultUser() {
        instance = ConfigurationSource.createFromDefaultProfile();
    }

    /**
     * Replaces the active configuration, e.g. by a mock in tests. {@link #switchToDefaultUser()} restores the default
     * profile.
     */
    public static void setInstance(final Configuration configuration) {
        instance = configuration;
    }

}
