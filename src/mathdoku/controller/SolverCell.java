package mathdoku.controller;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a clone version of the {@link mathdoku.model.Cell} class that is used to find its value.
 * Similarly, has possible values, coordinates and id.
 */
public class SolverCell {

    private Set<Integer> possibleValues;
    private int value;
    private boolean hasCage;
    private int id;

    /**
     * Constructor that creates a solver cell based on its possible values.
     *
     * @param possibleValues
     */
    public SolverCell(Set<Integer> possibleValues) {
        this.possibleValues = new HashSet<>(possibleValues);
    }

    /**
     * Constructor that builds a solver cell by copying an old one.
     *
     * @param solverCell
     */
    public SolverCell(SolverCell solverCell) {
        this.possibleValues = new HashSet<>(solverCell.possibleValues);
        this.value = solverCell.value;
    }

    public Set<Integer> getPossibleValues() {
        return possibleValues;
    }

    public int getValue() {
        return value;
    }

    public boolean getHasCage() {
        return hasCage;
    }

    public int getID() {
        return id;
    }

    public void setHasCage(boolean hasCage) {
        this.hasCage = hasCage;
    }

    public void setID(int i, int j, int size) {
        id = j * size + i + 1;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
