package net.gazeplay.commons.configuration;

import lombok.Getter;

public class ActiveConfigurationContext {

    @Getter
    private static Configuration instance = ConfigurationSource.createFromDefaultProfile();

    public static void switchToUser(final String userId) {
        instance = ConfigurationSource.createFromProfile(userId);
    }

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
