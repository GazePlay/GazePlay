package net.gazeplay.commons.configuration;

import lombok.Getter;

public class ActiveConfigurationContext {

    @Getter
    private static Configuration instance = ConfigurationSource.createFromDefaultProfile();

    public static void switchToUser(String userId) {
        instance = ConfigurationSource.createFromProfile(userId);
    }

    public static void switchToDefaultUser() {
        instance = ConfigurationSource.createFromDefaultProfile();
    }

}
