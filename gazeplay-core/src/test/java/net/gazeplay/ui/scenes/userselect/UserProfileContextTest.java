package net.gazeplay.ui.scenes.userselect;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleWrapper;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FileUtils;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

    /**
     * Profiles of the temporary user.home used during tests (see build.gradle).
     */
    private static File profileRoot() {
        return GazePlayDirectories.getProfilesDirectory();
    }

    @BeforeEach
    void setupMockProfiles() throws IOException {
        File rootDir = profileRoot();

        File hiddenDir = new File(rootDir, hiddenDirectory);
        hiddenDir.mkdirs();
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            Files.setAttribute(hiddenDir.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);

        File profileDir = new File(rootDir, profileDirectory);
        profileDir.mkdirs();

        File realFile = new File(rootDir, exampleFile);
        realFile.createNewFile();
    }

    @AfterEach
    void tearDownMockProfiles() throws IOException {
        FileUtils.deleteDirectoryRecursively(profileRoot());
    }

    private final BackgroundMusicManager originalMusicManager = BackgroundMusicManager.getInstance();

    @AfterEach
    void restoreGlobalState() {
        ActiveConfigurationContext.switchToDefaultUser();
        BackgroundMusicManager.setInstance(originalMusicManager);
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> screenDimension);
        when(mockGazePlay.getPrimaryStage()).thenReturn(mockStage);
        // The static logo follows the height of the window
        when(mockStage.heightProperty()).thenReturn(new ReadOnlyDoubleWrapper(1080).getReadOnlyProperty());
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockTranslator.translate(anyString())).thenReturn("UserName");
        when(mockScene.getStylesheets()).thenReturn(FXCollections.observableArrayList());
    }

    @Test
    void shouldFindAllUsersProfiles() {
        List<String> result = UserProfileContext.findAllUsersProfiles();

        assertTrue(result.contains(profileDirectory));
        assertFalse(result.contains(hiddenDirectory));
        assertFalse(result.contains(exampleFile));
    }

    @Test
    void shouldReturnEmptyListIfNoProfiles() {
        FileUtils.deleteDirectoryRecursively(profileRoot());

        List<String> result = UserProfileContext.findAllUsersProfiles();

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void shouldCreateUserPickerChoicePane() {
        UserProfileContext context = new UserProfileContext(mockGazePlay);

        ScrollPane result = context.createUserPickerChoicePane(mockGazePlay);
        FlowPane panel = (FlowPane) result.getContent();

        assertEquals(3, panel.getChildren().size()); // Should render Default, New User and test1
    }

    /**
     * An existing picture. Formerly a missing file (src/test/resources/bear.jpg does not exist here), which passed only
     * because the default picture was returned.
     */
    @Test
    void shouldLookupProfilePicture() throws URISyntaxException {
        File picture = new File(getClass().getResource("/data/common/default/images/bear.jpg").toURI());
        when(mockConfig.getUserPicture()).thenReturn(picture.getAbsolutePath());

        ImagePattern result = UserProfileContext.lookupProfilePicture(mockConfig);

        assertNotNull(result.getImage());
    }

    /**
     * No picture, or a missing file: none (the card then shows a colored disc with an icon).
     */
    @Test
    void shouldGetNoProfilePicture() {
        when(mockConfig.getUserPicture()).thenReturn(null).thenReturn("someOtherFile.png");

        assertNull(UserProfileContext.lookupProfilePicture(mockConfig));
        assertNull(UserProfileContext.lookupProfilePicture(mockConfig));
    }

    /**
     * Regression test: a profile without picture has an empty value. Since Java 25, new File("") is the current folder
     * and exists: the screen of the profiles failed when opening it as an image (AccessDeniedException on Windows).
     */
    @Test
    void shouldGetNoProfilePictureForAnEmptyValueOrAFolder() {
        when(mockConfig.getUserPicture()).thenReturn("").thenReturn(localDataFolder);

        assertNull(UserProfileContext.lookupProfilePicture(mockConfig));
        assertNull(UserProfileContext.lookupProfilePicture(mockConfig));
    }

    /**
     * @return the content of a window of the page, in the card drawn by MenuDialog (below the title and close button)
     */
    private static ScrollPane contentOf(Stage dialog) {
        return (ScrollPane) dialog.getScene().getRoot().lookup(".scroll-pane");
    }

    /**
     * The windows are drawn in the look of the menus, without the frame of the system: their own close button closes
     * them and removes the blur of the page.
     */
    @Test
    void shouldCloseAWindowWithItsOwnButton() throws Exception {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        BorderPane page = new BorderPane();
        page.setEffect(new BoxBlur());
        when(mockStage.getScene()).thenReturn(mockScene);
        when(mockScene.getRoot()).thenReturn(page);

        FlowPane choicePanel = new FlowPane();
        User user = new User("test");
        choicePanel.getChildren().add(user);

        // asyncFx(...).get(): a failed assertion fails the test (not with Platform.runLater)
        WaitForAsyncUtils.asyncFx(() -> {
            Stage result = context.createRemoveDialog(mockStage, choicePanel, user);
            assertEquals(StageStyle.TRANSPARENT, result.getStyle());
            result.show();

            Button close = (Button) result.getScene().getRoot().lookup(".gp-dialog-header .gp-icon-button");
            close.fire();

            assertFalse(result.isShowing());
            assertNull(page.getEffect());
            // Nothing deleted
            assertTrue(choicePanel.getChildren().contains(user));
        }).get();
    }

    /**
     * @return the circle of the picture of a profile card: a Rectangle with rounded corners, first in its avatar
     */
    private static Rectangle pictureOf(BorderPane content) {
        return (Rectangle) ((StackPane) content.getCenter()).getChildren().get(0);
    }

    @Test
    void shouldCreateDefaultUser() {
        when(mockTranslator.translate("DefaultUser")).thenReturn("Default User");
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        FlowPane choicePanel = new FlowPane();
        ImagePattern imagePattern = new ImagePattern(new Image("bear.jpg"));

        User result = context.createUser(mockGazePlay, choicePanel, "Default User",
            imagePattern, false, false, screenDimension);
        BorderPane content = (BorderPane) result.getChildren().get(0);
        Rectangle picture = pictureOf(content);

        assertEquals("Default User", result.getName());
        assertNotNull(picture.getFill());

        try (MockedStatic<BackgroundMusicManager> musicManager = mockStatic(BackgroundMusicManager.class, CALLS_REAL_METHODS)) {
            musicManager.when(BackgroundMusicManager::onConfigurationChanged).thenAnswer(invocation -> null);

            content.fireEvent(TestingUtils.clickOnTarget(content));

            musicManager.verify(BackgroundMusicManager::onConfigurationChanged);
        }
        verify(mockGazePlay).onReturnToMenu();
    }

    @Test
    void shouldCreateNamedUser() {
        when(mockTranslator.translate("DefaultUser")).thenReturn("Default User");

        UserProfileContext context = new UserProfileContext(mockGazePlay);
        FlowPane choicePanel = new FlowPane();
        ImagePattern imagePattern = new ImagePattern(new Image("bear.jpg"));

        User result = context.createUser(mockGazePlay, choicePanel, "Test User",
            imagePattern, true, false, screenDimension);
        BorderPane content = (BorderPane) result.getChildren().get(0);
        Rectangle picture = pictureOf(content);
        VBox buttonBox = (VBox) result.getChildren().get(1);
        Button editButton = (Button) buttonBox.getChildren().get(0);
        Button deleteButton = (Button) buttonBox.getChildren().get(1);

        assertEquals("Test User", result.getName());
        assertEquals(imagePattern, picture.getFill());
        assertTrue(deleteButton.getStyleClass().containsAll(List.of("gp-profile-action", "gp-danger")));
        assertTrue(editButton.getStyleClass().contains("gp-profile-action"));

        try (MockedStatic<BackgroundMusicManager> musicManager = mockStatic(BackgroundMusicManager.class, CALLS_REAL_METHODS);
             MockedStatic<ActiveConfigurationContext> configurationContext = mockStatic(ActiveConfigurationContext.class, CALLS_REAL_METHODS)) {
            musicManager.when(BackgroundMusicManager::onConfigurationChanged).thenAnswer(invocation -> null);

            content.fireEvent(TestingUtils.clickOnTarget(content));

            musicManager.verify(BackgroundMusicManager::onConfigurationChanged);
            configurationContext.verify(() -> ActiveConfigurationContext.switchToUser("Test User"));
        }
        verify(mockGazePlay).onReturnToMenu();
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
        Rectangle picture = pictureOf(content);

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
    void shouldCreateExitButton() {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        HBox topBar = (HBox) context.getRoot().getTop();

        // After the space, the button changing the mode and the one of the theme
        Button exitButton = (Button) topBar.getChildren().get(3);
        assertTrue(exitButton.getStyleClass().contains("gp-quit"));

        exitButton.fire();

        verify(mockGazePlay).quit();
    }

    @Test
    void shouldGoBackToTheChoiceOfTheMode() {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        HBox topBar = (HBox) context.getRoot().getTop();

        ((Button) topBar.getChildren().get(1)).fire();

        verify(mockGazePlay).goToModePage();
    }

    /**
     * Without picture, a profile shows a colored disc (a color class on the circle) with an icon.
     */
    @Test
    void shouldShowAColoredDiscWithoutPicture() {
        UserProfileContext context = new UserProfileContext(mockGazePlay);
        FlowPane choicePanel = new FlowPane();

        User result = context.createUser(mockGazePlay, choicePanel, "Test User", null, true, false, screenDimension);
        BorderPane content = (BorderPane) result.getChildren().get(0);
        StackPane avatar = (StackPane) content.getCenter();

        assertEquals(2, avatar.getChildren().size());
        assertTrue(pictureOf(content).getStyleClass().stream().anyMatch(c -> c.startsWith("gp-avatar-")
            && !c.equals("gp-avatar-shape")));
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
            Stage result = context.createRemoveDialog(mockStage, choicePanel, user);
            ScrollPane scroller = contentOf(result);
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
            ScrollPane scroller = contentOf(result);
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
            ScrollPane scroller = contentOf(result);
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
            ScrollPane scroller = contentOf(result);
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
