package net.gazeplay.games.colors;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class GazeFollowerIndicator extends AbstractGazeIndicator {

    public static final double GAZE_PROGRESS_INDICATOR_WIDTH = 15;
    public static final double GAZE_PROGRESS_INDICATOR_HEIGHT = GAZE_PROGRESS_INDICATOR_WIDTH;
    public static final double GAZE_PROGRESS_INDICATOR_OFFSET = 2;

    public GazeFollowerIndicator(final Node root) {

        super();

        this.setMinWidth(GAZE_PROGRESS_INDICATOR_WIDTH);
        this.setMinHeight(GAZE_PROGRESS_INDICATOR_HEIGHT);

        root.addEventFilter(MouseEvent.ANY, (event) -> {

            this.toFront();
            moveGazeIndicator(event.getX() + GAZE_PROGRESS_INDICATOR_OFFSET,
                    event.getY() + GAZE_PROGRESS_INDICATOR_OFFSET);
        });
        root.addEventFilter(GazeEvent.ANY, (event) -> {

            this.toFront();
            moveGazeIndicator(event.getX() + GAZE_PROGRESS_INDICATOR_OFFSET,
                    event.getY() + GAZE_PROGRESS_INDICATOR_OFFSET);
        });
    }

    private void moveGazeIndicator(double x, double y) {
        this.setTranslateX(x);
        this.setTranslateY(y);
        /*
         * log.info("progress size : width = {}, height = {}", progressIndicator.getWidth(),
         * progressIndicator.getHeight()); log.info("translated to : x = {}, y = {}", x, y);
         */
    }
    
    @Override
    protected EventHandler buildEventHandler(final Node node) {
        
        final GazeFollowerIndicator thisIndicator = this;
        
        return (EventHandler) (Event event) -> {
            
            double eventX = 0;
            double eventY = 0;
            
            if(event.getEventType() == MouseEvent.ANY) {
                
                MouseEvent mouseEvent = (MouseEvent) event;   
                eventX = mouseEvent.getX();
                eventY = mouseEvent.getY();
            }
            else if(event.getEventType() == GazeEvent.ANY) {
                
                GazeEvent gazeEvent = (GazeEvent) event;
                eventX = gazeEvent.getX();
                eventY = gazeEvent.getY();
            }
            else {
                throw new UnsupportedOperationException("Unsupported event type");
            }
            
            if(event.getEventType() == MouseEvent.MOUSE_ENTERED || 
                    event.getEventType() == GazeEvent.GAZE_ENTERED) {
                
                thisIndicator.moveGazeIndicator(eventX, eventY);
                thisIndicator.start();
            }
            else if(event.getEventType() == MouseEvent.MOUSE_EXITED || 
                    event.getEventType() == GazeEvent.GAZE_EXITED) {
                thisIndicator.stop();
            }
            else if(event.getEventType() == MouseEvent.MOUSE_MOVED || 
                    event.getEventType() == GazeEvent.GAZE_MOVED) {
                
                thisIndicator.moveGazeIndicator(eventX, eventY);
            }
        };
    }
}
