package net.gazeplay.latestnews;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CustomButton;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by schwab on 24/08/2019.
 * <p>
 * This class manages the opening popup. It connect to a Webpage in order to give to users last news about GazePlay or a
 * default HTML page if no connexion
 */
@Slf4j
public class LatestNewPopup {

    private static final String serviceBaseUrl = "https://gazeplay.github.io/GazePlay/updates";

    private final Configuration config;

    private final WebEngine webEngine;

    private final TextField locationUrlLabel = new TextField();

    private final Stage stage;

    @Nullable
    private final Optional<String> versionNumber = VersionInfo.findVersionInfo(VersionInfo.artifactId, false);

    private final String envInfo = findEnvInfo();

    private static String findEnvInfo() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String javaVmVendor = System.getProperty("java.vm.vendor");
        String javaVmVersion = System.getProperty("java.vm.version");
        return osName + " " + osVersion + " - " + javaVmVendor + " " + javaVmVersion;
    }

    private static Dimension2D computePreferedDimension() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        float ratio = 3f / 4f;

        double width = screen.getWidth() * ratio;
        double height = screen.getHeight() * ratio;
        return new Dimension2D(width, height);
    }

    public static void displayIfNeeded(Configuration config, Translator translator) {
        if (wasDisplayRecently(config)) {
            // popup was already show recently
            // we do not want to bother the user again with this popup
            return;
        }

        LatestNewPopup latestNewPopup = new LatestNewPopup(config, translator);
        latestNewPopup.loadPage();
        latestNewPopup.showAndWait();
    }

    private static boolean wasDisplayRecently(Configuration config) {
        return config.getLatestNewsPopupShownTime().get() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
    }

    private void markDisplayedNow(Configuration config) {
        config.getLatestNewsPopupShownTime().set(System.currentTimeMillis());
    }

    private LatestNewPopup(Configuration config, Translator translator) {
        this.config = config;

        final Dimension2D preferredDimension = computePreferedDimension();

        final String userAgentString = "GazePlay " + versionNumber.orElse("unknown version") + " - " + envInfo;

        locationUrlLabel.setEditable(false);
        locationUrlLabel.setDisable(true);

        // Create a WebView
        WebView browser = new WebView();
        browser.setPrefHeight(preferredDimension.getWidth());
        browser.setPrefWidth(preferredDimension.getHeight());

        BorderPane root = new BorderPane();

        VBox topPane = new VBox();
        topPane.setAlignment(Pos.CENTER_RIGHT);

        HBox bottomPane = new HBox();
        bottomPane.setAlignment(Pos.CENTER_RIGHT);

        Scene scene = new Scene(root, preferredDimension.getWidth(), preferredDimension.getHeight());

        Label userAgentLabel = new Label();
        userAgentLabel.prefWidthProperty().bind(scene.widthProperty());
        userAgentLabel.setText(userAgentString);
        userAgentLabel.setAlignment(Pos.CENTER_RIGHT);

        I18NLabel closeInstructionLabel = new I18NLabel(translator, "closeWindowToContinueToGazePlay");
        closeInstructionLabel.setStyle("-fx-font-weight: bold");

        CustomButton continueButton = new CustomButton("data/common/images/continue.png");


        topPane.getChildren().addAll(userAgentLabel, locationUrlLabel);
        bottomPane.getChildren().addAll(closeInstructionLabel, continueButton);

        root.setTop(topPane);
        root.setCenter(browser);
        root.setBottom(bottomPane);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("GazePlay News");

        // Get WebEngine via WebView
        webEngine = browser.getEngine();
        webEngine.setUserAgent(userAgentString);
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            log.info("ov = {}, oldState = {}, newState = {}", ov, oldState, newState);
            if (newState == Worker.State.FAILED) {
                String html = getOfflinePageContent();
                webEngine.loadContent(html, "text/html");
            } else {
                markDisplayedNow(config);
            }
            locationUrlLabel.setText(webEngine.getLocation());
        });

        stage.titleProperty().bind(new SimpleStringProperty() {

            {
                webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
                    // trigger invalidation and notify listeners
                    setValue(getValue());
                });
            }

            @Override
            public String get() {
                String originalPageTitle = webEngine.titleProperty().get();
                StringBuilder titleBuilder = new StringBuilder();
                titleBuilder.append("GazePlay News");
                if (originalPageTitle != null) {
                    titleBuilder.append(" - ").append(originalPageTitle);
                }
                return titleBuilder.toString();
            }
        });

        continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> stage.close());
    }


    private String createDocumentUri() {
        if (versionNumber.isEmpty()) {
            return "";
        }
        //
        String languageCode = config.getLanguage();
        if (!languageCode.equals("fra")) {
            languageCode = "eng";
        }
        StringBuilder documentUri = new StringBuilder();
        documentUri.append("gazeplay");
        versionNumber.ifPresent(s -> documentUri.append("-").append(s.replace('.', '-')));
        documentUri.append("-").append(languageCode);
        return documentUri.toString();
    }

    private void loadPage() {
        String documentUri = createDocumentUri();
        String serviceUrl = serviceBaseUrl + "/" + documentUri;
        webEngine.load(serviceUrl);
    }

    public void show() {
        stage.show();
    }

    private void showAndWait() {
        stage.showAndWait();
    }

    private String getOfflinePageContent() {
        return loadOfflinePageTemplate().replaceAll("\\{ version \\}", versionNumber.orElse("unknown version"));
    }

    private String loadOfflinePageTemplate() {
        try {
            return loadOfflinePageTemplate(config.getLanguage());
        } catch (Exception e) {
            return loadOfflinePageTemplate("eng");
        }
    }

    private String loadOfflinePageTemplate(String languageCode) {
        String resourceName = "updates-popup/offline-page-" + languageCode + ".html";
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(resourceName);
            return IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Failed to load page", e);
            throw new RuntimeException(e);
        }
    }

}

