package net.gazeplay.games.cups.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class Strategy {
    private final int nbExchanges;
    @Getter
    private final ArrayList<Action> actions;
    private final int maxCellsX;
    private final int maxCellsY;

    private enum Strategies {
        rotation_up_down, rotation_move_closest_in_place
    }

    public Strategy(final int nbCups, final int nbExchanges, final int maxCellsX, final int maxCellsY) {
        this.nbExchanges = nbExchanges;
        this.actions = new ArrayList<>();
        this.maxCellsX = maxCellsX;
        this.maxCellsY = maxCellsY;
    }

    public ArrayList<Action> chooseStrategy() {
        for (int iteration = 0; iteration < nbExchanges; iteration++) {

            final Random randomGenerator = new Random();
            final int strategy_choice = randomGenerator.nextInt(Strategies.values().length);

            switch (Strategies.values()[strategy_choice]) {
                case rotation_up_down:
                    log.info("Strategy chosen : rotation_up_down");

                    final ArrayList<Integer> cupsExchangeCellsXrud = randomCupTwoChoices(false);
                    final ArrayList<Integer> cupsExchangeCellsYrud = randomCupTwoChoices(true);

                    rotation_up_down(cupsExchangeCellsXrud.get(0), cupsExchangeCellsXrud.get(1),
                        cupsExchangeCellsYrud.get(0), cupsExchangeCellsYrud.get(1));
                    break;
                case rotation_move_closest_in_place:
                    log.info("Strategy chosen : rotation_move_closest_in_place");

                    final ArrayList<Integer> cupsExchangeCellsXrmcip = randomCupTwoChoices(false);
                    final Integer cupStartCellYrmcip = randomCupOneChoice(true);

                    rotation_move_closest_in_place(cupsExchangeCellsXrmcip.get(0), cupsExchangeCellsXrmcip.get(1),
                        cupStartCellYrmcip);
                    break;
            }
        }
        return actions;
    }

    private void rotation_up_down(final Integer startCellX, final Integer targetCellX, final Integer firstCupMoveToY,
                                  final Integer secondCupMoveToY) {
        actions.add(new Action(startCellX, maxCellsY / 2, startCellX, firstCupMoveToY));
        actions.add(new Action(startCellX, firstCupMoveToY, targetCellX, firstCupMoveToY));
        actions.add(new Action(targetCellX, maxCellsY / 2, targetCellX, secondCupMoveToY));
        actions.add(new Action(targetCellX, secondCupMoveToY, startCellX, secondCupMoveToY));
        actions.add(new Action(startCellX, secondCupMoveToY, startCellX, maxCellsY / 2));
        actions.add(new Action(targetCellX, firstCupMoveToY, targetCellX, maxCellsY / 2));
    }

    private void rotation_move_closest_in_place(final Integer startCellX, final Integer targetCellX, final Integer firstCupMoveToY) {
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

    public Integer randomCupOneChoice(final boolean isYChoice) {
        final ArrayList<Integer> numbersToChooseFrom = new ArrayList();
        for (int index = 0; index < maxCellsX; index++) {
            if (isYChoice && index == maxCellsY / 2) {
                continue;
            }
            numbersToChooseFrom.add(index);
        }
        final Random randomGenerator = new Random();
        final int cupChoice;
        cupChoice = randomGenerator.nextInt(numbersToChooseFrom.size());
        return numbersToChooseFrom.get(cupChoice);
    }

    public ArrayList randomCupTwoChoices(final boolean isYChoice) {
        final ArrayList<Integer> numbersToChooseFrom = new ArrayList();
        for (int index = 0; index < maxCellsX; index++) {
            if (isYChoice && index == maxCellsY / 2) {
                continue;
            }
            numbersToChooseFrom.add(index);
        }
        final ArrayList<Integer> choices = new ArrayList();
        final Random randomGenerator = new Random();
        for (int index = 0; index < 2; index++) {
            final int cupChoice = randomGenerator.nextInt(numbersToChooseFrom.size());
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
