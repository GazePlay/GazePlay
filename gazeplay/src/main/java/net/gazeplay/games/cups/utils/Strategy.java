package net.gazeplay.games.cups.utils;

import java.util.ArrayList;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.games.cups.Cup;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

@Slf4j
public class Strategy {
    private int nbCups;
    private int nbIterations;
    @Getter
    private ArrayList<Action> actions;

    private enum Strategies {
        rotation_up_down,
        rotation_move_closest_in_place
    };

    public Strategy(int nbCups, int nbIterations) {
        this.nbCups = nbCups;
        this.nbIterations = nbIterations;
        this.actions = new ArrayList<Action>();
    }

    public ArrayList<Action> chooseStrategy(Cup[] cups) {
        Random random = new Random();
        boolean moveUp;
        Cup cupToMove;
        Cup cupToSwitch;
        for (int iteration = 0; iteration < nbIterations; iteration++) {
            ArrayList<Integer> numbersToChoose = new ArrayList<>();
            for (int index = 0; index < nbCups; index++) {
                numbersToChoose.add(index);
            }
            ArrayList randomNumbers = new ArrayList<>();
            for (int index = 0; index < 2; index++) {
                int cupChoice = random.nextInt(numbersToChoose.size());
                randomNumbers.add(numbersToChoose.get(cupChoice));
                numbersToChoose.remove(cupChoice);
            }

            cupToMove = cups[(Integer) randomNumbers.get(0)];
            cupToSwitch = cups[(Integer) randomNumbers.get(1)];
            moveUp = random.nextBoolean();

            int strategy_choice = random.nextInt(Strategies.values().length);

            switch (Strategies.values()[strategy_choice]) {
                case rotation_up_down:
                    log.info("Strategy chosen : rotation_up_down");
                    rotation_up_down(cupToMove.getPositionCup().getCellX(), cupToSwitch.getPositionCup().getCellX(),
                            moveUp);
                    break;
                /*case rotation_move_closest_in_place:
                    log.info("Strategy chosen : rotation_move_closest_in_place");
                    rotation_move_closest_in_place(cupToMove.getPositionCup().getCellX(), cupToSwitch.getPositionCup().getCellX(), moveUp);
*/
            }
        }
        return actions;
    }

    private void rotation_up_down(int startCellX, int targetCellX, boolean moveUp) {
        if (moveUp) {
            actions.add(new Action(startCellX, 1, startCellX, 0));
            actions.add(new Action(startCellX, 0, targetCellX, 0));
            actions.add(new Action(targetCellX, 1, targetCellX, 2));
            actions.add(new Action(targetCellX, 2, startCellX, 2));
            actions.add(new Action(startCellX, 2, startCellX, 1));
            actions.add(new Action(targetCellX, 0, targetCellX, 1));
        } else {
            actions.add(new Action(startCellX, 1, startCellX, 2));
            actions.add(new Action(startCellX, 2, targetCellX, 2));
            actions.add(new Action(targetCellX, 1, targetCellX, 0));
            actions.add(new Action(targetCellX, 0, startCellX, 0));
            actions.add(new Action(startCellX, 0, startCellX, 1));
            actions.add(new Action(targetCellX, 2, targetCellX, 1));
        }
    }

    private void rotation_move_closest_in_place(int startCellX, int targetCellX, boolean moveUp){
        if (moveUp) {
            actions.add(new Action(startCellX, 1, startCellX, 0));
            actions.add(new Action(startCellX, 0, targetCellX, 0));
            actions.add(new Action(targetCellX, 1, targetCellX, 2));
            actions.add(new Action(targetCellX, 2, startCellX, 2));
            actions.add(new Action(startCellX, 2, startCellX, 1));
            actions.add(new Action(targetCellX, 0, targetCellX, 1));
        } else {
            actions.add(new Action(startCellX, 1, startCellX, 2));
            actions.add(new Action(startCellX, 2, targetCellX, 2));
            actions.add(new Action(targetCellX, 1, targetCellX, 0));
            actions.add(new Action(targetCellX, 0, startCellX, 0));
            actions.add(new Action(startCellX, 0, startCellX, 1));
            actions.add(new Action(targetCellX, 2, targetCellX, 1));
        }
    }
}
