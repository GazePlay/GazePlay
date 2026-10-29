package net.gazeplay.commons.utils.games;

import net.gazeplay.commons.VersionInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuUtilsTest {

    @Test
    void shouldGiveTheVersion() {
        // The version comes from the jar manifest, which is present or not depending on how the tests are launched
        String expectedVersion = VersionInfo.findVersionInfo(VersionInfo.artifactId)
            .map(version -> "GazePlay Learning " + version)
            .orElse("GazePlay Learning unreleased version");

        assertEquals(expectedVersion, MenuUtils.versionText());
    }

    @Test
    void givenLicenseFileIsFoundShouldGiveTheLicense() {
        assertTrue(MenuUtils.licenseText().contains("test licence"));
    }
}
