package net.gazeplay.commons.gamevariants.difficulty;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SourceSetTest {

    @Test
    void givenValidFile_shouldReadSuccessfully() {
        try {
            new SourceSet("source-sets.json");
        } catch (FileNotFoundException fe) {
            fail();
        }
    }

    @Test
    void givenValidFile_givenValidDifficulty_shouldReturnList() throws FileNotFoundException {
        SourceSet sourceSet = new SourceSet("source-sets.json");
        Set<String> dirs = sourceSet.getResources(Difficulty.NORMAL);

        assertTrue(dirs.contains("grey"));
    }

    @Test
    void givenValidFile_givenInvalidDifficulty_shouldReturnEmptyList() throws FileNotFoundException {
        SourceSet sourceSet = new SourceSet("source-sets.json");
        Set<String> dirs = sourceSet.getResources(Difficulty.HARD);

        assertEquals(0, dirs.size());
    }

    @Test
    void givenInvalidFile_shouldThrowFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> {
            new SourceSet("invalid-file.json");
        });
    }

}
