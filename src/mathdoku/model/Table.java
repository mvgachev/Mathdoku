package mathdoku.model;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import mathdoku.view.View;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the table of the Mathdoku game.
 * Takes the form of a grid pane containing all of the cells.
 * Has a specific size and control buttons.
 */
public class Table extends GridPane {

    private int maxNum = 0;
    private int size;
    private ArrayList<Cage> allCages = new ArrayList<>();
    private Cell[][] cellTable;
    private Set<Integer> redColumns = new HashSet<>();
    private Set<Integer> redRows = new HashSet<>();
    private Button[] buttons;


    /**
     * Creates a new table by taking a file from the PC directory.
     *
     * @param file the file chosen by the user.
     */
    public Table(File file) {
        super();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                collect(line);
                line = bufferedReader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        createTable();
        createButtonVBox();
    }

    /**
     * Creates table by taking a string of the data written from the user.
     *
     * @param text the string of data written by the user.
     */
    public Table(String text) {
        super();
        String[] lines = text.split("\n");

        for (String line : lines) {
            collect(line);
        }

        createTable();
        createButtonVBox();
    }

    /**
     * Creates a table with the data that has collected.
     */
    private void createTable() {
        //Create grid pane for the visualization of the cells

        this.setAlignment(Pos.CENTER);


        //Create a two dimensional array to storage the cell objects
        size = (int) Math.sqrt(maxNum);
        cellTable = new Cell[size][size];

        //Fill in the cell table
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                Cell cell = new Cell();
                cellTable[i][j] = cell;
                cell.setCoordinates(i, j);
                cell.prefWidthProperty().bind(this.widthProperty());
                cell.prefHeightProperty().bind(this.heightProperty());
                this.add(cellTable[i][j], i, j);
            }

        //Puts the cells in their specific cages
        //Draws the borders
        for (Cage cage : allCages) {
            for (int id : cage.getIDs()) {
                //Puts in the cell with specific coordinates dependent on the id
                int[] coordinates = getCoordinatesOfID(id, size);
                int i = coordinates[0];
                int j = coordinates[1];
                cage.addCell(cellTable[i][j]);
                //Sets an ID to the cell as well
                cellTable[i][j].setID(id);
                if (id == cage.getIDs().get(0)) cellTable[i][j].setTarget(cage.getTarget());
            }
            cage.drawBorder(cellTable);
        }


        this.setGridLinesVisible(true);
        double gridCellSize = this.cellTable[0][0].getHeight();

        //Add the grid pane to the View class
        View.getInstance().getPane().setCenter(this);

    }

    /**
     * Creating buttons for the mouse control of the table.
     */
    private void createButtonVBox() {
        //Creating a vbox with numeric buttons and putting it in the pane
        HBox buttonHBox = new HBox();
        buttons = new Button[size + 1];
        for (int i = 0; i < size; i++) {
            buttons[i] = new Button(String.valueOf(i + 1));
            buttons[i].setPrefSize(80, 40);
            buttonHBox.getChildren().add(buttons[i]);
        }
        buttons[size] = new Button("Backspace");
        buttons[size].setPrefSize(80, 40);
        buttonHBox.getChildren().add(buttons[size]);
        buttonHBox.setAlignment(Pos.CENTER);
        View.getInstance().getPane().setBottom(buttonHBox);
    }


    public ArrayList<Cage> getAllCages() {
        return allCages;
    }

    public Cell[][] getCellTable() {
        return cellTable;
    }

    public Set<Integer> getRedColumns() {
        return redColumns;
    }

    public Set<Integer> getRedRows() {
        return redRows;
    }

    public int getSize() {
        return size;
    }

    public Button[] getButtons() {
        return buttons;
    }


    /**
     * Find the size of the table.
     * Create new cages with the info given.
     *
     * @param line takes the lines from the file one by one.
     */
    public void collect(String line) {
        String target = line.substring(0, line.indexOf(" "));
        String[] stringIds = line.substring(line.indexOf(" ") + 1).split(",");
        ArrayList<Integer> ids = new ArrayList<>();

        //Iterates to change the locations from string to int
        //Finds the max number to find the size
        for (int i = 0; i < stringIds.length; i++) {
            ids.add(Integer.parseInt(stringIds[i]));
            if (ids.get(i) > maxNum) maxNum = ids.get(i);
        }

        //Collects all of the cages in an array list
        allCages.add(new Cage(target, ids));
    }

    /**
     * Returns the specific coordinates of an ID.
     *
     * @param id of the cell.
     */
    public static int[] getCoordinatesOfID(int id, int size) {
        int i, j;
        int[] coordinates = new int[2];
        if (id % size == 0) {
            i = size - 1;
            j = id / size - 1;
        } else {
            i = id % size - 1;
            j = id / size;
        }
        coordinates[0] = i;
        coordinates[1] = j;
        return coordinates;
    }

    /**
     * Color all the cells in the given column or row of a table.
     *
     * @param option      column or row.
     * @param columnOrRow
     */
    public void colorColumnOrRow(String option, int columnOrRow) {
        if (option.equals("column")) {
            redColumns.add(columnOrRow);
            for (int c = 0; c < cellTable.length; c++) {
                cellTable[columnOrRow][c].setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
                cellTable[columnOrRow][c].setRed(true);
            }
        }

        if (option.equals("row")) {
            redRows.add(columnOrRow);
            for (int r = 0; r < cellTable.length; r++) {
                cellTable[r][columnOrRow].setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
                cellTable[r][columnOrRow].setRed(true);
            }
        }
    }

    /**
     * Discolors all the cells in the given column or row of a table.
     *
     * @param option      column or row.
     * @param columnOrRow
     */
    public void discolorColumnOrRow(String option, int columnOrRow) {

        if (option.equals("column")) {
            redColumns.remove(columnOrRow);
            for (int m = 0; m < cellTable.length; m++) {
                //Checks if the cell is a part of another row or cage that has a mistake
                if (!redRows.contains(m) && !cellTable[columnOrRow][m].getCage().getIsRed())
                    cellTable[columnOrRow][m].setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
                cellTable[columnOrRow][m].setRed(false);
            }
        }
        if (option.equals("row")) {
            redRows.remove(columnOrRow);
            for (int m = 0; m < cellTable.length; m++) {
                //Checks if the cell is a part of another column or cage that has a mistake
                if (!redColumns.contains(m) && !cellTable[m][columnOrRow].getCage().getIsRed())
                    cellTable[m][columnOrRow].setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
                cellTable[m][columnOrRow].setRed(false);
            }
        }
    }

    /**
     * Checks if the column or row of a cell has a value the same as the cell's.
     *
     * @param cell
     */
    public void checkColumnOrRow(Cell cell) {
        int[] coordinates = cell.getCoordinates();
        int i = coordinates[0];
        int j = coordinates[1];
        boolean columnState = true;
        boolean rowState = true;
        List<String> cellValues;

        //Checks the column of the cell for an equal numbers
        cellValues = new ArrayList<>();
        cellValues.add(cell.getText());
        for (int k = 0; k < cellTable.length; k++) {
            if (k != j && !cellTable[i][k].getText().equals(""))
                if (cellValues.contains(cellTable[i][k].getText())) {
                    colorColumnOrRow("column", i);
                    columnState = false;
                    break;
                } else cellValues.add(cellTable[i][k].getText());
        }
        if (columnState && redColumns.contains(i)) discolorColumnOrRow("column", i);

        //Checks the row of the cell for an equal number
        cellValues = new ArrayList<>();
        cellValues.add(cell.getText());
        for (int k = 0; k < cellTable.length; k++) {
            if (k != i && !cellTable[k][j].getText().equals(""))
                if (cellValues.contains(cellTable[k][j].getText())) {
                    colorColumnOrRow("row", j);
                    rowState = false;
                    break;
                } else cellValues.add(cellTable[k][j].getText());
        }
        if (rowState && redRows.contains(j)) discolorColumnOrRow("row", j);
    }
}
