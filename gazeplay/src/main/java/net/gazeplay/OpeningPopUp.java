package net.gazeplay;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import net.gazeplay.commons.configuration.Configuration;

/**
 * Created by schwab on 24/08/2019.
 *
 * This class manages the opening popup. It connect to a Webpage in order to give to users last news about GazePlay or a
 * default HTML page if no connexion
 */

public class OpeningPopUp {

    final Configuration config = Configuration.getInstance();

    public OpeningPopUp() {

        /*
         * Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle("GazePlay");
         * //alert.setHeaderText("Results:"); //alert.setContentText("Connect to the database successfully!");
         *
         * alert.showAndWait();
         *
         */

        // Create a WebView
        WebView browser = new WebView();

        Rectangle2D screen = Screen.getPrimary().getBounds();

        VBox root = new VBox();

        // root.setPadding(new Insets(5));
        // root.setSpacing(5);
        root.getChildren().addAll(browser);

        float ratio = 3f / 4f;

        double width = screen.getWidth() * ratio;
        double height = screen.getHeight() * ratio;

        browser.setPrefHeight(screen.getWidth());
        browser.setPrefWidth(screen.getHeight());

        Scene scene = new Scene(root, width, height);

        Stage stage = new Stage();
        stage.setScene(scene);

        // Get WebEngine via WebView
        WebEngine webEngine = browser.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.FAILED) {
                    stage.setTitle("");

                    String html = buildDefaultPage();

                    webEngine.loadContent(html);

                    webEngine.loadContent(html, "text/html");
                }
            }
        });
        // Load page

        if (config.getLanguage().equals("fra"))
            webEngine.load("https://gazeplayreleases.wordpress.com/gazeplay-1-6-1-fra");
        else
            webEngine.load("https://gazeplayreleases.wordpress.com/gazeplay-1-6-1-eng/");

        stage.showAndWait();

    }

    private String buildDefaultPage() {

        if (config.getLanguage().equals("fra"))
            return buildFrenchPage();
        else
            return buildEnglishPage();
    }

    private String buildFrenchPage() {

        StringBuilder WepPage = new StringBuilder(2000);

        WepPage.append("<html>");

        WepPage.append("<BODY bgcolor = \"black\">");

        WepPage.append("<FONT color=\"white\">");

        WepPage.append("<H1>GazePlay 1.6 - http://GazePlay.net - gazeplay.net@gmail.com</H1>");

        WepPage.append("<H2>Vous voyez cette fenêtre car Internet semble désactivé</H2>");

        WepPage.append("<H2>Fermez cette fenêtre pour accéder à GazePlay</H2>");

        WepPage.append("<H2>Connectez-vous à Internet pour recevoir les dernières informations sur GazePlay</H2>");

        WepPage.append(asciiImage());

        WepPage.append("GazePlay est un logiciel libre mis à disposition gratuitement.</BR></BR>");
        WepPage.append(
                "Si vous aimez GazePlay, n'hésitez pas à écrire à l'équipe de développement pour lui dire, les encouragements font toujours plaisir.</BR></BR>");
        WepPage.append("Le sourire de vos enfants est notre meilleure récompense.</BR></BR>");
        WepPage.append(
                "Il est possible de soutenir le développement de GazePlay par des dons. Ces dons serviront à financer le recrutement de stagiaires pour aider au développement du logiciel (env. 550€/mois) ou les frais annexes (location d’url, env. 13 euros/an/url, l’achat de matériel informatique comme les eye-trackers, site Web, …). </BR>");
        WepPage.append("https://paypal.me/pools/c/80nEd8cVq5 ou contactez-nous par mail (gazeplay.net@gmail.com)");

        WepPage.append("<hr color=\"white\">");
        WepPage.append("GAZEPLAY Copyright (C) 2016-2019 Univ. Grenoble Alpes, CNRS, LIG UMR 5217");

        WepPage.append("</FONT");

        WepPage.append("</BODY>");

        WepPage.append("</html>");

        return WepPage.toString();

    }

    private String buildEnglishPage() {

        StringBuilder WepPage = new StringBuilder(2000);

        WepPage.append("<html>");

        WepPage.append("<BODY bgcolor = \"black\">");

        WepPage.append("<FONT color=\"white\">");

        WepPage.append("<H1>GazePlay 1.6 - http://GazePlay.net - gazeplay.net@gmail.com</H1>");

        WepPage.append("<H2>Internet seems switch off</H2>");

        WepPage.append(asciiImage());

        WepPage.append("Please, connect to the Internet to get last information about GazePlay.</BR></BR>");

        WepPage.append("GazePlay est un logiciel libre mis à disposition gratuitement.</BR></BR>");
        WepPage.append(
                "Si vous aimez GazePlay, n'hésitez pas à écrire à l'équipe de développement pour lui dire, les encouragements font toujours plaisir.</BR></BR>");
        WepPage.append("Le sourire de vos enfants est notre meilleure récompense.</BR></BR>");
        WepPage.append(
                "Il est possible de soutenir le développement de GazePlay par des dons. Ces dons serviront à financer le recrutement de stagiaires pour aider au développement du logiciel (env. 550€/mois) ou les frais annexes (location d’url, env. 13 euros/an/url, l’achat de matériel informatique comme les eye-trackers, site Web, …). </BR> https://paypal.me/pools/c/80nEd8cVq5");

        WepPage.append("<hr color=\"white\">");
        WepPage.append("GAZEPLAY Copyright (C) 2016-2019 Univ. Grenoble Alpes, CNRS, LIG UMR 5217");

        WepPage.append("</FONT");

        WepPage.append("</BODY>");

        WepPage.append("</html>");

        return WepPage.toString();
    }

    private String asciiImage() {

        return "<code><span style=\"display:block;line-height:8px; font-size: 8px; font-weight:bold;white-space:pre;font-family: monospace;color: white; background: black;\">"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@%,,,,,,,,@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@*,,*,*****,,,,@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@/*****,,********,,%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@#/********,********,,@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@//**********,/*******,,@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@#//********************,/@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@&@@@@@@@@@@@</BR>"
                + "@@@///*********************,@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@,,,*****,,#@@@@@</BR>"
                + "@@@@////****************(***,&@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@,,,*******,****@@@@</BR>"
                + "@@@@@/(///***********//*******,@@@@@@@@@@@@@@@@@@@@@@@@@@@,,********/******/@@@</BR>"
                + "@@@@@@@((///***********//******,@@@@@@@@@@@@@@@@@@@@@@@@,,**************////@@@</BR>"
                + "@@@@@@@@#(((///*******//*///*****@@@@@@@@@@@@@@@@@@@@@,,***/**********/////@@@@</BR>"
                + "@@@@@@@@@@@((((///*********/(/***#@..&&&&&%......@@@&,*******/******////((@@@@@</BR>"
                + "@@@@@@@@@@@@@(((((/////***/////*...*&,   .,     ...*****/**//****////(((@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@(((((/////////*....&   ,@@@        &%&&**/****////((((@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@(((((((////.....,  &@@@@@@        .*#*/////(((((@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@&(((,,...      &&&@@@@@@   @@@@@,. ((((((@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@,,...       &&*  @@@@  &&@@@@@  ,@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@,.. .       %&   %@@&  &  .@@@   @@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@,.          %&  &&@   &  .&@    @@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@%..             *       &&&&     @@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@/,..          ,      .         %@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@/,..          ,%,,&, @,....   %@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@,..                         @@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@,.........              .*@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*,*@@*,....         %@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%,,,,,*....,@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</BR>"
                + "</span></code>";
    }
}

class Browser extends StackPane {
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public Browser(String url) {
        webEngine.load(url);
        getChildren().add(browser);
    }
}