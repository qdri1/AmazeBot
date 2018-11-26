/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kudri
 */
public class MyNameIsAmaze extends PlayerThread {

    private Cell closestCell = null;
    private List<Cell> wayCell = new ArrayList<>();
    private List<Cell> wallCell = new ArrayList<>();
    private List<Cell> tupCell = new ArrayList<>();
    private Cell saveCurrentCell = null;
    private List<Integer> saveHistoryOfDirection = new ArrayList<>();
    private List<Priority> availableDirections;
    private static final int UP = 0, RIGHT = 1, DOWN = 2, LEFT = 3;
    private boolean calledOnlyOnce = false;
    
    private Map<Integer, List<Cell>> saveHistoryOfAllPlayerPos = new HashMap<>();

    @Override
    public Direction move() {
        return doMagicQdri();
    }

    private Direction doMagicQdri() {
        Direction[] ds = Direction.values();

        calledOnlyOnce();

        initDirections(ds.length); // available all 4 directions (0 - UP, 1 - RIGHT, 2 - DOWN, 3 - LEFT)

        checkGoalExistance(); // always know about the closest garbage goal

        generateWallOrWayCell(); // generate and save way or wall that was in your way

        findDirectionToTheGarbage(); // find direction to the closest gargabe

        checkForWalls(); // check for walls

        checkForTups(); // check for tupiks

        checkForWays(); // check for ways that was saved before

        int direction = getHighPriorityDirection();
        saveHistoryOfDirection.add(direction);
        return ds[direction];
    }

    private void saveAllPlayersLastPosition() {
        for (int i = 0; i < players.length; i++) {
            List<Cell> itemsList = saveHistoryOfAllPlayerPos.get(i);

            // if list does not exist create it
            if (itemsList == null) {
                itemsList = new ArrayList<>();
                itemsList.add(players[i]);
                saveHistoryOfAllPlayerPos.put(i, itemsList);
            } else {
                // add if item is not already in list
                if (!itemsList.contains(players[i])) {
                    itemsList.add(players[i]);
                    saveHistoryOfAllPlayerPos.put(i, itemsList);
                }
            }
        }
    }

    private void calledOnlyOnce() {
        if (!calledOnlyOnce) {
            calledOnlyOnce = true;
            for (int i = 1; i <= 50; i++) {
                wallCell.add(new Cell(0, i));
                wallCell.add(new Cell(i, 0));
            }
        }
    }

    private void findDirectionToTheGarbage() {
        if (closestCell.row > pos.row) {
            increasePriority(DOWN, 1);
        } else if (pos.row > closestCell.row) {
            increasePriority(UP, 1);
        }

        if (closestCell.column > pos.column) {
            increasePriority(RIGHT, 1);
        } else if (pos.column > closestCell.column) {
            increasePriority(LEFT, 1);
        }
    }

    private void checkForWalls() {
        if (!wallCell.isEmpty()) {
            int count = 0;
            for (Cell cell : wallCell) {
                if ((pos.row + 1) == cell.row && pos.column == cell.column) {
                    count++;
                    decreasePriority(DOWN, 100);
                } else if ((pos.row - 1) == cell.row && pos.column == cell.column) {
                    count++;
                    decreasePriority(UP, 100);
                } else if (pos.row == cell.row && (pos.column + 1) == cell.column) {
                    count++;
                    decreasePriority(RIGHT, 100);
                } else if (pos.row == cell.row && (pos.column - 1) == cell.column) {
                    count++;
                    decreasePriority(LEFT, 100);
                }
            }
            if (count >= 3) {
                tupCell.add(pos);
            }
        }
    }

    private void checkForTups() {
        if (!tupCell.isEmpty()) {
            for (Cell cell : tupCell) {
                if ((pos.row + 1) == cell.row && pos.column == cell.column) {
                    decreasePriority(DOWN, 50);
                } else if ((pos.row - 1) == cell.row && pos.column == cell.column) {
                    decreasePriority(UP, 50);
                } else if (pos.row == cell.row && (pos.column + 1) == cell.column) {
                    decreasePriority(RIGHT, 50);
                } else if (pos.row == cell.row && (pos.column - 1) == cell.column) {
                    decreasePriority(LEFT, 50);
                }
            }
        }
    }

    private void checkForWays() {
        if (!wayCell.isEmpty()) {
            for (Cell cell : wayCell) {
                if ((pos.row + 1) == cell.row && pos.column == cell.column) {
                    decreasePriority(DOWN, 2);
                } else if ((pos.row - 1) == cell.row && pos.column == cell.column) {
                    decreasePriority(UP, 2);
                } else if (pos.row == cell.row && (pos.column + 1) == cell.column) {
                    decreasePriority(RIGHT, 2);
                } else if (pos.row == cell.row && (pos.column - 1) == cell.column) {
                    decreasePriority(LEFT, 2);
                }
            }
        }
    }

    private int getHighPriorityDirection() {
        Collections.sort(availableDirections, new Comparator<Priority>() {
            @Override
            public int compare(Priority o1, Priority o2) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            }
        });

        for (int i = 0; i < availableDirections.size(); i++) {
            System.out.println("directions: " + availableDirections.get(i).getDirection() + " / " + availableDirections.get(i).getPriority());
        }

        return availableDirections.get(0).getDirection();
    }

    private void increasePriority(int index, int value) {
        int newPriority = availableDirections.get(index).priority + value;
        availableDirections.set(index, new Priority(index, newPriority));
    }

    private void decreasePriority(int index, int value) {
        int newPriority = availableDirections.get(index).priority - value;
        availableDirections.set(index, new Priority(index, newPriority));
    }

    private void generateWallOrWayCell() {
        boolean isWall = isWall();
        if (isWall) {
            Cell cell = generateWallCell();
            if (cell != null && !isWallAlreadyAdded(cell)) {
                wallCell.add(generateWallCell());
            }
        } else {
//            if (!isWayAlreadyAdded(pos)) {
            wayCell.add(pos);
//            }
        }
    }

    private boolean isWallAlreadyAdded(Cell cell) {
        if (wallCell.isEmpty()) {
            return false;
        } else {
            for (Cell c : wallCell) {
                if (cell.row == c.row && cell.column == c.column) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWayAlreadyAdded(Cell cell) {
        if (wayCell.isEmpty()) {
            return false;
        } else {
            for (Cell c : wayCell) {
                if (cell.row == c.row && cell.column == c.column) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initDirections(int length) {
        availableDirections = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            availableDirections.add(new Priority(i, 0));
        }
    }

    private void checkGoalExistance() {
        if (closestCell == null) {
            closestCell = getClosestCell(pos);
        }
        System.out.println("#####" + closestCell.row + " / " + closestCell.column);
        for (Cell cell : garbages) {
            if (closestCell.row == cell.row && closestCell.column == cell.column) {
                return;
            }
        }
        clearWays();
        closestCell = getClosestCell(pos);
    }

    private void clearWays() {
        System.out.println("#####cleared");
        wayCell.clear();
    }

    private boolean isWall() {
        boolean isWall = false;
        if (saveCurrentCell != null) {
            isWall = saveCurrentCell.row == pos.row && saveCurrentCell.column == pos.column;
        }
        saveCurrentCell = pos;
        return isWall;
    }

    private Cell generateWallCell() {
        if (!saveHistoryOfDirection.isEmpty()) {
            switch (saveHistoryOfDirection.get(saveHistoryOfDirection.size() - 1)) {
                case 0: // UP
                    return new Cell(pos.row - 1, pos.column);
                case 1: // RIGHT
                    return new Cell(pos.row, pos.column + 1);
                case 2: // DOWN
                    return new Cell(pos.row + 1, pos.column);
                case 3: // LEFT
                    return new Cell(pos.row, pos.column - 1);
            }
        }
        return null;
    }

    public Cell getClosestCell(Cell myPos) {
        double[] distanceValues = new double[garbages.length];
        for (int i = 0; i < garbages.length; i++) {
            distanceValues[i] = getDistance(myPos, garbages[i]);
        }
        int index = getIndexOfMinValue(distanceValues);
        return garbages[index];
    }

    private int getIndexOfMinValue(double[] distanceValues) {
        double max = 10000;
        int index = 0;
        for (int i = 0; i < distanceValues.length; i++) {
            if (distanceValues[i] < max) {
                max = distanceValues[i];
                index = i;
            }
        }
        return index;
    }

    public double getDistance(Cell myPos, Cell gbPos) {
        double distance = (Math.pow((myPos.row - gbPos.row), 2)) + (Math.pow((myPos.column - gbPos.column), 2));
        distance = Math.sqrt(distance);
        return distance;
    }

}
