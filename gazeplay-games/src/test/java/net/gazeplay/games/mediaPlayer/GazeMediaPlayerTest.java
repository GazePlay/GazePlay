package net.gazeplay.games.mediaPlayer;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import net.gazeplay.components.StackPaneButton;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class GazeMediaPlayerTest {

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCreateButtonOfSimpleStackPaneButton() {
        StackPaneButton stackPaneButton = GazeMediaPlayer.createSimpleStackPaneButton(500, 500);
        Assert.assertNotNull(stackPaneButton.getButton());
    }

    @Test
    void shouldCreateButtonOfTextStackPaneButton() {
        StackPaneButton stackPaneButton = GazeMediaPlayer.createTextStackPaneButton("Test", 500, 500);
        Assert.assertNotNull(stackPaneButton.getButton());
        Assert.assertEquals("Test", stackPaneButton.getButton().getText());
    }

    @Test
    void shouldCreateButtonOfGraphicStackPaneButton() {
        StackPaneButton stackPaneButton = GazeMediaPlayer.createGraphicStackPaneButton(500, 500, "images/blue/blue.jpg");
        ImageView graphicImageView = (ImageView) stackPaneButton.getButton().getGraphic();
        Assert.assertNotNull(stackPaneButton.getButton());
        Assert.assertTrue(graphicImageView.getImage().getUrl().contains("blue.jpg"));
    }

    @Test
    void shouldCreateTextButtonWhenCallingMediaButton() {
        Button button = GazeMediaPlayer.createMediaButton("Test", 500, 500);
        Assert.assertEquals("Test", button.getText());
    }
}
