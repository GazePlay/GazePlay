package net.gazeplay.games.surviveAgainstRobots;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.gazeplay.IGameContext;

import java.util.Random;
/**
 * The `Bonus` class represents a bonus object in the game, which can provide certain benefits to the player upon collection.
 */
public class Bonus extends Rectangle {

    protected boolean isDestroyed;
    private boolean isStopped;
    private final SurviveAgainstRobots gameInstance;
    private final double spawningDuration;
    private final IGameContext gameContext;
    protected AnimationTimer timer;
    private final double slowfactor;
    private Random random;
    private int nbSeconds;
    private final Label timerlabel;
    private final Rectangle bonusIcon;

    /**
     Constructs a new Bonus object with the specified coordinates, dimensions, game instance, and game context.
     @param x the x-coordinate of the Bonus object
     @param y the y-coordinate of the Bonus object
     @param width the width of the Bonus object
     @param height the height of the Bonus object
     @param gameInstance the SurviveAgainstRobots game instance to which the Bonus object belongs
     @param gameContext the IGameContext game context of the SurviveAgainstRobots game instance
     */
    public Bonus(double x, double y, double width, double height, SurviveAgainstRobots gameInstance, IGameContext gameContext) {
        super(x, y, width, height);
        this.isDestroyed = false;
        this.isStopped = false;
        this.gameInstance = gameInstance;
        this.spawningDuration = 8;
        this.gameContext = gameContext;
        this.slowfactor = 0.5;
        this.timerlabel = new Label();
        this.random = new Random();
        timerlabel.setFont(new Font("Arial", 30));
        timerlabel.setStyle("-fx-text-fill: black ;");
        this.bonusIcon = new Rectangle(getWidth(),getHeight());
        generateRandomBonus();
    }


    /**
     * Generates a random bonus from the BonusEnum values and starts a timer for its effect.
     */
    private void generateRandomBonus() {
        int randomIndex = random.nextInt(BonusEnum.values().length);
        BonusEnum bonus = BonusEnum.values()[randomIndex];

        startTimerWithEffect(bonus);
    }


    /**
     * Starts a timer with the given bonus effect.
     * @param bonusEnum the bonus effect to apply
     */
    private void startTimerWithEffect(BonusEnum bonusEnum){

        // set bonus sprite and add bonus to game context and obstacles list
        setFill(bonusEnum.getImagePattern());
        gameContext.getChildren().add(this);
        gameInstance.obstacles.add(this);

        // define animation timer to handle bonus effect and duration
        nbSeconds = 0;
        timer = new AnimationTimer() {
            int nbframes = 0;
            boolean switchSprite = false;
            boolean isTimerlabelActivated = false;
            boolean resetTimer = false;
            @Override
            public void handle(long now) {
                // increment timer's seconds and frames counters
                if (nbframes == 60){
                    nbframes = 0;
                    if (isTimerlabelActivated){
                        updateTimerLabel();
                    }
                    nbSeconds++;
                }
                nbframes++;

                // apply bonus effect if it is destroyed
                if (isDestroyed && !isStopped){
                    if (bonusEnum == BonusEnum.SLOW){
                        applySlow();
                    }else if (bonusEnum == BonusEnum.FIRERATE){
                        applyFireRate();
                    }else if (bonusEnum == BonusEnum.SHIELD){
                        applyShield();
                    }
                }
                // stop bonus if it exceeds its duration
                if (nbSeconds == spawningDuration){
                    isStopped = true;
                }

                // update sprite every 20 frames for blinking effect during the last 4 seconds
                if (nbframes %20 == 0){
                    if (nbSeconds >= spawningDuration-4){
                        if (!switchSprite){
                            switchSprite = true;
                            setFill(Color.TRANSPARENT);
                        }else{
                            switchSprite = false;
                            setFill(bonusEnum.getImagePattern());
                        }
                    }
                }

                // remove bonus if it is destroyed and reset timer
                if (isDestroyed){
                    removeObject();
                    if (!resetTimer){
                        resetTimer = true;
                        nbSeconds = 0;
                        showBonusTimer(bonusEnum);
                        isTimerlabelActivated = true;
                    }
                }
                // stop bonus and remove effect if it has exceeded its duration
                if (isStopped){
                    removeBonusEffect(bonusEnum);
                    removeObject();
                    stop();
                }

            }
        };
        timer.start();
    }

    private void showBonusTimer(BonusEnum bonusEnum){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        for (int i = 0; i < gameContext.getChildren().size(); i++){

            if (gameContext.getChildren().get(i) instanceof Rectangle){
                if (gameContext.getChildren().get(i).getId() != null && gameContext.getChildren().get(i).getId().equals(bonusEnum.getName())){
                    gameContext.getChildren().remove(gameContext.getChildren().get(i));
                }
            }
            if (gameContext.getChildren().get(i) instanceof Label){
                if (gameContext.getChildren().get(i).getId() != null && gameContext.getChildren().get(i).getId().equals(bonusEnum.getName())){
                    gameContext.getChildren().remove(gameContext.getChildren().get(i));
                }
            }
        }

        if (!gameContext.getChildren().contains(bonusIcon)){
            bonusIcon.setFill(bonusEnum.getImagePattern());
            if (bonusEnum == BonusEnum.SLOW){
                bonusIcon.setX(dimension2D.getWidth()-400);
            }else if (bonusEnum == BonusEnum.SHIELD){
                bonusIcon.setX(dimension2D.getWidth()-300);
            }else if (bonusEnum == BonusEnum.FIRERATE){
                bonusIcon.setX(dimension2D.getWidth()-200);
            }
            bonusIcon.setId(bonusEnum.getName());
            bonusIcon.setY(20);
            bonusIcon.setOpacity(0.8);
            gameContext.getChildren().add(bonusIcon);
        }
        if (!gameContext.getChildren().contains(timerlabel)){
            timerlabel.toFront();
            timerlabel.setLayoutX(bonusIcon.getX() + bonusIcon.getWidth()/2);
            timerlabel.setId(bonusEnum.getName());
            timerlabel.setLayoutY(bonusIcon.getY() + bonusIcon.getHeight());
            gameContext.getChildren().add(timerlabel);
        }
    }

    private void updateTimerLabel(){
        int timeleft = (int)spawningDuration-nbSeconds-1;
        if (timeleft == 0){
            gameContext.getChildren().remove(timerlabel);
            gameContext.getChildren().remove(bonusIcon);
        }else{
            timerlabel.setText(String.valueOf(timeleft));
        }

    }


    /**
     * Applies a slow effect to the robots in the game by adjusting their speed based on the slow factor.
     * If the bonus is destroyed but not stopped, the robots with the same speed as the initial speed will have their speed reduced by the slow factor.
     * If the bonus is stopped, the robots with a speed different from the initial speed will have their speed restored to the initial speed by dividing it by the slow factor.
     */
    private void applySlow(){
        double robotSpeed = gameInstance.robotSpeed;
        if(isDestroyed && !isStopped){
            for (Robot robot : gameInstance.robots){
                if (robot.speed == robotSpeed){
                    robot.speed = robot.speed * slowfactor;
                }
            }
        }
    }

    private void applyFireRate(){
        double playerFireRate = gameInstance.playerFireRate;
        if (isDestroyed && !isStopped){
            if (playerFireRate == gameInstance.player.freqShoot){
                gameInstance.player.freqShoot = 0.25;
            }
        }
    }

    private void applyShield() {
        if (isDestroyed && !isStopped) {
            gameInstance.isShieldEnabled = true;

        }
    }

    private void removeBonusEffect(BonusEnum bonusEnum){
        if (bonusEnum == BonusEnum.SLOW){
            double robotSpeed = gameInstance.robotSpeed;
            for (Robot robot : gameInstance.robots){
                if (robot.speed != robotSpeed){
                    robot.speed = robot.speed / slowfactor;
                }
            }
        }else if (bonusEnum == BonusEnum.FIRERATE){
            double playerFireRate = gameInstance.playerFireRate;
            if (playerFireRate != gameInstance.player.freqShoot){
                gameInstance.player.freqShoot = 0.5;
            }
        }else if (bonusEnum == BonusEnum.SHIELD){
            gameInstance.isShieldEnabled = false;
        }
    }

    private void removeObject(){
        gameContext.getChildren().remove(this);
        gameInstance.obstacles.remove(this);
        gameInstance.bonuses.remove(this);
    }
}
