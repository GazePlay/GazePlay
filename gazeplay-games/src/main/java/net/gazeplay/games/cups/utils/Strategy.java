package net.gazeplay.games.cups.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.ArrayList;

@Slf4j
public class Strategy {
    private final int nbExchanges;
    @Getter
    private final ArrayList<Action> actions;
    private final int maxCellsX;
    private final int maxCellsY;
    private final ReplayablePseudoRandom randomGenerator;

    private enum Strategies {
        rotationUpDown, rotationMoveClosestInPlace
    }

    public Strategy(final int nbCups, final int nbExchanges, final int maxCellsX, final int maxCellsY, ReplayablePseudoRandom random) {
        this.nbExchanges = nbExchanges;
        this.actions = new ArrayList<>();
        this.maxCellsX = maxCellsX;
        this.maxCellsY = maxCellsY;
        this.randomGenerator = random;
    }

    public ArrayList<Action> chooseStrategy() {
        for (int iteration = 0; iteration < nbExchanges; iteration++) {

            final int strategyChoice = randomGenerator.nextInt(Strategies.values().length);

            switch (Strategies.values()[strategyChoice]) {
                case rotationUpDown:
                    log.info("Strategy chosen : rotation_up_down");

                    final ArrayList<Integer> cupsExchangeCellsXrud = randomCupTwoChoices(false);
                    final ArrayList<Integer> cupsExchangeCellsYrud = randomCupTwoChoices(true);

                    rotationUpDown(cupsExchangeCellsXrud.get(0), cupsExchangeCellsXrud.get(1),
                        cupsExchangeCellsYrud.get(0), cupsExchangeCellsYrud.get(1));
                    break;
                case rotationMoveClosestInPlace:
                    log.info("Strategy chosen : rotation_move_closest_in_place");

                    final ArrayList<Integer> cupsExchangeCellsXrmcip = randomCupTwoChoices(false);
                    final Integer cupStartCellYrmcip = randomCupOneChoice(true);

                    rotationMoveClosestInPlace(cupsExchangeCellsXrmcip.get(0), cupsExchangeCellsXrmcip.get(1),
                        cupStartCellYrmcip);
                    break;
            }
        }
        return actions;
    }

    private void rotationUpDown(final Integer startCellX, final Integer targetCellX, final Integer firstCupMoveToY,
                                final Integer secondCupMoveToY) {
        actions.add(new Action(startCellX, maxCellsY / 2, startCellX, firstCupMoveToY));
        actions.add(new Action(startCellX, firstCupMoveToY, targetCellX, firstCupMoveToY));
        actions.add(new Action(targetCellX, maxCellsY / 2, targetCellX, secondCupMoveToY));
        actions.add(new Action(targetCellX, secondCupMoveToY, startCellX, secondCupMoveToY));
        actions.add(new Action(startCellX, secondCupMoveToY, startCellX, maxCellsY / 2));
        actions.add(new Action(targetCellX, firstCupMoveToY, targetCellX, maxCellsY / 2));
    }

    private void rotationMoveClosestInPlace(final Integer startCellX, final Integer targetCellX, final Integer firstCupMoveToY) {
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
