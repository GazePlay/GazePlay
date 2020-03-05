package net.gazeplay.games.mediaPlayer;


import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static java.nio.file.StandardOpenOption.APPEND;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class MediaFileReaderTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext mockGameContext;

    private static File testDir = new File("src/test/resources/testTestForMediaFileReader");

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCreatePlayerListCSVFileOnMediaFileReaderConstruct() {
        createTempTestDir();
        MediaFileReader mfr = new MediaFileReader(mockGameContext) {
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };
        assertNotNull(mfr);
        assertTrue((new File(testDir, "playerList.csv")).exists());
        FileUtils.deleteDirectoryRecursively(testDir);
    }

    @Test
    void shouldReadPlayerListCSVCorrectly() throws IOException {
        createTempTestDir();
        File player = new File(testDir, "playerList.csv");
        if (player.createNewFile() || player.exists()) {
            OutputStream fileOutputStream = Files.newOutputStream(player.toPath(), StandardOpenOption.CREATE, APPEND);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            bw.write("URL,https://www.test1.fr/,test1,");
            bw.write("\nMEDIA,images/black/black.jpg,test2,");
            bw.write("\nURL,https://www.test3.fr/,test3,images/black/black.jpg");
            bw.write("\nMEDIA,images/black/black.jpg,test4,images/black/black.jpg");
            bw.close();
        }

        MediaFileReader mfr = new MediaFileReader(mockGameContext) {
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };

        assertEquals("URL", mfr.getMediaList().get(0).getType());
        assertEquals("https://www.test1.fr/", mfr.getMediaList().get(0).getPath());
        assertEquals("test1", mfr.getMediaList().get(0).getName());
        assertNull(mfr.getMediaList().get(0).getImagepath());

        assertEquals("MEDIA", mfr.getMediaList().get(1).getType());
        assertTrue(mfr.getMediaList().get(1).getPath().contains("black"));
        assertEquals("test2", mfr.getMediaList().get(1).getName());
        assertNull(mfr.getMediaList().get(1).getImagepath());

        assertEquals("URL", mfr.getMediaList().get(2).getType());
        assertEquals("https://www.test3.fr/", mfr.getMediaList().get(2).getPath());
        assertEquals("test3", mfr.getMediaList().get(2).getName());
        assertTrue(mfr.getMediaList().get(2).getImagepath().contains("black"));

        assertEquals("MEDIA", mfr.getMediaList().get(3).getType());
        assertTrue(mfr.getMediaList().get(3).getPath().contains("black"));
        assertEquals("test4", mfr.getMediaList().get(3).getName());
        assertTrue(mfr.getMediaList().get(3).getImagepath().contains("black"));
        FileUtils.deleteDirectoryRecursively(testDir);
    }

    @Test
    void shouldReturnMinusOneWhenThereIsNoMediaToDisplay() {
        createTempTestDir();
        MediaFileReader mfr = new MediaFileReader(mockGameContext) {
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };

        assertEquals(mfr.getMediaList().size(), 0);
        assertEquals(mfr.getIndexOfFirstToDisplay(), -1);
        FileUtils.deleteDirectoryRecursively(testDir);
    }

    @Test
    void shouldAddMedia() {
        createTempTestDir();
        MediaFileReader mfr = new MediaFileReader(mockGameContext) {
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };
        MediaFile mediaTypeMediaFile = new MediaFile("MEDIA", "https://www.testMEDIA.fr/", "testMEDIA", "images/green/green.jpg");
        mfr.addMedia(mediaTypeMediaFile);
        assertTrue(mfr.getMediaList().contains(mediaTypeMediaFile));


        MediaFile urlTypeMediaFile = new MediaFile("URL", "https://www.testURL.fr/", "testURL", "images/green/green.jpg");
        mfr.addMedia(urlTypeMediaFile);
        assertTrue(mfr.getMediaList().contains(mediaTypeMediaFile));
        assertTrue(mfr.getMediaList().contains(urlTypeMediaFile));
        FileUtils.deleteDirectoryRecursively(testDir);
    }

    @Test
    void shouldReturnGoodValuesForFirstToDisplayAfterAdd() {
        createTempTestDir();
        MediaFileReader mfr = new MediaFileReader(mockGameContext) {
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };
        MediaFile mediaTypeMediaFile = new MediaFile("MEDIA", "https://www.testMEDIA.fr/", "testMEDIA", "images/green/green.jpg");
        mfr.addMedia(mediaTypeMediaFile);

        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        MediaFile urlTypeMediaFile = new MediaFile("URL", "https://www.testURL.fr/", "testURL", "images/green/green.jpg");
        mfr.addMedia(urlTypeMediaFile);
        assertEquals(1, mfr.getIndexOfFirstToDisplay());
        mfr.previous();
        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        mfr.previous();
        assertEquals(1, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(1, mfr.getIndexOfFirstToDisplay());

        FileUtils.deleteDirectoryRecursively(testDir);
    }

    @Test
    void shouldReturnGoodValuesForFirstToDisplayFromSave() throws IOException {
        createTempTestDir();
        File player = new File(testDir, "playerList.csv");
        if (player.createNewFile() || player.exists()) {
            OutputStream fileOutputStream = Files.newOutputStream(player.toPath(), StandardOpenOption.CREATE, APPEND);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            bw.write("URL,https://www.test1.fr/,test1,");
            bw.write("\nMEDIA,images/black/black.jpg,test2,");
            bw.close();
        }

        MediaFileReader mfr = new MediaFileReader(mockGameContext) {
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };

        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        mfr.previous();
        assertEquals(1, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(1, mfr.getIndexOfFirstToDisplay());

        FileUtils.deleteDirectoryRecursively(testDir);
    }

    @Test
    void shouldReturnGoodValuesForFirstToDisplayFromSaveAfterAdd() throws IOException {
        createTempTestDir();
        File player = new File(testDir, "playerList.csv");
        if (player.createNewFile() || player.exists()) {
            OutputStream fileOutputStream = Files.newOutputStream(player.toPath(), StandardOpenOption.CREATE, APPEND);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            bw.write("URL,https://www.test1.fr/,test1,");
            bw.write("\nMEDIA,images/black/black.jpg,test2,");
            bw.close();
        }

        MediaFileReader mfr = new MediaFileReader(mockGameContext) {
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };

        MediaFile mediaTypeMediaFile = new MediaFile("MEDIA", "https://www.testMEDIA.fr/", "testMEDIA", "images/green/green.jpg");
        mfr.addMedia(mediaTypeMediaFile);

        MediaFile urlTypeMediaFile = new MediaFile("URL", "https://www.testURL.fr/", "testURL", "images/green/green.jpg");
        mfr.addMedia(urlTypeMediaFile);

        assertEquals(3, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(1, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(2, mfr.getIndexOfFirstToDisplay());
        mfr.next();
        assertEquals(3, mfr.getIndexOfFirstToDisplay());
        mfr.previous();
        assertEquals(2, mfr.getIndexOfFirstToDisplay());
        mfr.previous();
        assertEquals(1, mfr.getIndexOfFirstToDisplay());
        mfr.previous();
        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        mfr.previous();
        assertEquals(3, mfr.getIndexOfFirstToDisplay());

        FileUtils.deleteDirectoryRecursively(testDir);
    }

    private void createTempTestDir() {
        if (testDir.mkdirs()) {
            assertTrue(testDir.exists());
        }
    }

}
