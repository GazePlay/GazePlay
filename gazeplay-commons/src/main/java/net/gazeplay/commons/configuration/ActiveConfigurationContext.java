package net.gazeplay.commons.configuration;

import lombok.Getter;
import lombok.Setter;

public class ActiveConfigurationContext {
    @Getter
    @Setter
    private static Configuration instance = ConfigurationSource.createFromDefaultProfile();
}
