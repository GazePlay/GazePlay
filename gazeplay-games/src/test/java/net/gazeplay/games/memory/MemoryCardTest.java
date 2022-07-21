package net.gazeplay.games.memory;


import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
class MemoryCardTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext mockGameContext;

    @Mock
    Stats mockStats;

    @Mock
    Memory mockMemory;


    @Mock
    Configuration mockConfig;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(mockGameContext.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getFixationLength()).thenReturn(2);
    }

    @Test
    void shouldCreateCardFromResourceInOpenMode() {
        String resource = "images/blue/blue.jpg";
        Image img = new Image(resource);
        MemoryCard memoryCard = new MemoryCard(2, 2, 2, 2, img, 2,
            mockGameContext, mockStats, mockMemory, mockConfig.getFixationLength(), true);
        Image image = ((ImagePattern) memoryCard.getImageRectangle().getFill()).getImage();
        assert image.getUrl().contains(resource);
    }

    @Test
    void shouldCreateCardFromFileInOpenMode() {
        String resource = "file:" + System.getProperty("user.dir") + "src/test/resources/images/blue/blue.jpg";
        Image img = new Image(resource);
        MemoryCard memoryCard = new MemoryCard(2, 2, 2, 2, img, 2,
            mockGameContext, mockStats, mockMemory, mockConfig.getFixationLength(), true);
        Image image = ((ImagePattern) memoryCard.getImageRectangle().getFill()).getImage();
        assert image.getUrl().contains("blue.jpg");
    }

    @Test
    void shouldCreateCardFromFileInNotOpenModeAndFillWithBackCardImage() {
        String resource = "file:" + System.getProperty("user.dir") + "src/test/resources/images/blue/blue.jpg";
        Image img = new Image(resource);
        MemoryCard memoryCard = new MemoryCard(2, 2, 2, 2, img, 2,
            mockGameContext, mockStats, mockMemory, mockConfig.getFixationLength(), false);
        Image image = ((ImagePattern) memoryCard.getImageRectangle().getFill()).getImage();
        assert image.getUrl().contains("data/magiccards/images/red-card-game.png");
    }

    @Test
    void shouldCreateCardFromResourceInNotOpenModeAndFillWithBackCardImage() {
        String resource = "images/blue/blue.jpg";
        Image img = new Image(resource);
        MemoryCard memoryCard = new MemoryCard(2, 2, 2, 2, img, 2,
            mockGameContext, mockStats, mockMemory, mockConfig.getFixationLength(), false);
        Image image = ((ImagePattern) memoryCard.getImageRectangle().getFill()).getImage();
        assert image.getUrl().contains("data/magiccards/images/red-card-game.png");
    }


}
