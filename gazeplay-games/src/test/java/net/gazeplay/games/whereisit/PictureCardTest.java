package net.gazeplay.games.whereisit;

import javafx.scene.image.Image;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
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
        when(mockWhereIsIt.getGameType()).thenReturn(WhereIsItGameType.SHAPES_EASY);
    }

    @Test
    void shouldCreateImageViewFromResource() {
        String resource = "images/blue/blue.jpg";
        PictureCard pictureCard = new PictureCard(2, 2, 2, 2, mockGameContext, true,
            resource, mockStats, mockWhereIsIt);
        Image image = pictureCard.getImageRectangle().getImage();
        assert image.getUrl().contains(resource);
    }

    /**
     * Tester's feedback of 20/11/2026, point 29: the card is white in both themes, and receives the mouse on all its
     * surface, not only on the picture.
     */
    @Test
    void shouldPutThePictureOnAWhiteCardChosenEverywhere() {
        PictureCard pictureCard = new PictureCard(2, 2, 200, 200, mockGameContext, true,
            "images/blue/blue.jpg", mockStats, mockWhereIsIt);
        assertTrue(pictureCard.getBackground().getStyle().contains("-fx-fill: #FFFFFF"),
            pictureCard.getBackground().getStyle());
        assertFalse(pictureCard.getBackground().isMouseTransparent());
    }

    @Test
    void shouldCreateImageViewFromFile() {
        String resource = "file:" + System.getProperty("user.dir") + "src/test/resources/images/blue/blue.jpg";
        PictureCard pictureCard = new PictureCard(2, 2, 2, 2, mockGameContext, true,
            resource, mockStats, mockWhereIsIt);
        Image image = pictureCard.getImageRectangle().getImage();
        assert image.getUrl().contains("blue.jpg");
    }

    /**
     * A wrong picture (16/11/2026): it costs a life, cannot be chosen again, stays a little visible; then the question
     * can be heard again; the last life loses the game.
     */
    @Test
    void shouldCostALifeAndLockAWrongPicture() throws Exception {
        final PictureCard wrong = WaitForAsyncUtils.asyncFx(() -> new PictureCard(2, 2, 200, 200, mockGameContext,
            false, "images/blue/blue.jpg", mockStats, mockWhereIsIt)).get();
        assertTrue(wrong.isSelectable());
        WaitForAsyncUtils.asyncFx(() -> {
            wrong.onFixationCompleted();
            return null;
        }).get();
        verify(mockWhereIsIt).wrongPictureChosen();
        assertFalse(wrong.isSelectable());
        verify(mockWhereIsIt, timeout(3000)).showReplayHint();

        final PictureCard last = WaitForAsyncUtils.asyncFx(() -> new PictureCard(2, 2, 200, 200, mockGameContext,
            false, "images/blue/blue.jpg", mockStats, mockWhereIsIt)).get();
        when(mockWhereIsIt.wrongPictureChosen()).thenReturn(true);
        WaitForAsyncUtils.asyncFx(() -> {
            last.onFixationCompleted();
            return null;
        }).get();
        verify(mockWhereIsIt, timeout(3000)).lose();
    }

    @Test
    void shouldCreateErrorMark() {
        String resource = "images/blue/blue.jpg";
        PictureCard pictureCard = new PictureCard(2, 2, 2, 2, mockGameContext, true,
            resource, mockStats, mockWhereIsIt);
        assert pictureCard.getErrorMark() != null;
    }
}
