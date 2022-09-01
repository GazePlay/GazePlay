package net.gazeplay.commons.gamevariants.difficulty;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SourceSetTest {

    @Test
    void givenValidFileShouldReadSuccessfully() {
        try {
            new SourceSet("source-sets.json");
        } catch (FileNotFoundException fe) {
            fail();
        }
    }

    @Test
    void givenValidFileGivenValidDifficultyShouldReturnList() throws FileNotFoundException {
        SourceSet sourceSet = new SourceSet("source-sets.json");
        Set<String> dirs = sourceSet.getResources(Difficulty.NORMAL.toString());

        assertTrue(dirs.contains("grey"));
    }

    @Test
    void givenValidFileGivenInvalidDifficultyShouldReturnEmptyList() throws FileNotFoundException {
        SourceSet sourceSet = new SourceSet("source-sets.json");
        Set<String> dirs = sourceSet.getResources(Difficulty.HARD.toString());

        assertEquals(0, dirs.size());
    }

    @Test
    void givenInvalidFileShouldThrowFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> {
            new SourceSet("invalid-file.json");
        });
    }

}
