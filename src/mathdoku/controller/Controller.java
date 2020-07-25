package mathdoku.controller;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mathdoku.model.Cage;
import mathdoku.model.Cell;
import mathdoku.model.Table;
import mathdoku.view.View;
import mathdoku.view.WinningAnimation;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * Represents the controller of the application.
 * Contains all the listeners and functionality of the program.
 */
public class Controller {

    private static Table table;
    private static Stack<Cell> undoCells = new Stack<>();
    private static Stack<Cell> redoCells = new Stack<>();
    public static boolean showMistakesMode = false;
    private static Solver solver;

    public static void main(String[] args) {
        Application.launch(args);
    }

    public static Table getTable() {
        return table;
    }

    /**
     * Event handler attached to the Load from PC menu item.
     * Opens a menu where the user can choose a file from the PC for the creation of the table.
     */
    public static class LoadTable implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            File file;
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
                table = new Table(file);
                //Solves the game from now
                solver = new Solver(table);
            }
        }
    }

    /**
     * Event handler attached to the show mistakes menu item.
     * Shows the mistakes the user has made by highlighting the wrong column, row or cage in red
     */
    public static class ShowMistakeMode implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {
            showMistakesMode = true;
            for (int i = 0; i < table.getCellTable().length; i++) table.checkColumnOrRow(table.getCellTable()[i][i]);
            for (Cage cage : table.getAllCages()) cage.check();
        }
    }

    /**
     * Event handler attached to all the cells
     * When the cell is clicked with the mouse it turns gray, indicating that it is selected
     */
    public static class CellSelected implements EventHandler<MouseEvent> {
        private static Cell cellSelected;
        private static final Background transparentBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
        private static final Background redBackground = new Background(new BackgroundFill(Color.RED, null, null));
        private static final Background grayBackground = new Background(new BackgroundFill(Color.GRAY, null, null));

        @Override
        public void handle(MouseEvent mouseEvent) {

            //Returns the background color of the last cell selected if there is such and is not selected
            if (cellSelected != null && !cellSelected.equals(mouseEvent.getSource())) {
                if (cellSelected.getIsRed()) cellSelected.setBackground(redBackground);
                else cellSelected.setBackground(transparentBackground);
            }

            //Saves the old background before changing it
            cellSelected = (Cell) mouseEvent.getSource();

            //Changes the current background to gray
            cellSelected.setBackground(grayBackground);

            View.getInstance().getScene().setOnKeyPressed(keyEvent -> {
                //Puts the possible keys to enter in the cell
                List<String> possibleValuesToEnter = new ArrayList<>();
                for (int i = 1; i <= table.getCellTable().length; i++) possibleValuesToEnter.add(String.valueOf(i));

                //Puts the old value in the undo stack
                undoCells.push(cellSelected);
                cellSelected.pushUndoValue(cellSelected.getText());

                //Checks if the key entered is one of them and changes the value of the cell
                String keyPressed = keyEvent.getCode().toString();      //e.g. "DIGIT1
                String digit = keyPressed.substring(keyPressed.length() - 1);   //takes last digit
                if (possibleValuesToEnter.contains(digit)) {
                    //Enables the undo and disables the redo items
                    View.getInstance().setDisableUndoItem(false);
                    View.getInstance().setDisableRedoItem(true);
                    //Make the redo stack empty
                    RedoListener.emptyRedoCells();
                    //Changes the value
                    cellSelected.setText(digit);

                    //Checks for a win
                    if (checkForWin()) new WinningAnimation();


                } else if (keyPressed.equals("BACK_SPACE")) {
                    //Disables the redo item
                    View.getInstance().setDisableRedoItem(true);
                    //Changes the value
                    cellSelected.setText("");

                    //It is still selected
                    cellSelected.setBackground(grayBackground);
                }


            });

            for (Button button : table.getButtons()) {
                button.setOnAction(actionEvent -> {

                    //Puts the old value in the undo stack
                    undoCells.push(cellSelected);
                    cellSelected.pushUndoValue(cellSelected.getText());
                    //Changes the value
                    if (button.getText().equals("Backspace")) {
                        //Disables the redo item
                        View.getInstance().setDisableRedoItem(true);
                        //Changes the value
                        cellSelected.setText("");

                        //It is still selected
                        cellSelected.setBackground(grayBackground);
                    } else {
                        //Enables the undo and disables the redo items
                        View.getInstance().setDisableUndoItem(false);
                        View.getInstance().setDisableRedoItem(true);
                        //Make the redo stack empty
                        RedoListener.emptyRedoCells();
                        cellSelected.setText(button.getText());
                    }

                    //Checks if there is a win and activates the win animation class
                    if (checkForWin()) new WinningAnimation();
                });
            }

        }

        /**
         * Checks if the user has won the game after every value entered.
         * If there are no red cells the table is solved and the user wins.
         */
        private boolean checkForWin() {

            for (int i = 0; i < table.getSize(); i++)
                for (Cell cell : table.getCellTable()[i]) {
                    if (cell.getIsRed()) return false;
                    if (cell.getText().equals("")) return false;
                }
            return true;
        }
    }

    /**
     * Event handler attached to the Clear Board menu item.
     * Clears the value of every cell, making the table in its original state.
     */
    public static class ClearBoard implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {
            displayMessageBox();
        }

        /**
         * Displays a message box that asks the user if he wants to delete all the current values entered.
         */
        private void displayMessageBox() {
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Message");
            window.setMaxWidth(100);

            Label message = new Label("Are you sure you want to clear the table? All the numbers entered will be deleted!");
            FlowPane flowPane = new FlowPane();
            flowPane.getChildren().add(message);

            Button yesButton = new Button("Yes");
            Button noButton = new Button("No");
            yesButton.setOnAction(actionEvent -> {
                window.close();
                clear();
            });
            noButton.setOnAction(actionEvent -> window.close());

            HBox buttonLayout = new HBox();
            buttonLayout.getChildren().addAll(yesButton, noButton);
            buttonLayout.setAlignment(Pos.CENTER);
            VBox layout = new VBox(10);
            layout.getChildren().addAll(flowPane, buttonLayout);
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout);
            window.setScene(scene);
            window.showAndWait();
        }

        /**
         * Clears the data of the table by setting the value of every cell to nothing.
         */
        private void clear() {
            int size = table.getCellTable().length;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    table.getCellTable()[i][j].setText("");
                    table.getCellTable()[i][j].setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
                }
            }
            //Disables and empties the redo and undo items
            UndoListener.emptyUndoCells();
            View.getInstance().setDisableUndoItem(true);
            RedoListener.emptyRedoCells();
            View.getInstance().setDisableRedoItem(true);
        }

    }

    /**
     * Event handler attached to the Create Game From Text Input menu item.
     * Displays a window where the user can write the information for his table and then create it.
     */
    public static class GameTextInput implements EventHandler<ActionEvent> {
        private int tableLength;
        private List<String> cellsInLine;

        @Override
        public void handle(ActionEvent actionEvent) {
            displayTextInput();
        }

        /**
         * Displays window to enter the data for the table.
         */
        private void displayTextInput() {
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Game Input");
            window.setMinWidth(150);

            TextArea textArea = new TextArea();
            Button createButton = new Button("Create a game");
            createButton.setOnAction(actionEvent -> {

                if (getErrorCheck(textArea.getText()).equals("true")) {
                    table = new Table(textArea.getText());
                    //Solves the game from now
                    solver = new Solver(table);
                } else displayErrorMessage(getErrorCheck(textArea.getText()));
            });

            VBox layout = new VBox();
            layout.getChildren().addAll(textArea, createButton);
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout);
            window.setScene(scene);
            window.show();
        }

        /**
         * Makes an error and format check to the data entered.
         *
         * @param text
         */
        private String getErrorCheck(String text) {
            String[] line = text.split("\n");
            List<String> cellList = new ArrayList<>();
            //Adds the possible values in a list
            List<String> possibleSignsToEnter = new ArrayList<>();
            possibleSignsToEnter.add("+");
            possibleSignsToEnter.add("-");
            possibleSignsToEnter.add("x");
            possibleSignsToEnter.add("÷");

            int tableSize = getSize(line);
            if (!isSquare(tableSize)) return "Not every cell has a cage in the table";
            tableLength = (int) Math.sqrt(tableSize);

            //Iterates through the lines
            for (int i = 0; i < line.length; i++) {

                if (!line[i].contains(" ")) return "There is no space between the target and the cells!";

                String target = line[i].split(" ")[0];
                //Takes the cells of the line
                String[] cells = line[i].split(" ")[1].split(",");

                //Checks if the size of the target is two or one -> there are multiple cells or one cell
                if (target.length() > 1 && cells.length <= 1) {
                    return "There are not enough cells to fulfill the target on line " + (i + 1);
                }
                if (target.length() == 1 && cells.length != 1) {
                    return "There should be just one cell on line " + (i + 1);
                }

                if (target.length() > 1) {
                    //Checks if there is a sign for the target
                    String sign = target.substring(target.length() - 1);
                    if (!possibleSignsToEnter.contains(sign)) return "The sign on line " + (i + 1) + " is incorrect";
                }


                cellsInLine = new ArrayList<>();
                //Checks the cells, their cages and ids
                for (String cell : cells) {

                    //Checks if there are repeated cell ids
                    if (cellList.contains(cell)) return "There is a cell in two cages in line " + (i + 1);
                    cellList.add(cell);
                    cellsInLine.add(cell);
                    int id = Integer.parseInt(cell);
                    if (id > tableSize) {
                        return "ID out of bounds on line " + (i + 1);
                    }

                }
                //Checks if the cells are near enough to make a cage
                int numberOfCellsInLine = cellsInLine.size();
                if (findAllNeighbours(cells[0]) != numberOfCellsInLine)
                    return "Cells can not make a cage in line " + (i + 1);
            }
            if (cellList.size() != tableSize) return "Not every cell has a cage";

            //If there are no mistakes return true
            return "true";
        }

        /**
         * Finds all the neighbours of a cell.
         *
         * @param cell
         */
        private int findAllNeighbours(String cell) {
            int id = Integer.parseInt(cell);
            int right = id + 1, left = id - 1;
            //Making left and right to look for id=0, which is out of the table when the cells are on rightest or leftest
            if (id == tableLength) right = 0;
            if (id == 1) left = 0;
            //Checks if a cell is not part of the list
            if (!cellsInLine.contains(cell)) return 0;
            else {
                //Removes the current cell so that the next one would not bother to check it again
                cellsInLine.remove(cell);
                //Checks up, down, left and right
                return 1 + findAllNeighbours(String.valueOf(id - tableLength)) + findAllNeighbours(String.valueOf(id + tableLength))
                        + findAllNeighbours(String.valueOf(left)) + findAllNeighbours(String.valueOf(right));
            }
        }

        /**
         * Returns the size of the table generated from the data entered.
         *
         * @param line
         */
        private int getSize(String[] line) {
            int size = 4;
            for (String s : line) {
                String[] cells = s.split(" ")[1].split(",");
                for (String cell : cells) {
                    int id = Integer.parseInt(cell);
                    if (id > size) size = id;
                }
            }
            return size;
        }

        /**
         * Displays an error message when the user has made a syntax or format error.
         *
         * @param message
         */
        private void displayErrorMessage(String message) {
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Message");
            window.setMinWidth(150);

            Label messageLabel = new Label(message);
            Button okButton = new Button("Okay");

            VBox buttonLayout = new VBox();
            buttonLayout.getChildren().addAll(okButton);
            VBox layout = new VBox(10);
            layout.getChildren().addAll(messageLabel, buttonLayout);
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout);
            window.setScene(scene);
            window.showAndWait();
        }

        /**
         * Checks if the the size of the table is a square number.
         *
         * @param size
         * @return true if it is.
         */
        private boolean isSquare(int size) {
            double sqrt = Math.sqrt(size);
            return sqrt - (int) sqrt == 0;
        }
    }

    /**
     * Event handler attached to the Undo menu item.
     * Undoes the last action taken.
     */
    public static class UndoListener implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {
            //Enables the redo item
            View.getInstance().setDisableRedoItem(false);

            //Pops the last changed cell
            Cell previousCell = undoCells.pop();

            //Pushes in the redo stacks before it is gone
            redoCells.push(previousCell);
            previousCell.pushRedoValue(previousCell.getText());

            //Pops the last changed value of the cell and sets it
            previousCell.setText(previousCell.popUndoValue());

            //Disables if there are no elements left in the stack
            if (undoCells.empty()) View.getInstance().setDisableUndoItem(true);

        }

        /**
         * Empties the undoCells list.
         */
        public static void emptyUndoCells() {
            while (!undoCells.empty()) {
                Cell poppedCell = undoCells.pop();
                poppedCell.popUndoValue();
            }
        }
    }

    /**
     * Event handler attached to the Redo menu item.
     * Redoes the last action taken.
     */
    public static class RedoListener implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {

            //Enables the redo item
            View.getInstance().setDisableUndoItem(false);

            Cell redoCell = redoCells.pop();

            //Pushes the undo back
            undoCells.push(redoCell);
            redoCell.pushUndoValue(redoCell.getText());

            redoCell.setText(redoCell.popRedoValue());

            //Disables if there are not elements left in the stack
            if (redoCells.empty()) View.getInstance().setDisableRedoItem(true);
        }

        /**
         * Empties the redoCells list.
         */
        public static void emptyRedoCells() {
            while (!redoCells.empty()) {
                Cell poppedCell = redoCells.pop();
                poppedCell.popRedoValue();
            }
        }
    }

    /**
     * Event handler attached to the Hint menu item.
     * Shows a hint to the user by displaying the first cell of the table that has a value mistaken or unwritten.
     */
    public static class HintListener implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            int id = 1;
            setSolvedCellValue(id);
        }

        /**
         * When hint is pressed sets the value of a cell to the value returned from the solver.
         *
         * @param id the id of the cell
         */
        private void setSolvedCellValue(int id) {
            int[] coordinates = Table.getCoordinatesOfID(id, table.getSize());
            int i = coordinates[0];
            int j = coordinates[1];
            Cell cell = table.getCellTable()[i][j];
            String solverCellValue = Integer.toString(solver.getSolverCellTable()[i][j].getValue());
            if (!solverCellValue.equals(cell.getText()))
                cell.setText(solverCellValue);
            else {
                id++;
                setSolvedCellValue(id);
            }
        }
    }

    /**
     * Event handler attached to the Generate Game menu item.
     * Displays a window to chose the size of the table to generate.
     * After size is selected generates a table and leaves the user to solve it.
     */
    public static class GenerateGame implements EventHandler<ActionEvent> {
        private Set<Integer> possibleValues;
        private int counter;
        private List<SolverCell> allSolverCells;


        @Override
        public void handle(ActionEvent actionEvent) {
            showGenerationOptions();
        }

        /**
         * Shows the window with the table size options.
         */
        public void showGenerationOptions() {
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Game Generator Menu");
            window.setMinWidth(350);
            window.setMinHeight(200);


            Label label = new Label("Choose table size:");
            Slider slider = new Slider();
            slider.setMin(2);
            slider.setMax(8);
            slider.setValue(3);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit(3);
            slider.setMinorTickCount(2);
            slider.setBlockIncrement(1);

            Button generateButton = new Button("Generate");
            generateButton.setOnAction(actionEvent -> {
                generateGame((int) slider.getValue());
                window.close();
            });


            VBox layout = new VBox();
            layout.getChildren().addAll(label, slider, generateButton);
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout);
            window.setScene(scene);
            window.show();
        }

        /**
         * Generates a table with a given size.
         *
         * @param size the size of the table.
         */
        private void generateGame(int size) {
            SolverCell[][] solverCellTable;
            boolean state;
            do {
                solver = new Solver(size);
                solverCellTable = solver.getSolverCellTable();
                possibleValues = new HashSet<>();

                //Creates and sets the possible values for every solver cell
                for (int i = 1; i <= size; i++) possibleValues.add(i);
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        SolverCell solverCell = new SolverCell(possibleValues);
                        //puts the solver cells in a table
                        solverCellTable[i][j] = solverCell;
                        //Sets the id for every solver cell
                        solverCell.setID(i, j, size);
                    }
                }


                state = findValue(1, solverCellTable);
            }
            //If there is a problem generating the table generate a new one
            while (!state);

            //If the table is ready with all the values in it create the player interface
            String textLoader = findCages(1, solverCellTable);
            table = new Table(textLoader);
            solver = new Solver(solverCellTable);

        }

        /**
         * Finds and creates the cages appropriate for the table.
         *
         * @param id              the id of the first cell (1)
         * @param solverCellTable the solver cell table needed to access and save the possible values of all cells
         */
        private String findCages(int id, SolverCell[][] solverCellTable) {
            int size = solverCellTable.length;
            StringBuilder textLoader = new StringBuilder();
            int[] coordinates = Table.getCoordinatesOfID(id, size);
            int i = coordinates[0];
            int j = coordinates[1];
            SolverCell solverCell = solverCellTable[i][j];

            //If the cell is not part of another cage and its id is less than the last id
            if (id <= size * size) {
                if (!solverCell.getHasCage()) {
                    Random random = new Random();
                    String target;
                    //Gets a random number for the cage size
                    int cageSize = random.nextInt(5) + 1;
                    allSolverCells = new ArrayList<>();
                    //Creates the cage and saves its ids
                    createCage(id, solverCellTable, cageSize);
                    target = getTarget(allSolverCells);
                    textLoader.append(target).append(" ");
                    for (SolverCell solverCells : allSolverCells) {
                        textLoader.append(solverCells.getID()).append(",");
                    }
                    textLoader = new StringBuilder(textLoader.substring(0, textLoader.length() - 1) + "\n");
                }
                if (id < size * size) textLoader.append(findCages(id + 1, solverCellTable));
            }
            return textLoader.toString();
        }

        /**
         * Chooses an appropriate random sign and calculates the target
         *
         * @param allSolverCells all the cells in the newly created cage
         */
        private String getTarget(List<SolverCell> allSolverCells) {
            String sign = "";
            Random random = new Random();
            int signIndicator = random.nextInt(4) + 1;
            double sum = 0, max;

            //If the cage is not consisted of only one cell find its sign
            if (allSolverCells.size() != 1) {
                switch (signIndicator) {
                    case 1:
                        sign = "+";
                        sum = 0;
                        for (SolverCell solverCells : allSolverCells) {
                            sum += solverCells.getValue();
                        }
                        break;
                    case 2:
                        sign = "-";
                        max = findMaxValue(allSolverCells);
                        sum = max;
                        for (SolverCell solverCell : allSolverCells) {
                            if (solverCell.getValue() != max) sum -= solverCell.getValue();
                        }
                        break;
                    case 3:
                        sign = "x";
                        sum = 1;
                        for (SolverCell solverCells : allSolverCells) {
                            sum *= solverCells.getValue();
                        }
                        break;
                    case 4:
                        sign = "÷";
                        max = findMaxValue(allSolverCells);
                        sum = max;
                        for (SolverCell solverCell : allSolverCells) {
                            if (solverCell.getValue() != max) sum /= solverCell.getValue();
                        }
                        break;
                }
                //If the sum is larger than 0 and a round number return it with the sign
                if (sum > 0 && sum % 1 == 0) {
                    return (int) sum + sign;
                }
                //Else try again until it finds a suitable sign
                else return getTarget(allSolverCells);
            }

            //If there is only one solver cell in the cage return it without a sign
            else return String.valueOf(allSolverCells.get(0).getValue());
        }

        /**
         * Finds the maximum value of a solver cell list
         *
         * @param allSolverCells
         */
        private int findMaxValue(List<SolverCell> allSolverCells) {
            int max = 0;
            for (SolverCell solverCell : allSolverCells) {
                if (solverCell.getValue() > max) max = solverCell.getValue();
            }
            return max;
        }

        /**
         * Creates cages from the available solver cell table by iterating over every solver cell value
         * looking if it is a part of a cell
         *
         * @param id              the id of the solver cell
         * @param solverCellTable
         * @param cageSize        the random size of the current cage
         */
        private void createCage(int id, SolverCell[][] solverCellTable, int cageSize) {
            if (allSolverCells.size() < cageSize) {
                int size = solverCellTable.length;
                int[] coordinates = Table.getCoordinatesOfID(id, size);
                int i = coordinates[0];
                int j = coordinates[1];
                SolverCell solverCell = solverCellTable[i][j];
                solverCell.setHasCage(true);
                allSolverCells.add(solverCell);
                List<Integer> possibleDirections = new ArrayList<>();

                //Go up
                if (j > 0 && !solverCellTable[i][j - 1].getHasCage())
                    possibleDirections.add(id - size);
                //Go right
                if (i < size - 1 && !solverCellTable[i + 1][j].getHasCage())
                    possibleDirections.add(id + 1);
                //Go down
                if (j < size - 1 && !solverCellTable[i][j + 1].getHasCage())
                    possibleDirections.add(id + size);
                //Go left
                if (i > 0 && !solverCellTable[i - 1][j].getHasCage())
                    possibleDirections.add(id - 1);

                //If there is a possible direction continue with the cage there
                if (!possibleDirections.isEmpty()) {
                    Random random = new Random();
                    int direction = random.nextInt(possibleDirections.size());
                    id = possibleDirections.get(direction);
                    createCage(id, solverCellTable, cageSize);
                }
                //If there is not the cage is done and the process finishes
            }
        }

        /**
         * Recursive method that gets a random number for a given solver cell
         *
         * @param id              the id of the solverCell
         * @param solverCellTable the solver cell table
         */
        private boolean findValue(int id, SolverCell[][] solverCellTable) {
            int size = solverCellTable.length;
            int[] coordinates = Table.getCoordinatesOfID(id, size);
            int i = coordinates[0];
            int j = coordinates[1];
            SolverCell solverCell = solverCellTable[i][j];
            int value;
            boolean switched;

            Set<Integer> possibleValues = solverCell.getPossibleValues();
            //If there are no possible values for a solver cell make an edit in the row of the table
            if (possibleValues.size() == 0) {
                //Sets the counter for the switch position method to 0
                counter = 0;
                switched = switchPosition(i, j, solverCellTable);
                //if there is a problem with the switch method return false and start building a new table
                if (!switched) return false;
            } else {
                //Takes a random number from the possible values and sets the solver cell with it
                Random random = new Random();
                int pos = random.nextInt(possibleValues.size());
                value = getValueOfSet(possibleValues, pos);
                solverCell.setValue(value);
                //Removes the possibilities from other solver cells in the column and row
                solver.removePosColumnRow(i, j, value, solverCellTable);
            }
            id++;
            //finds the next value recursively
            if (id <= size * size) return findValue(id, solverCellTable);
                //when the id is equal to the size of the table the process is finished and returns true
            else return true;
        }

        /**
         * Method to find reposition the numbers in a given row in order to have a possible random order
         *
         * @param i               coordinate
         * @param j               coordinate
         * @param solverCellTable the table of solverCells
         */
        private boolean switchPosition(int i, int j, SolverCell[][] solverCellTable) {

            counter++;
            //Returns false after the method has been repeated too many times
            if (counter == 50) return false;
            //Finds the number missing by finding the sum of all the possible values and then subtracting the one seen
            List<Integer> remainingPossibleValues = new ArrayList<>(possibleValues);
            for (int k = 0; k < i; k++) remainingPossibleValues.remove((Integer) solverCellTable[k][j].getValue());

            for (Integer value : remainingPossibleValues) {
                for (int k = 0; k < i; k++) {
                    if (solverCellTable[k][j].getPossibleValues().contains(value)) {
                        //Adds the possibilities back, saves the old value and changes to the new one
                        //removes the possibilities after setting the new value
                        int oldValue = solverCellTable[k][j].getValue();

                        if (checkColumn(i, j, oldValue)) {
                            solverCellTable[k][j].setValue(value);
                            addPosColumnRow(k, j, oldValue);
                            solver.removePosColumnRow(k, j, value, solverCellTable);

                            solverCellTable[i][j].setValue(oldValue);
                            solver.removePosColumnRow(i, j, oldValue, solverCellTable);
                            return true;
                        } else {
                            solverCellTable[k][j].setValue(value);
                            addPosColumnRow(k, j, oldValue);
                            solver.removePosColumnRow(k, j, value, solverCellTable);
                            solverCellTable[k][j].getPossibleValues().remove(oldValue);

                            return switchPosition(i, j, solverCellTable);
                        }
                    }
                }
            }
            return false;
        }

        private int getValueOfSet(Set<Integer> possibleValues, int pos) {
            int count = 0;
            for (Integer value : possibleValues) {
                if (count == pos) return value;
                else count++;
            }
            return 0;
        }

        /**
         * Checks if its possible to put this value in the column
         *
         * @param i        coordinate
         * @param j        coordinate
         * @param oldValue the value to put
         */
        private boolean checkColumn(int i, int j, int oldValue) {
            SolverCell[][] solverCellTable = solver.getSolverCellTable();
            for (int r = 0; r < j; r++) {
                if (solverCellTable[i][r].getValue() == oldValue) return false;
            }
            return true;
        }

        private void addPosColumnRow(int i, int j, Integer oldValue) {
            SolverCell[][] solverCellTable = solver.getSolverCellTable();
            //Adds the old value as possible for the cells under the given one
            for (int r = j + 1; r < solverCellTable.length; r++) {
                solverCellTable[i][r].getPossibleValues().add(oldValue);
            }
            //Adds the old value as possible for the cells next to the one given
            for (int c = 0; c < solverCellTable.length; c++) {
                //only if the cells above don't have it
                if (checkColumn(c, j, oldValue) && c != i) solverCellTable[c][j].getPossibleValues().add(oldValue);
            }

        }
    }

    /**
     * Event handler attached to the Font menu items.
     * Gives an option for the user to select the size of the table: small, medium, or large.
     */
    public static class FontSize implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {
            MenuItem smallSize = View.getInstance().getSmallFontItem();
            MenuItem mediumSize = View.getInstance().getMediumSizeItem();
            MenuItem largeSize = View.getInstance().getLargeSizeItem();

            if (actionEvent.getSource().equals(smallSize)) {
                setTableFont(60.00);
            }

            if (actionEvent.getSource().equals(mediumSize)) {
                setTableFont(80);
            }

            if (actionEvent.getSource().equals(largeSize)) {
                setTableFont(100);
            }

        }

        /**
         * Sets the table font by taking a specific size for the cells in it.
         *
         * @param length the length of a single cell.
         */
        private void setTableFont(double length) {
            Cell[][] cellTable = table.getCellTable();
            for (int i = 0; i < table.getSize(); i++)
                for (int j = 0; j < table.getSize(); j++) {
                    cellTable[i][j].setMaxSize(length, length);
                }
        }
    }
}
