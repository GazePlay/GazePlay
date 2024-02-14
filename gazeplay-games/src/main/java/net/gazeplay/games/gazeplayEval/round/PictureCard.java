package net.gazeplay.games.gazeplayEval.round;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.games.gazeplayEval.EvalState;
import net.gazeplay.games.gazeplayEval.config.*;

import java.util.function.Function;

@Slf4j
@ToString
public class PictureCard extends Group {
    private final ItemConfig config;

    private final ImageView imageView;
    private final Rectangle notification;

    @Getter
    private boolean selected;

    private final Function<PictureCard, Void> onSelection;

    private final MouseEventHandler mouserHandler;
    private final SelectionProgress progress;

    public PictureCard(ItemConfig config, int i, int j, Function<PictureCard, Void> onSelection) {
        log.info("imagePath = " + config.getGrid(i, j));

        selected = false;
        this.config = config;
        this.onSelection = onSelection;

        double initialX = GameSizing.width * i;
        double initialY = GameSizing.height * j + 10;  // Not sure why the +-10, but that was here before me
        double initialWidth = GameSizing.width;
        double initialHeight = GameSizing.height - 10;

        // Setting up the image to display
        Image image = new Image(config.getGrid(i, j).getPath());
        imageView = new ImageView(image);
        imageView.setFitWidth(initialWidth);
        imageView.setFitHeight(initialHeight);
        imageView.setX(initialX);
        imageView.setY(initialY);
        double reduceCoeff = Math.min(initialWidth / image.getWidth(), initialHeight / image.getHeight());
        imageView.setTranslateX((initialWidth - image.getWidth() * reduceCoeff) / 2);
        imageView.setTranslateY((initialHeight - image.getHeight() * reduceCoeff) / 2);
        imageView.setPreserveRatio(true);
        this.getChildren().add(imageView);

        // Setting up the progress indicator
        final Function<Void, Void> onProgressFinishDummy = (aVoid) -> {
            this.onProgressFinish();
            return aVoid;
        };
        progress = new SelectionProgress(config.getGazeTime(), initialX, initialY, initialWidth, initialHeight, onProgressFinishDummy);
        this.getChildren().add(progress);

        // Setting up the notification image
        Image notifImage;
        double trueWidth = initialWidth;
        double trueHeight = initialHeight;
        double trueTranslateX = 0;
        double trueTranslateY = 0;
        if (ActiveConfigurationContext.getInstance().getFeedback().equals("standard")) {
            notifImage = new Image("data/common/images/blackCircle.png");
        } else {
            notifImage = new Image("data/common/images/redFrame.png");
            reduceCoeff = Math.min(initialWidth / notifImage.getWidth(), initialHeight / notifImage.getHeight());
            trueWidth = notifImage.getWidth() * reduceCoeff;
            trueHeight = notifImage.getHeight() * reduceCoeff;
            trueTranslateX = (initialWidth - trueWidth) / 2;
            trueTranslateY = (initialHeight - trueHeight) / 2;
        }
        notification = new NotificationImage(notifImage, initialX, initialY, trueWidth, trueHeight, trueTranslateX, trueTranslateY);
        this.getChildren().add(notification);

        // Adding the event filters
        EvalState.gameContext.getGazeDeviceManager().addEventFilter(imageView);
        mouserHandler = new MouseEventHandler();
        this.addEventFilter(MouseEvent.ANY, mouserHandler);
        this.addEventFilter(GazeEvent.ANY, mouserHandler);
    }

    private void onProgressFinish() {
        selected = true;
        mouserHandler.disable();
        notification.setVisible(!ActiveConfigurationContext.getInstance().getFeedback().equals("nothing"));
        this.setVisible(false);

        EvalState.gameContext.getGazeDeviceManager().removeEventFilter(imageView);
        imageView.removeEventFilter(MouseEvent.ANY, mouserHandler);
        imageView.removeEventFilter(GazeEvent.ANY, mouserHandler);

        onSelection.apply(PictureCard.this);
    }

    private class MouseEventHandler implements EventHandler<Event> {
        /**
         * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
         * do not want the game to continue to process input, as the user input is irrelevant while the animation is
         * in progress
         */
        private boolean ignoreAnyInput = false;
        private boolean moved = false;

        @Override
        public void handle(Event e) {
            if (ignoreAnyInput || selected)
                return;

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)
                onEntered();
            else if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_MOVED)
                onEnteredOnceWhileMoved();
            else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED)
                onExited();
        }

        public void disable() {
            ignoreAnyInput = true;
        }

        private void onEntered() {
            moved = true;
            log.info("ENTERED {}", imageView.getImage().getUrl());
            progress.start();
        }

        private void onEnteredOnceWhileMoved() {
            if (!moved) {
                moved = true;
                log.info("ENTERED {}", imageView.getImage().getUrl());
                progress.start();
            }
        }

        private void onExited() {
            moved = false;
            log.info("EXITED {}", imageView.getImage().getUrl());
            progress.stop();
        }
    }
}
