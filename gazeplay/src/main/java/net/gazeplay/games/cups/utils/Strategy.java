package net.gazeplay.games.cups.utils;

import java.util.ArrayList;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Strategy {
    private final int nbCups;
    private final int nbExchanges;
    @Getter
    private final ArrayList<Action> actions;
    private final int maxCellsX;
    private final int maxCellsY;

    private enum Strategies {
        rotation_up_down, rotation_move_closest_in_place
    };

    public Strategy(int nbCups, int nbExchanges, int maxCellsX, int maxCellsY) {
        this.nbCups = nbCups;
        this.nbExchanges = nbExchanges;
        this.actions = new ArrayList<>();
        this.maxCellsX = maxCellsX;
        this.maxCellsY = maxCellsY;
    }

    public ArrayList<Action> chooseStrategy() {
        for (int iteration = 0; iteration < nbExchanges; iteration++) {

            Random randomGenerator = new Random();
            int strategy_choice = randomGenerator.nextInt(Strategies.values().length);

            switch (Strategies.values()[strategy_choice]) {
            case rotation_up_down:
                log.info("Strategy chosen : rotation_up_down");

                ArrayList<Integer> cupsExchangeCellsXrud = randomCupTwoChoices(false);
                ArrayList<Integer> cupsExchangeCellsYrud = randomCupTwoChoices(true);

                rotation_up_down(cupsExchangeCellsXrud.get(0), cupsExchangeCellsXrud.get(1),
                        cupsExchangeCellsYrud.get(0), cupsExchangeCellsYrud.get(1));
                break;
            case rotation_move_closest_in_place:
                log.info("Strategy chosen : rotation_move_closest_in_place");

                ArrayList<Integer> cupsExchangeCellsXrmcip = randomCupTwoChoices(false);
                Integer cupStartCellYrmcip = randomCupOneChoice(true);

                rotation_move_closest_in_place(cupsExchangeCellsXrmcip.get(0), cupsExchangeCellsXrmcip.get(1),
                        cupStartCellYrmcip);
                break;
            }
        }
        return actions;
    }

    private void rotation_up_down(Integer startCellX, Integer targetCellX, Integer firstCupMoveToY,
            Integer secondCupMoveToY) {
        actions.add(new Action(startCellX, maxCellsY / 2, startCellX, firstCupMoveToY));
        actions.add(new Action(startCellX, firstCupMoveToY, targetCellX, firstCupMoveToY));
        actions.add(new Action(targetCellX, maxCellsY / 2, targetCellX, secondCupMoveToY));
        actions.add(new Action(targetCellX, secondCupMoveToY, startCellX, secondCupMoveToY));
        actions.add(new Action(startCellX, secondCupMoveToY, startCellX, maxCellsY / 2));
        actions.add(new Action(targetCellX, firstCupMoveToY, targetCellX, maxCellsY / 2));
    }

    private void rotation_move_closest_in_place(Integer startCellX, Integer targetCellX, Integer firstCupMoveToY) {
        actions.add(new Action(startCellX, maxCellsY / 2, startCellX, firstCupMoveToY));
        actions.add(new Action(startCellX, firstCupMoveToY, targetCellX, firstCupMoveToY));
        if (Math.abs(startCellX - targetCellX) == 1) {
            actions.add(new Action(targetCellX, maxCellsY / 2, startCellX, maxCellsY / 2));
        } else {
            if (startCellX < targetCellX) {
                for (int index = startCellX + 1; index <= targetCellX; index++) {
                    actions.add(new Action(index, maxCellsY / 2, index - 1, maxCellsY / 2));
                }
            } else if (startCellX > targetCellX) {
                for (int index = startCellX - 1; index >= targetCellX; index--) {
                    actions.add(new Action(index, maxCellsY / 2, index + 1, maxCellsY / 2));
                }
            }

        }
        actions.add(new Action(targetCellX, firstCupMoveToY, targetCellX, maxCellsY / 2));
    }

    public Integer randomCupOneChoice(boolean isYChoice) {
        ArrayList<Integer> numbersToChooseFrom = new ArrayList();
        for (int index = 0; index < maxCellsX; index++) {
            if (isYChoice && index == maxCellsY / 2) {
                continue;
            }
            numbersToChooseFrom.add(index);
        }
        Random randomGenerator = new Random();
        int cupChoice;
        if (!isYChoice) {
            cupChoice = randomGenerator.nextInt(numbersToChooseFrom.size());
        } else {
            cupChoice = randomGenerator.nextInt(numbersToChooseFrom.size());
        }
        return numbersToChooseFrom.get(cupChoice);
    }

    public ArrayList randomCupTwoChoices(boolean isYChoice) {
        ArrayList<Integer> numbersToChooseFrom = new ArrayList();
        for (int index = 0; index < maxCellsX; index++) {
            if (isYChoice && index == maxCellsY / 2) {
                continue;
            }
            numbersToChooseFrom.add(index);
        }
        ArrayList<Integer> choices = new ArrayList();
        Random randomGenerator = new Random();
        for (int index = 0; index < 2; index++) {
            int cupChoice = randomGenerator.nextInt(numbersToChooseFrom.size());
            choices.add(numbersToChooseFrom.get(cupChoice));
            numbersToChooseFrom.remove(cupChoice);
            if (isYChoice) {
                if (cupChoice < maxCellsY / 2) {
                    for (Integer i = 0; i < cupChoice; i++) {
                        numbersToChooseFrom.remove(i);
                    }
                } else {
                    for (Integer i = cupChoice; i < maxCellsY; i++) {
                        numbersToChooseFrom.remove(i);
                    }
                }
            }
        }
        return choices;
    }
}
