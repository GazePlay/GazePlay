package net.gazeplay.games.mediaPlayer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import net.gazeplay.components.AbstractGazeIndicator;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class MediaButtonTest {

    @Mock
    AbstractGazeIndicator progressIndicator;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldNotSetUpGraphicForMediaButtonWithNoSetUpImageMethodCall() {
        MediaButton mediaButton = new MediaButton(500, 500);
        Assert.assertNull(mediaButton.getButton().getGraphic());
    }

    @Test
    void shouldNotSetUpGraphicForMediaButtonWithNoMediaFileSet() {
        MediaButton mediaButton = new MediaButton(500, 500);
        mediaButton.setupImage();
        Assert.assertNull(mediaButton.getButton().getGraphic());
    }

    @Test
    void shouldSetUpNullGraphicForMediaButtonWithMediaFileSetUpWithNoImagePath() {
        MediaButton mediaButton = new MediaButton(500, 500);
        MediaFile mediaFile = new MediaFile("URL", "https://github.com/GazePlay/GazePlay", "gazeplayTest", null);
        mediaButton.setMediaFile(mediaFile);
        mediaButton.setupImage();
        Assert.assertNull(mediaButton.getButton().getGraphic());
    }

    @Test
    void shouldSetUpGraphicForMediaButtonWithMediaFileSetUpWithImagePath() {
        MediaButton mediaButton = new MediaButton(500, 500);
        MediaFile mediaFile = new MediaFile("URL", "https://github.com/GazePlay/GazePlay", "gazeplayTest", "src/test/resources/images/blue/blue.jpg");
        mediaButton.setMediaFile(mediaFile);
        mediaButton.setupImage();
        ImageView graphicImageView = (ImageView) mediaButton.getButton().getGraphic();
        Assert.assertTrue(graphicImageView.getImage().getUrl().contains("blue.jpg"));
    }

    @Test
    void shouldKeepEmptyEventHandlersForMediaButtonWithNoSetUpEventMethodCall() {
        MediaButton mediaButton = new MediaButton(500, 500);
        Assert.assertEquals(mediaButton.getClickEvent(), MediaButton.emptyEvent);
        Assert.assertEquals(mediaButton.getEnterEvent(), MediaButton.emptyEvent);
        Assert.assertEquals(mediaButton.getExitEvent(), MediaButton.emptyEvent);
    }

    @Test
    void shouldCreateEventsHandlersForMediaButtonWithSetUpEventMethodCall() {
        MediaButton mediaButton = new MediaButton(500, 500);
        EventHandler<ActionEvent> testEvent = e -> {
            String testString = "test";
        };
        mediaButton.setupEvent(testEvent, progressIndicator);
        Assert.assertNotEquals(mediaButton.getClickEvent(), (MediaButton.emptyEvent));
        Assert.assertNotEquals(mediaButton.getEnterEvent(), (MediaButton.emptyEvent));
        Assert.assertNotEquals(mediaButton.getExitEvent(), (MediaButton.emptyEvent));
    }
}
