package net.gazeplay.games.colors;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class AbstractGazeIndicator extends ProgressIndicator implements IGazeProgressIndicator {

    @Getter @Setter
    private double duration;

    private Timeline animation;

    private EventHandler<ActionEvent> finishHandler;
    final EventHandler<ActionEvent> onFinishHandler;
    
    private final Map<Node, EventHandler> nodedToListenTo;
    
    public static final long TIME_DISPLAYED_AFTER_FINISHED_MS = 1000;
    
    @Getter
    private boolean isStarted;

    public AbstractGazeIndicator() {
        super(0);

        this.setVisible(false);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        this.duration = config.getFixationlength();
        this.animation = new Timeline();
        this.nodedToListenTo = new HashMap<Node, EventHandler>();
        
        onFinishHandler = buildOnFinishEventHandler();
    }

    @Override
    public void setOnFinish(EventHandler<ActionEvent> handler) {
        this.finishHandler = handler;
    }

    @Override
    public void start() {

        this.setVisible(true);
        this.setProgress(0);

        animation.stop();
        animation = new Timeline();

        animation.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(this.progressProperty(), 1)));
        
        animation.setOnFinished(onFinishHandler);
        animation.play();
        isStarted = true;
    }

    @Override
    public void stop() {

        animation.stop();
        this.setVisible(false);
        this.setProgress(0);
        isStarted = false;
    }
    
    private EventHandler<ActionEvent> buildOnFinishEventHandler() {
        
        final AbstractGazeIndicator thisIndicator = this;
        
        return (ActionEvent event) -> {
            
            isStarted = false;
            
            log.info("on finish event");
            if (finishHandler != null) {
                finishHandler.handle(event);
            }
            
            final Timer timer = new Timer("Finished display timer");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    thisIndicator.stop();
                }
            }, TIME_DISPLAYED_AFTER_FINISHED_MS);

        };
    }
    
    protected EventHandler buildEventHandler(final Node node) {
        
        final AbstractGazeIndicator thisIndicator = this;
        
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
                thisIndicator.start();
            }
            else if(event.getEventType() == MouseEvent.MOUSE_EXITED || 
                    event.getEventType() == GazeEvent.GAZE_EXITED) {
                thisIndicator.stop();
            }
        };
    }
    
    @Override
    public boolean addNodeToListen(final Node node) {
        
        final EventHandler eventHandler = buildEventHandler(node);
        
        node.addEventFilter(MouseEvent.ANY, eventHandler);
        node.addEventFilter(GazeEvent.ANY, eventHandler);
        this.nodedToListenTo.put(node, eventHandler);
        
        return true;
    }
    
    @Override
    public boolean removeNodeToListen(final Node node) {
        
        EventHandler eventHandler = this.nodedToListenTo.remove(node);
        node.removeEventFilter(MouseEvent.ANY, eventHandler);
        node.removeEventFilter(GazeEvent.ANY, eventHandler);
        
        return true;
    }
}
