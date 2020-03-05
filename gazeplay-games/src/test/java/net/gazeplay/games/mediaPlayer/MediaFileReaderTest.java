package net.gazeplay.games.mediaPlayer;


import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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
    void shouldCreatePlayerListCSVFileOnMediaFileReaderConstruct() throws IOException {
        testDir.mkdirs();
        MediaFileReader mfr = new MediaFileReader(mockGameContext){
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };
        assertTrue((new File(testDir,"playerList.csv")).exists());
        FileUtils.deleteDirectory(testDir);
    }

    @Test
    void shouldReadPlayerListCSVCorrectly() throws IOException {
        testDir.mkdirs();
        File player = new File(testDir,"playerList.csv");
        player.createNewFile();
        FileWriter myWriter = new FileWriter(player);
        myWriter.write("URL,https://www.test1.fr/,test1,");
        myWriter.write("\nMEDIA,images/black/black.jpg,test2,");
        myWriter.write("\nURL,https://www.test3.fr/,test3,images/black/black.jpg");
        myWriter.write("\nMEDIA,images/black/black.jpg,test4,images/black/black.jpg");
        myWriter.close();

        MediaFileReader mfr = new MediaFileReader(mockGameContext){
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
        FileUtils.deleteDirectory(testDir);
    }

    @Test
    void shouldReturnMinusOneWhenThereIsNoMediaToDisplay() throws IOException {
        testDir.mkdirs();
        MediaFileReader mfr = new MediaFileReader(mockGameContext){
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };

        assertEquals(mfr.getMediaList().size(), 0);
        assertEquals(mfr.getIndexOfFirstToDisplay(), -1);
        FileUtils.deleteDirectory(testDir);
    }

    @Test
    void shouldAddMedia() throws IOException {
        testDir.mkdirs();
        MediaFileReader mfr = new MediaFileReader(mockGameContext){
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };
        MediaFile mediaTypeMediaFile = new MediaFile("MEDIA","https://www.testMEDIA.fr/","testMEDIA","images/green/green.jpg");
        mfr.addMedia(mediaTypeMediaFile);
        assertTrue(mfr.getMediaList().contains(mediaTypeMediaFile));


        MediaFile urlTypeMediaFile = new MediaFile("URL","https://www.testURL.fr/","testURL","images/green/green.jpg");
        mfr.addMedia(urlTypeMediaFile);
        assertTrue(mfr.getMediaList().contains(mediaTypeMediaFile));
        assertTrue(mfr.getMediaList().contains(urlTypeMediaFile));
        FileUtils.deleteDirectory(testDir);
    }

    @Test
    void shouldReturnGoodValuesForFirstToDisplayAfterAdd() throws IOException {
        testDir.mkdirs();
        MediaFileReader mfr = new MediaFileReader(mockGameContext){
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };
        MediaFile mediaTypeMediaFile = new MediaFile("MEDIA","https://www.testMEDIA.fr/","testMEDIA","images/green/green.jpg");
        mfr.addMedia(mediaTypeMediaFile);

        assertEquals(0, mfr.getIndexOfFirstToDisplay());
        MediaFile urlTypeMediaFile = new MediaFile("URL","https://www.testURL.fr/","testURL","images/green/green.jpg");
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

        FileUtils.deleteDirectory(testDir);
    }

    @Test
    void shouldReturnGoodValuesForFirstToDisplayFromSave() throws IOException {
        testDir.mkdirs();
        File player = new File(testDir,"playerList.csv");
        player.createNewFile();
        FileWriter myWriter = new FileWriter(player);
        myWriter.write("URL,https://www.test1.fr/,test1,");
        myWriter.write("\nMEDIA,images/black/black.jpg,test2,");
        myWriter.close();

        MediaFileReader mfr = new MediaFileReader(mockGameContext){
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

        FileUtils.deleteDirectory(testDir);
    }

    @Test
    void shouldReturnGoodValuesForFirstToDisplayFromSaveAfterAdd() throws IOException {
        testDir.mkdirs();
        File player = new File(testDir,"playerList.csv");
        player.createNewFile();
        FileWriter myWriter = new FileWriter(player);
        myWriter.write("URL,https://www.test1.fr/,test1,");
        myWriter.write("\nMEDIA,images/black/black.jpg,test2,");
        myWriter.close();

        MediaFileReader mfr = new MediaFileReader(mockGameContext){
            @Override
            public File getMediaPlayerDirectory() {
                return testDir;
            }
        };

        MediaFile mediaTypeMediaFile = new MediaFile("MEDIA","https://www.testMEDIA.fr/","testMEDIA","images/green/green.jpg");
        mfr.addMedia(mediaTypeMediaFile);

        MediaFile urlTypeMediaFile = new MediaFile("URL","https://www.testURL.fr/","testURL","images/green/green.jpg");
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

        FileUtils.deleteDirectory(testDir);
    }

}
