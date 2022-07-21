package net.gazeplay.games.whereisit;

import javafx.scene.image.Image;
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
class PictureCardTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext mockGameContext;

    @Mock
    Stats mockStats;

    @Mock
    WhereIsIt mockWhereIsIt;

    @Mock
    Configuration mockConfig;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(mockGameContext.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getFixationLength()).thenReturn(2);
    }

    @Test
    void shouldCreateImageViewFromResource() {
        String resource = "images/blue/blue.jpg";
        PictureCard pictureCard = new PictureCard(2, 2, 2, 2, mockGameContext, true,
            resource, mockStats, mockWhereIsIt);
        Image image = pictureCard.getImageRectangle().getImage();
        assert image.getUrl().contains(resource);
    }

    @Test
    void shouldCreateImageViewFromFile() {
        String resource = "file:" + System.getProperty("user.dir") + "src/test/resources/images/blue/blue.jpg";
        PictureCard pictureCard = new PictureCard(2, 2, 2, 2, mockGameContext, true,
            resource, mockStats, mockWhereIsIt);
        Image image = pictureCard.getImageRectangle().getImage();
        assert image.getUrl().contains("blue.jpg");
    }

    @Test
    void shouldCreateErrorImageRectangle() {
        String resource = "images/blue/blue.jpg";
        PictureCard pictureCard = new PictureCard(2, 2, 2, 2, mockGameContext, true,
            resource, mockStats, mockWhereIsIt);
        assert pictureCard.getErrorImageRectangle() != null;
    }
}
