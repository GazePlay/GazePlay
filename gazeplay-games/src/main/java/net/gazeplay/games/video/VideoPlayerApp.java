package net.gazeplay.games.video;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class VideoPlayerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static final String videoId;

    static {
        videoId = "YE7VzlLtp-4"; // big buck bunny
    }

    @Override
    public void start(Stage stage) throws Exception {

        String videoUrl = "http://www.youtube.com/embed/" + videoId + "?autoplay=1";

        WebView webview = new WebView();
        webview.getEngine().load(videoUrl);
        webview.setPrefSize(640, 360); // 360p

        stage.setScene(new Scene(webview));
        stage.show();
    }

}
