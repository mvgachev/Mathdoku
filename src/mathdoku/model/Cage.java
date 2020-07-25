package mathdoku.model;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import mathdoku.controller.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cage in the Mathdoku table.
 * Is consisted of a number of cells.
 * Has a target and a list of cells.
 */
public class Cage {

    private String target;
    private List<Integer> ids;
    private List<Cell> allCells = new ArrayList<>();
    private boolean allCellsHaveValues = false;
    private boolean isRed = false;
    private List<List<Integer>> allGroupsOfPossibleValues;

    /**
     * Constructor for the cage
     * Takes the target and ids of the cells in it as arguments
     *
     * @param target
     * @param ids
     */
    public Cage(String target, List<Integer> ids) {
        this.target = target;
        this.ids = ids;
    }

    /**
     * Adds a single cell to the cage list
     *
     * @param cell
     */
    public void addCell(Cell cell) {
        allCells.add(cell);
        cell.enterCage(this);
    }

    /**
     * Draws the borders for the cage
     *
     * @param cellTable
     */
    public void drawBorder(Cell[][] cellTable) {

        for (Cell cell : allCells) {
            int up = 3, down = 3, right = 3, left = 3;
            if (cell.checkForNeighbour(cellTable, "up")) up = 0;
            if (cell.checkForNeighbour(cellTable, "down")) down = 0;
            if (cell.checkForNeighbour(cellTable, "right")) right = 0;
            if (cell.checkForNeighbour(cellTable, "left")) left = 0;
            cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(up, right, down, left))));
        }

    }

    /**
     * Sets all of the cells in the cage to red
     *
     * @param isRed
     */
    public void setRed(boolean isRed) {
        this.isRed = isRed;
        if (isRed) {
            for (Cell cell : allCells) {
                cell.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
                cell.setRed(true);
            }
        } else {
            for (Cell cell : allCells) {
                int i = cell.getCoordinates()[0];
                int j = cell.getCoordinates()[1];
                //Checks if the cell is a part of a row or column that has a mistake
                if (!Controller.getTable().getRedRows().contains(j) && !Controller.getTable().getRedColumns().contains(i))
                    cell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
                cell.setRed(false);
            }
        }
    }

    public boolean getIsRed() {
        return isRed;
    }

    public List<Integer> getIDs() {
        return ids;
    }

    public String getTarget() {
        return target;
    }

    public List<Cell> getAllCells() {
        return allCells;
    }

    public List<List<Integer>> getAllGroupsOfPossibleValues() {
        return allGroupsOfPossibleValues;
    }

    public void setAllGroupsOfPossibleValues(List<List<Integer>> allGroupsOfPossibleValues) {
        this.allGroupsOfPossibleValues = allGroupsOfPossibleValues;
    }

    /**
     * Checks if all of the cells have a value
     */
    public boolean checkAllCellsHaveValues() {
        for (Cell cell : allCells) {
            if (cell.getText().equals("")) return false;
        }
        return true;
    }

    /**
     * Checks if the target of the cage is achieved
     */
    public boolean checkTarget() {
        //Checks for a cage with a single cell
        if (target.length() == 1) {
            if (allCells.get(0).getText().equals(target)) return true;
        } else {
            int sum = Integer.parseInt(target.substring(0, target.length() - 1));
            String sign = target.substring(target.length() - 1);

            int cellSum = 0;
            //Calculates the sum for every sign
            switch (sign) {
                case "+":
                    for (Cell cell : allCells) cellSum += Integer.parseInt(cell.getText());
                    if (cellSum == sum) return true;
                case "x":
                    cellSum = 1;
                    for (Cell cell : allCells) cellSum *= Integer.parseInt(cell.getText());
                    if (cellSum == sum) return true;
                case "-":
                    for (Cell cell : allCells) {
                        cellSum = Integer.parseInt(cell.getText());
                        for (Cell cell2 : allCells) {
                            if (!cell2.equals(cell)) cellSum -= Integer.parseInt(cell2.getText());
                        }
                        if (cellSum == sum) return true;
                    }
                case "÷":
                    for (Cell cell : allCells) {
                        Double doubleSum = Double.parseDouble(cell.getText());
                        for (Cell cell2 : allCells) {
                            if (!cell2.equals(cell)) doubleSum /= Double.parseDouble(cell2.getText());
                        }
                        if (doubleSum.equals((double) sum)) return true;
                    }
            }
        }
        return false;
    }

    /**
     * Checks if the cage has a mistake or not and changes its background color to red if so
     */
    public void check() {
        if (this.checkAllCellsHaveValues() && !this.checkTarget()) {
            this.setRed(true);
        } else if (this.getIsRed()) this.setRed(false);
    }
}
