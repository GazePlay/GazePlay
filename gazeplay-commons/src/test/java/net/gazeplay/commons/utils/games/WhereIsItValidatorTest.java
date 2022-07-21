package net.gazeplay.commons.utils.games;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class WhereIsItValidatorTest {

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep
            + "data";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnTrueIfTheFileIsAnImageFile() {
        assertTrue(WhereIsItVaildator.fileIsImageFile(new File(localDataFolder + "/common/default/images/bear.jpg")));
    }

    @Test
    void shouldReturnFalseIfTheFileIsNotAnImageFile() {
        assertFalse(WhereIsItVaildator.fileIsImageFile(new File(localDataFolder + "/music/song.mp3")));
    }

    @Test
    void shouldReturnFalseIfTheFileDoesntExist() {
        assertFalse(WhereIsItVaildator.fileIsImageFile(new File(localDataFolder + "unknown")));
    }

    @Test
    void shouldReturnTheGoodNumberOfValidDirectoriesWhenAllValid() {
        LinkedList<File> tempDirList = new LinkedList<File>();
        assertEquals(3, WhereIsItVaildator.getNumberOfValidDirectories(localDataFolder + "/whereisit/all_valid", tempDirList));
        boolean containsBears = false;
        boolean containsCats = false;
        boolean containsDogs = false;
        for (int i = 0; i < 3; i++) {
            switch (tempDirList.get(i).getName()) {
                case "bears":
                    containsBears = true;
                    break;
                case "cats":
                    containsCats = true;
                    break;
                case "dogs":
                    containsDogs = true;
                    break;
                default:
                    break;
            }
        }
        assertTrue(containsBears && containsCats && containsDogs);
    }

    @Test
    void shouldReturnTheGoodNumberOfValidDirectoriesWhenAllValidWithUselessFiles() {
        LinkedList<File> tempDirList = new LinkedList<File>();
        assertEquals(3, WhereIsItVaildator.getNumberOfValidDirectories(localDataFolder + "/whereisit/all_valid_with_useless_files", tempDirList));
        boolean containsBears = false;
        boolean containsCats = false;
        boolean containsDogs = false;
        for (int i = 0; i < 3; i++) {
            switch (tempDirList.get(i).getName()) {
                case "bears":
                    containsBears = true;
                    break;
                case "cats":
                    containsCats = true;
                    break;
                case "dogs":
                    containsDogs = true;
                    break;
                default:
                    break;
            }
        }
        assertTrue(containsBears && containsCats && containsDogs);
    }

    @Test
    void shouldReturnTheGoodNumberOfValidDirectoriesWhenAllInvalid() {
        LinkedList<File> tempDirList = new LinkedList<File>();
        assertEquals(0, WhereIsItVaildator.getNumberOfValidDirectories(localDataFolder + "/whereisit/all_invalid", tempDirList));
        assertEquals(tempDirList.size(), 0);
    }

    @Test
    void shouldReturnTheGoodNumberOfValidDirectoriesWhenSomeValid() {
        LinkedList<File> tempDirList = new LinkedList<File>();
        assertEquals(1, WhereIsItVaildator.getNumberOfValidDirectories(localDataFolder + "/whereisit/some_valid", tempDirList));
        assertEquals(tempDirList.get(0).getName(), "bears");
    }
}
