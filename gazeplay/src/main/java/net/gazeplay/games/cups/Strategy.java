package net.gazeplay.games.cups;

import java.util.ArrayList;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

@Slf4j
public class Strategy {
    private int nbCups;
    private int nbIterations;
    @Getter
    private ArrayList<Action> actions;
    private enum Strategies{movement_rotation};

    public Strategy(int nbCups, int nbIterations){
        this.nbCups = nbCups;
        this.nbIterations = nbIterations;
        this.actions = new ArrayList<Action>();
    }
    
    public ArrayList<Action> chooseStrategy(Cup[] cups){
        Random random = new Random();
        boolean moveUp;
        Cup cupToMove;
        Cup cupToSwitch;
        for (int iteration = 0; iteration < nbIterations; iteration++){
            ArrayList<Integer> numbersToChoose = new ArrayList<>();
            for (int index = 0; index < nbCups; index++){
                numbersToChoose.add(index);
            }
            ArrayList randomNumbers = new ArrayList<>();
            for(int index = 0; index < 2; index++){
                int cupChoice = random.nextInt(numbersToChoose.size());
                randomNumbers.add(numbersToChoose.get(cupChoice));
                numbersToChoose.remove(cupChoice);
            }
            
            cupToMove = cups[(Integer)randomNumbers.get(0)];
            cupToSwitch = cups[(Integer)randomNumbers.get(1)];
            moveUp = random.nextBoolean();
            
            int strategy_choice = random.nextInt(Strategies.values().length);
            
            switch (Strategies.values()[strategy_choice]){
                case movement_rotation :
                    log.info("Strategy chosen : movement_rotation");
                    movement_rotation(cupToMove.getPositionCup().getCellX(), cupToSwitch.getPositionCup().getCellX(), moveUp);
                    break;
            }
        }
        return actions;
    }
    
    private void movement_rotation(int startCellX, int targetCellX, boolean moveUp){
        if (moveUp){
            actions.add(new Action(startCellX,1,startCellX,0));
            actions.add(new Action(startCellX,0,targetCellX,0));
            actions.add(new Action(targetCellX,1,targetCellX,2));
            actions.add(new Action(targetCellX,2,startCellX,2));
            actions.add(new Action(startCellX,2,startCellX,1));
            actions.add(new Action(targetCellX,0,targetCellX,1));
        }else{
            actions.add(new Action(startCellX,1,startCellX,2));
            actions.add(new Action(startCellX,2,targetCellX,2));
            actions.add(new Action(targetCellX,1,targetCellX,0));
            actions.add(new Action(targetCellX,0,startCellX,0));
            actions.add(new Action(startCellX,0,startCellX,1));
            actions.add(new Action(targetCellX,2,targetCellX,1));
        }
    }
    
    
}
