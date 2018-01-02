package net.gazeplay.commons.utils;

import com.sun.glass.ui.Screen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertTrue;

@Slf4j
public class BravoTestVisual {

	private static CountDownLatch finishedAnimation;

	private static long duration;

	@Before
	public void before() {
		finishedAnimation = new CountDownLatch(1);
	}

	@Test
	public void shouldRunBravo() throws InterruptedException {
		Application.launch(BravoTestApp.class, null);
		finishedAnimation.await();

		assertTrue(duration > 13000);
		assertTrue(duration < 14000);
	}

	public static class BravoTestApp extends Application {

		private Scene scene;
		private Group root;

		@Override
		public void start(Stage primaryStage) {

			primaryStage.setTitle(getClass().getSimpleName());

			primaryStage.setFullScreen(true);

			root = new Group();

			Screen screen = Screen.getMainScreen();
			scene = new Scene(root, screen.getWidth(), screen.getHeight(), Color.BLACK);

			primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

			primaryStage.setScene(scene);

			Bravo bravo = new Bravo();

			root.getChildren().add(bravo);

			primaryStage.show();

			long startTime = System.currentTimeMillis();

			bravo.playWinTransition(scene, 2000, actionEvent -> {
				long finishedTime = System.currentTimeMillis();

				duration = finishedTime - startTime;
				log.info("duration = {}", duration);

				primaryStage.close();
				finishedAnimation.countDown();

			});
		}
	}


}
