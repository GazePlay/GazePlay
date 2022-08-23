package net.gazeplay.ui.scenes.userselect;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.FileUtils;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class UserProfileContextTest {

    @Mock
    private Configuration mockConfig;

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @Mock
    private Scene mockScene;

    @Mock
    private Stage mockStage;

    private static String profileRoot = "profiles";
    private static String profileDirectory = "test1";
    private static String exampleFile = "test.txt";
    private static String hiddenDirectory = ".hidden";

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;
    private final Dimension2D screenDimension = new Dimension2D(1920, 1080);

    @BeforeAll
    static void setupMockProfiles() throws IOException {
        File rootDir = new File(profileRoot);

        File hiddenDir = new File(rootDir, hiddenDirectory);
        hiddenDir.mkdirs();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            Files.setAttribute(hiddenDir.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
        }

        File profileDir = new File(rootDir, profileDirectory);
        profileDir.mkdirs();

        File realFile = new File(rootDir, exampleFile);
        realFile.createNewFile();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> screenDimension);
        when(mockGazePlay.getPrimaryStage()).thenReturn(mockStage);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockTranslator.translate(anyString())).thenReturn("UserName");
        when(mockScene.getStylesheets()).thenReturn(FXCollections.observableArrayList());
    }

    @AfterAll
    static void tearDownMockProfiles() {
        new File(profileRoot, hiddenDirectory).delete();
        new File(profileRoot, profileDirectory).delete();
        new File(profileRoot, exampleFile).delete();
        new File(profileRoot).delete();
    }

    @Test
    void shouldFindAllUsersProfiles() {
        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getProfilesDirectory() {
                return new File(profileRoot);
            }
        };

        List<String> result = UserProfileContext.findAllUsersProfiles();

        assertTrue(result.contains(profileDirectory));
        assertFalse(result.contains(hiddenDirectory));
        assertFalse(result.contains(exampleFile));
    }

    @Test
    void shouldReturnEmptyListIfNoProfiles() {
        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getProfilesDirectory() {
                return new File("bad/path");
            }
        };

        List<String> result = UserProfileContext.findAllUsersProfiles();

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void shouldCreateUserPickerChoicePane() {
        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getProfilesDirectory() {
                return new File(profileRoot);
            }
        };

        UserProfileContext context = new UserProfileContext(mockGazePlay);

        ScrollPane result = context.createUserPickerChoicePane(mockGazePlay);
        FlowPane panel = (FlowPane) result.getContent();

        assertEquals(3, panel.getChildren().size()); // Should render Default, New User and test1
    }

    @Test
    void shouldLookupProfilePicture() {
        when(mockConfig.getUserPicture()).thenReturn(localDataFolder + "bear.jpg");

        ImagePattern result = UserProfileContext.lookupProfilePicture(mockConfig);

        assertNotNull(result.getImage());
    }

    @Test
    void shouldGetDefaultProfilePicture() {
        when(mockConfig.getUserPicture()).thenReturn(null).thenReturn("someOtherFile.png");

        ImagePattern result1 = UserProfileContext.lookupProfilePicture(mockConfig);
        ImagePattern result2 = UserProfileContext.lookupProfilePicture(mockConfig);

        assertTrue(result1.getImage().getUrl().contains("DefaultUser.png"));
        assertTrue(result2.getImage().getUrl().contains("DefaultUser.png"));
    }

    @Test
    void shouldCreateDefaultUser() {
        when(mockTranslator.translate("DefaultUser")).thenReturn("Default User");
        AtomicBoolean called = new AtomicBoolean(false);

        new MockUp<BackgroundMusicManager>() {
            @mockit.Mock
            public void onConfigurationChanged() {
                called.set(true);
            }
        };

        UserProfileContext context = new UserProfileContext(mockGazePlay);
        FlowPane choicePanel = new FlowPane();
        ImagePattern imagePattern = new ImagePattern(new Image("bear.jpg"));

        User result = context.createUser(mockGazePlay, choicePanel, "Default User",
            imagePattern, false, false, screenDimension);
        BorderPane content = (BorderPane) result.getChildren().get(0);
        Rectangle picture = (Rectangle) content.getCenter();

        assertEquals("Default User", result.getName());
        assertNotNull(picture.getFill());

        content.fireEvent(TestingUtils.clickOnTarget(content));
        verify(mockGazePlay).onReturnToMenu();
        assertTrue(called.get());
    }

    @Test
    void shouldCreateNamedUser(@Mocked ActiveConfigurationContext configurationContext) {
        when(mockTranslator.translate("DefaultUser")).thenReturn("Default User");
        AtomicBoolean called = new AtomicBoolean(false);

        new MockUp<BackgroundMusicManager>() {
            @mockit.Mock
            public void onConfigurationChanged() {
                called.set(true);  // Really, this should be the same as the ActiveConfigurationContext
                // but I just couldn't make it work...
            }
        };

        UserProfileContext context = new UserProfileContext(mockGazePlay);
        FlowPane choicePanel = new FlowPane();
        ImagePattern imagePattern = new ImagePattern(new Image("bear.jpg"));

        User result = context.createUser(mockGazePlay, choicePanel, "Test User",
            imagePattern, true, false, screenDimension);
        BorderPane content = (BorderPane) result.getChildren().get(0);
        Rectangle picture = (Rectangle) content.getCenter();
        VBox buttonBox = (VBox) result.getChildren().get(1);
        BorderPane editButton = (BorderPane) buttonBox.getChildren().get(0);
        BorderPane deleteButton = (BorderPane) buttonBox.getChildren().get(1);

        assertEquals("Test User", result.getName());
        assertNotNull(picture.getFill());
        assertTrue(deleteButton.getStyleClass().containsAll(List.of("gameChooserButton", "button")));
        assertTrue(editButton.getStyleClass().containsAll(List.of("gameChooserButton", "button")));

        content.fireEvent(TestingUtils.clickOnTarget(content));
        verify(mockGazePlay).onReturnToMenu();
        assertTrue(called.get());

        new Verifications() {{
            ActiveConfigurationContext.switchToUser("Test User");
        }};
    }

    @Test
    void shouldCreateAddNewUser() throws InterruptedException {
        when(mockTranslator.translate("NewUser")).thenReturn("New User");

        UserProfileContext context = new UserProfileContext(mockGazePlay);
        FlowPane choicePanel = new FlowPane();
        ImagePattern imagePattern = new ImagePattern(new Image("bear.jpg"));

        User result = context.createUser(mockGazePlay, choicePanel, "Add User",
            imagePattern, false, true, screenDimension);
        BorderPane content = (BorderPane) result.getChildren().get(0);
        Rectangle picture = (Rectangle) content.getCenter();

        assertEquals("Add User", result.getName());
        assertNotNull(picture.getFill());

        Platform.runLater(() -> content.fireEvent(TestingUtils.clickOnTarget(content)));
        TestingUtils.waitForRunLater();

        assertTrue(context.getRoot().getEffect() instanceof BoxBlur);
    }

    @Test
    void shouldThrowExceptionIfUserNameIsEmpty() {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        FlowPane choicePanel = new FlowPane();
        ImagePattern imagePattern = new ImagePattern(new Image("bear.jpg"));

        assertThrows(IllegalArgumentException.class,
            () -> context.createUser(mockGazePlay, choicePanel, "", imagePattern, false, false, screenDimension));
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void shouldCreateExitButton() {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        BorderPane topPane = (BorderPane) context.getRoot().getTop();
        HBox topRightPane = (HBox) topPane.getRight();

        CustomButton exitButton = (CustomButton) topRightPane.getChildren().get(0);

        exitButton.fireEvent(TestingUtils.clickOnTarget(exitButton));
    }

    @Test
    void shouldCreateRemoveDialog() throws InterruptedException {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        when(mockStage.getHeight()).thenReturn(1080d);
        when(mockStage.getWidth()).thenReturn(1920d);
        when(mockStage.getScene()).thenReturn(mockScene);
        when(mockScene.getRoot()).thenReturn(new BorderPane());

        FlowPane choicePanel = new FlowPane();
        User user = new User("test");

        choicePanel.getChildren().add(user);

        Platform.runLater(() -> {
            Stage result = context.createRemoveDialog(mockStage, choicePanel, user, () -> screenDimension);
            ScrollPane scroller = (ScrollPane) result.getScene().getRoot();
            HBox choicePane = (HBox) scroller.getContent();
            assertEquals(2, choicePane.getChildren().size());

            Button yes = (Button) choicePane.getChildren().get(0);
            Button no = (Button) choicePane.getChildren().get(1);

            yes.fireEvent(TestingUtils.clickOnTarget(yes));
            assertFalse(choicePanel.getChildren().contains(user));

            result.show();
            no.fireEvent(TestingUtils.clickOnTarget(no));
            assertFalse(result.isShowing());
        });
        TestingUtils.waitForRunLater();
    }

    @Test
    void shouldCreateExistingUserDialog() throws InterruptedException {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        when(mockStage.getHeight()).thenReturn(1080d);
        when(mockStage.getWidth()).thenReturn(1920d);
        when(mockStage.getScene()).thenReturn(mockScene);
        when(mockScene.getRoot()).thenReturn(new BorderPane());
        when(mockTranslator.translate("ChooseImage")).thenReturn("Choose Image");

        FlowPane choicePanel = new FlowPane();
        User user = new User("test");

        choicePanel.getChildren().add(user);

        Platform.runLater(() -> {
            Stage result = context.createDialog(mockGazePlay, mockStage, choicePanel, user, false, screenDimension);
            ScrollPane scroller = (ScrollPane) result.getScene().getRoot();
            VBox choicePane = (VBox) scroller.getContent();
            HBox imageField = (HBox) choicePane.getChildren().get(0);

            Button chooseImage = (Button) imageField.getChildren().get(1);
            Button reset = (Button) imageField.getChildren().get(2);

            reset.fireEvent(TestingUtils.clickOnTarget(reset));
            assertNull(chooseImage.getGraphic());
            assertEquals("Choose Image", chooseImage.getText());
        });
        TestingUtils.waitForRunLater();
    }

    @Test
    void shouldCreateNewUserDialog() throws InterruptedException {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        when(mockStage.getHeight()).thenReturn(1080d);
        when(mockStage.getWidth()).thenReturn(1920d);
        when(mockStage.getScene()).thenReturn(mockScene);
        when(mockScene.getRoot()).thenReturn(new BorderPane());
        when(mockTranslator.translate("ChooseImage")).thenReturn("Choose Image");

        FlowPane choicePanel = new FlowPane();
        User user = new User("test");

        choicePanel.getChildren().add(user);

        Platform.runLater(() -> {
            Stage result = context.createDialog(mockGazePlay, mockStage, choicePanel, user, true, screenDimension);
            ScrollPane scroller = (ScrollPane) result.getScene().getRoot();
            VBox choicePane = (VBox) scroller.getContent();
            HBox imageField = (HBox) choicePane.getChildren().get(0);
            HBox nameField = (HBox) choicePane.getChildren().get(1);
            TextField tf = (TextField) nameField.getChildren().get(1);

            tf.setText("New Name");

            Button chooseImage = (Button) imageField.getChildren().get(1);
            Button reset = (Button) imageField.getChildren().get(2);
            Button ok = (Button) choicePane.getChildren().get(3);

            assertEquals(2, nameField.getChildren().size());

            reset.fireEvent(TestingUtils.clickOnTarget(reset));
            assertNull(chooseImage.getGraphic());
            assertEquals("Choose Image", chooseImage.getText());

            ok.fireEvent(TestingUtils.clickOnTarget(ok));
            assertTrue(choicePanel.getChildren().contains(user));
            assertEquals("New Name", ((User) choicePanel.getChildren().get(0)).getName());
        });
        TestingUtils.waitForRunLater();

        //Tidy Up
        File createdDir = GazePlayDirectories.getUserProfileDirectory("New Name");
        FileUtils.deleteDirectoryRecursively(createdDir);
    }

    @Test
    void shouldNotCreateExistingUser() throws InterruptedException {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        when(mockStage.getHeight()).thenReturn(1080d);
        when(mockStage.getWidth()).thenReturn(1920d);
        when(mockStage.getScene()).thenReturn(mockScene);
        when(mockScene.getRoot()).thenReturn(new BorderPane());
        when(mockTranslator.translate("ChooseImage")).thenReturn("Choose Image");
        when(mockTranslator.translate("DefaultUser")).thenReturn("Default User");
        when(mockTranslator.translate("AlreadyUsed")).thenReturn("Already Used");

        FlowPane choicePanel = new FlowPane();
        User user = new User("test");

        choicePanel.getChildren().add(user);

        Platform.runLater(() -> {
            Stage result = context.createDialog(mockGazePlay, mockStage, choicePanel, user, true, screenDimension);
            ScrollPane scroller = (ScrollPane) result.getScene().getRoot();
            VBox choicePane = (VBox) scroller.getContent();
            HBox imageField = (HBox) choicePane.getChildren().get(0);
            HBox nameField = (HBox) choicePane.getChildren().get(1);
            TextField tf = (TextField) nameField.getChildren().get(1);

            tf.setText("Default User");

            Button chooseImage = (Button) imageField.getChildren().get(1);
            Button reset = (Button) imageField.getChildren().get(2);
            Button ok = (Button) choicePane.getChildren().get(3);

            assertEquals(2, nameField.getChildren().size());

            reset.fireEvent(TestingUtils.clickOnTarget(reset));
            assertNull(chooseImage.getGraphic());
            assertEquals("Choose Image", chooseImage.getText());

            ok.fireEvent(TestingUtils.clickOnTarget(ok));
            assertTrue(choicePanel.getChildren().contains(user));
            assertEquals("Already Used", ((Text) choicePane.getChildren().get(4)).getText());
        });
        TestingUtils.waitForRunLater();
    }
}
