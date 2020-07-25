package mathdoku.controller;

import mathdoku.model.Cage;
import mathdoku.model.Cell;
import mathdoku.model.Table;

import java.util.*;

/**
 * Represents a solver.
 * Solves the mathdoku table by using possible values for every cell and iterating through the cells by recursion.
 */
public class Solver {
    private Table table;
    private List<List<Integer>> allGroupsOfPossibleValues;
    private List<Integer> allValuesInCage;
    private Set<Integer> possibleValuesInCage;
    private Cell[][] cellTable;
    private SolverCell[][] solverCellTable;
    private int maxValue;
    private boolean solved = false;

    /**
     * Constructing a solver with given table
     *
     * @param table the table with all the cells in it
     */
    public Solver(Table table) {
        this.table = table;
        cellTable = table.getCellTable();
        maxValue = cellTable.length;
        solverCellTable = new SolverCell[maxValue][maxValue];
        storeAllPossibleValues();
        getCellValue(1, solverCellTable);
    }

    /**
     * Constructing a solver with given size.
     *
     * @param size the size of the table
     */
    public Solver(int size) {
        solverCellTable = new SolverCell[size][size];
    }

    /**
     * Creates a solver by taking a given solver cell table.
     *
     * @param solverCellTable the solver cell table with all the solver cells in it
     */
    public Solver(SolverCell[][] solverCellTable) {
        this.solverCellTable = solverCellTable;
    }

    public SolverCell[][] getSolverCellTable() {
        return solverCellTable;
    }

    private void getCellValue(int id, SolverCell[][] solverCellTable) {

        int[] coordinates = Table.getCoordinatesOfID(id, table.getSize());
        int i = coordinates[0];
        int j = coordinates[1];
        Cell cell = cellTable[i][j];
        SolverCell[][] solverCellTableCopy = copySolverCellTable(solverCellTable);
        Set<Integer> cellPossibleValues = new HashSet<>(solverCellTableCopy[i][j].getPossibleValues());

        for (int value : cellPossibleValues) {

            solverCellTable = copySolverCellTable(solverCellTableCopy);
            solverCellTable[i][j].setValue(value);
            removePosColumnRow(i, j, value, solverCellTable);
            removePosCage(cell, value, solverCellTable);

            if (id != Math.pow(cellTable.length, 2)) getCellValue(id + 1, solverCellTable);
            else {
                this.solverCellTable = solverCellTable;
                solved = true;
            }
            //Breaks every for loop left after the game has been solved
            if (solved) break;
        }

    }

    /**
     * Removes possible values for column and row when a cell accepts a sample value
     */
    public void removePosColumnRow(int i, int j, int value, SolverCell[][] solverCellTable) {

        //Removes the possibility of every cell in that column to have the value
        for (int r = 0; r < solverCellTable.length; r++) {
            solverCellTable[r][j].getPossibleValues().remove(value);
        }
        //Removes the possibility of every cell in that row to have the value
        for (int c = 0; c < solverCellTable.length; c++) {
            solverCellTable[i][c].getPossibleValues().remove(value);
        }
    }

    /**
     * Removes possible values for the cells in the cage when a cell accepts a sample value
     *
     * @param cell
     * @param value
     * @param solverCellTable
     */
    private void removePosCage(Cell cell, int value, SolverCell[][] solverCellTable) {

        Cage cage = cell.getCage();
        Set<Integer> remainingPossibleValues = new HashSet<>();

        //Puts the remaining possible values in a new set
        for (List<Integer> group : cage.getAllGroupsOfPossibleValues()) {
            //Checks for the groups that have the new value and adds the other values as a possible value for the
            //remaining cells in the cage
            if (group.contains(value))
                remainingPossibleValues.addAll(group);
        }

        //Puts the intersection between the current possible values of the cell (after the column and row check)
        //and the remaining cage possible values
        for (Cell remCell : cage.getAllCells()) {
            if (remCell != cell) {
                int i = remCell.getCoordinates()[0];
                int j = remCell.getCoordinates()[1];
                solverCellTable[i][j].getPossibleValues().retainAll(remainingPossibleValues);
            }
        }
    }

    /**
     * Puts all the possible values from a cage in the solver cell
     */
    private void storeAllPossibleValues() {

        for (Cage cage : table.getAllCages()) {
            possibleValuesInCage = new HashSet<>();
            allGroupsOfPossibleValues = new ArrayList<>();
            allValuesInCage = new ArrayList<>();
            int target;
            String sign;
            String cageTarget = cage.getTarget();
            //takes the attributes of the cage
            int numberOfCells = cage.getAllCells().size();
            //Checks if the cage has a sign or not
            if (cageTarget.length() != 1) {
                target = Integer.parseInt(cage.getTarget().substring(0, cage.getTarget().length() - 1));
                sign = cage.getTarget().substring(cage.getTarget().length() - 1);
                //checks for the possible values in the cage and adds them to the list of groups
                findPossibleValues(numberOfCells, target, sign);
            } else {
                target = Integer.parseInt(cage.getTarget());
                allGroupsOfPossibleValues.add(new ArrayList<>(Collections.singletonList(target)));
                possibleValuesInCage.add(target);
            }


            //Stores the group of possible values in the cage
            cage.setAllGroupsOfPossibleValues(allGroupsOfPossibleValues);
            //sets the possible values in every cell in that cage
            for (Cell cell : cage.getAllCells()) {
                SolverCell solverCell = new SolverCell(possibleValuesInCage);
                int i = cell.getCoordinates()[0];
                int j = cell.getCoordinates()[1];
                solverCellTable[i][j] = solverCell;
            }
        }
    }


    /**
     * Finds all the possible values the solver cell can have in dependence of its cage
     *
     * @param numberOfCells
     * @param target
     * @param sign
     */
    public void findPossibleValues(int numberOfCells, int target, String sign) {

        //Checks when the recursion is on the last solver cell and if the target is fulfilled
        if (numberOfCells == 1) {
            if (target <= maxValue && target > 0) {
                allValuesInCage.add(target);
                copy(allValuesInCage);
                allValuesInCage.remove(allValuesInCage.size() - 1);
            }

        } else {

            for (int i = 1; i <= maxValue; i++) {
                allValuesInCage.add(i);
                int newTarget;
                switch (sign) {
                    case "+":
                        newTarget = target - i;
                        findPossibleValues(numberOfCells - 1, newTarget, sign);
                        break;
                    case "-":
                        newTarget = target + i;
                        findPossibleValues(numberOfCells - 1, newTarget, sign);
                        break;
                    case "x":
                        if (target % i == 0) {
                            newTarget = target / i;
                            findPossibleValues(numberOfCells - 1, newTarget, sign);
                        }
                        break;
                    case "÷":
                        newTarget = target * i;
                        findPossibleValues(numberOfCells - 1, newTarget, sign);
                        break;
                }
                allValuesInCage.remove(allValuesInCage.size() - 1);
            }
        }
    }

    /**
     * Take the remaining possible values and put them in the
     *
     * @param allValuesInCage
     */
    private void copy(List<Integer> allValuesInCage) {
        List<Integer> copy = new ArrayList<>(allValuesInCage);
        //Sorts the list
        Collections.sort(copy);
        //Checks if it is already added as a group of possible values (different arrangement)
        if (!allGroupsOfPossibleValues.contains(copy)) allGroupsOfPossibleValues.add(copy);
        //Adds every possible value in the set
        possibleValuesInCage.addAll(copy);
    }

    /**
     * Copies the all of the elements of one solver cell to another
     *
     * @param copyFrom the map whose elements have to be copied
     */
    private SolverCell[][] copySolverCellTable(SolverCell[][] copyFrom) {
        SolverCell[][] copyTo = new SolverCell[maxValue][maxValue];
        for (int i = 0; i < copyFrom.length; i++)
            for (int j = 0; j < copyFrom.length; j++) {
                copyTo[i][j] = new SolverCell(copyFrom[i][j]);
            }
        return copyTo;
    }
}
