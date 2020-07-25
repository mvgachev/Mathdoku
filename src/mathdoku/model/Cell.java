package mathdoku.model;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import mathdoku.controller.Controller;

import java.util.Stack;

/**
 * Represents a cell of the mathdoku table.
 * Has possible values, value, undo and redo values, coordinates, id.
 */
public class Cell extends StackPane {

    private Label text = new Label("");
    private Label targetLabel = new Label("");
    private int[] coordinates = new int[2];
    private Cage cage;
    private Stack<String> undoValues = new Stack<>();
    private Stack<String> redoValues = new Stack<>();
    private boolean isRed;

    /**
     * Constructs a cell with stack pane and anchor pane.
     */
    public Cell() {
        super();

        this.setMaxHeight(80);
        this.setMaxWidth(80);
        this.setMinSize(30, 30);


        this.setOnMouseClicked(new Controller.CellSelected());

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(targetLabel);
        this.getChildren().addAll(text, anchorPane);
        AnchorPane.setLeftAnchor(targetLabel, 0.0);
        AnchorPane.setTopAnchor(targetLabel, 0.0);

    }

    /**
     * Sets the cell to red or white.
     *
     * @param condition whether it is red or not.
     */
    public void setRed(boolean condition) {
        isRed = condition;
    }

    public void setCoordinates(int i, int j) {
        coordinates[0] = i;
        coordinates[1] = j;
    }

    public void setTarget(String target) {
        targetLabel.setText(target);
        //super.setTop(targetLabel);
    }

    public void setID(int id) {
    }

    public void setText(String text) {
        this.text.setText(text);
        //Checks if the show mistake mode is on and makes the cell red if it has a mistake
        if (Controller.showMistakesMode) {
            this.getCage().check();
            Controller.getTable().checkColumnOrRow(this);
        }
    }

    public boolean getIsRed() {
        return isRed;
    }

    public String getText() {
        return text.getText();
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public Label getTextLabel() {
        return text;
    }

    public void pushUndoValue(String s) {
        undoValues.push(s);
    }

    public void pushRedoValue(String s) {
        redoValues.push(s);
    }

    public String popUndoValue() {
        return undoValues.pop();
    }

    public String popRedoValue() {
        return redoValues.pop();
    }

    /**
     * Checks if a specific neighbour is part of his cage
     *
     * @param cellTable
     * @param direction
     */
    public boolean checkForNeighbour(Cell[][] cellTable, String direction) {

        int i = coordinates[0];
        int j = coordinates[1];
        int size = cellTable.length - 1;
        switch (direction) {
            case "up":
                if (j > 0 && cellTable[i][j - 1].cage != null)
                    return (cellTable[i][j - 1].cage.equals(this.cage));
                break;
            case "down":
                if (j < size && cellTable[i][j + 1].cage != null) {
                    return (cellTable[i][j + 1].cage.equals(this.cage));
                }
                break;
            case "right":
                if (i < size && cellTable[i + 1][j].cage != null)
                    return (cellTable[i + 1][j].cage.equals(this.cage));
                break;
            case "left":
                if (i > 0 && cellTable[i - 1][j].cage != null)
                    return (cellTable[i - 1][j].cage.equals(this.cage));
                break;
        }
        return false;
    }

    public void enterCage(Cage cage) {
        this.cage = cage;
    }

    public Cage getCage() {
        return cage;
    }
}


