package net.gazeplay;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CustomButton;

@Data
@Slf4j
public class UserProfilContext extends GraphicalContext<BorderPane> {

    private final static String LOGO_PATH = "data/common/images/gazeplay.png";
    private int nbUser = 5;

    @Setter
    @Getter
    private GazeDeviceManager gazeDeviceManager;

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public static UserProfilContext newInstance(final GazePlay gazePlay, final Configuration config) {

        GamesLocator gamesLocator = new DefaultGamesLocator();
        List<GameSpec> games = gamesLocator.listGames();

        BorderPane root = new BorderPane();

        return new UserProfilContext(gazePlay, root, config);
    }

    public UserProfilContext(GazePlay gazePlay, BorderPane root, Configuration config) {
        super(gazePlay, root);
        
        Node logo = createLogo();
        StackPane topLogoPane = new StackPane();
        topLogoPane.getChildren().add(logo);
        
        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        CustomButton exitButton = createExitButton();
        topRightPane.getChildren().addAll(exitButton);
        

        ProgressIndicator indicator = new ProgressIndicator(0);
        Node userPickerChoicePane = createuUserPickerChoicePane(gazePlay, config, indicator);

        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        centerCenterPane.getChildren().add(userPickerChoicePane);
        

        BorderPane topPane = new BorderPane();
        topPane.setCenter(topLogoPane);
        topPane.setRight(topRightPane);

        root.setTop(topPane);
        root.setCenter(centerCenterPane);
        
        root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
        
    }
    
    
    private ScrollPane createuUserPickerChoicePane(GazePlay gazePlay, Configuration config, ProgressIndicator indicator) {
    	
    	final int flowpaneGap = 20;
        FlowPane choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.CENTER);
        choicePanel.setHgap(flowpaneGap);
        choicePanel.setVgap(flowpaneGap);
        choicePanel.setPadding(new Insets(20, 60, 20, 60));

        ScrollPane choicePanelScroller = new ScrollPane(choicePanel);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);
        
        for (int i = 0; i <= nbUser ; i ++) {
            final BorderPane gameCard = createUser(choicePanel,gazePlay, i);
            choicePanel.getChildren().add(gameCard);
        }
    	
    	return choicePanelScroller;
    }
   
    private BorderPane createUser(FlowPane choicePanel, GazePlay gazePlay, int i) {
    	BorderPane c = new BorderPane();
    	Rectangle r = new Rectangle(0,0,500,500);
    	r.setFill(Color.DIMGRAY);
    	c.setCenter(r);
    	String userName = (i == 0) ? "Default User" : "User"+i;
    	if(i == nbUser) {
    		userName = "Add User";
    	}
    	Text t = new Text(userName);
    	t.setFill(Color.WHITE);
    	c.setBottom(t);
    	EventHandler<Event> enterh;
    	
    	enterh = new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				gazePlay.getHomeMenuScreen().setUpOnStage(gazePlay.getPrimaryScene());
			}
    	};
    	
    	if(i == nbUser) {
    		enterh = new EventHandler<Event>() {
    			@Override
    			public void handle(Event event) {
    				int temp = nbUser;
    				nbUser++;
    				choicePanel.getChildren().remove(c);
    				choicePanel.getChildren().add(createUser(choicePanel,gazePlay,temp));
    				choicePanel.getChildren().add(c);
    			}
        	};  		
    	}
    	
    	c.addEventFilter(MouseEvent.MOUSE_PRESSED, enterh);
        return c;
    }
    
    
    private CustomButton createExitButton() {
        CustomButton exitButton = new CustomButton("data/common/images/power-off.png");
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> System.exit(0));
        return exitButton;
    }
    
    private Node createLogo() {
        double width = root.getWidth() * 0.5;
        double height = root.getHeight() * 0.2;

        log.info(LOGO_PATH);
        final Image logoImage = new Image(LOGO_PATH, width, height, true, true);
        final ImageView logoView = new ImageView(logoImage);

        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            final double newHeight = newValue.doubleValue() * 0.2;
            final Image newLogoImage = new Image(LOGO_PATH, width, newHeight, true, true);
            logoView.setImage(newLogoImage);
        });

        return logoView;
    }

}