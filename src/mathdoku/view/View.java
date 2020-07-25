package mathdoku.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import mathdoku.controller.Controller;

/**
 * Represents the user interface of the application
 */
public class View extends Application {

    private BorderPane pane = new BorderPane();
    private Scene scene;
    private static View instance;
    private MenuItem undoItem = new MenuItem("Undo");
    private MenuItem redoItem = new MenuItem("Redo");
    private MenuItem smallSize = new MenuItem("Small");
    private MenuItem mediumSize = new MenuItem("Medium");
    private MenuItem largeSize = new MenuItem("Large");
    private MenuBar menuBar;

    @Override
    public void start(Stage stage) {
        //Saves an instance of the View class
        View.instance = this;
        stage.setTitle("Mathdoku");

        //Setting up the menu
        menuBar = new MenuBar();
        Menu edit = new Menu("Edit");
        Menu load = new Menu("New Game");
        Menu showmis = new Menu("Operations");
        Menu font = new Menu("Font");
        undoItem.setDisable(true);
        redoItem.setDisable(true);
        MenuItem clearItem = new MenuItem("Clear the board");
        MenuItem loadFromPc = new MenuItem("Load from PC");
        MenuItem loadFromTextInput = new MenuItem("Load from text input");
        MenuItem generateGame = new MenuItem("Generate a game");
        MenuItem showMistakesItem = new MenuItem("Show Mistakes");
        MenuItem hint = new MenuItem("Show Hint");
        edit.getItems().addAll(undoItem, redoItem, clearItem);
        load.getItems().addAll(loadFromPc, loadFromTextInput, generateGame);
        showmis.getItems().addAll(showMistakesItem, hint);
        font.getItems().addAll(smallSize, mediumSize, largeSize);
        menuBar.getMenus().addAll(edit, load, showmis, font);

        //Attaching the event listeners to their specific buttons or menu items
        loadFromPc.setOnAction(new Controller.LoadTable());
        showMistakesItem.setOnAction(new Controller.ShowMistakeMode());
        clearItem.setOnAction(new Controller.ClearBoard());
        loadFromTextInput.setOnAction(new Controller.GameTextInput());
        undoItem.setOnAction(new Controller.UndoListener());
        redoItem.setOnAction(new Controller.RedoListener());
        hint.setOnAction(new Controller.HintListener());
        generateGame.setOnAction(new Controller.GenerateGame());
        smallSize.setOnAction(new Controller.FontSize());
        mediumSize.setOnAction(new Controller.FontSize());
        largeSize.setOnAction(new Controller.FontSize());


        //Putting the pane in a scene and then in the window
        pane.setTop(menuBar);
        scene = new Scene(pane, 1200, 650);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public BorderPane getPane() {
        return pane;
    }

    public Scene getScene() {
        return scene;
    }

    public MenuItem getSmallFontItem() {
        return smallSize;
    }

    public MenuItem getMediumSizeItem() {
        return mediumSize;
    }

    public MenuItem getLargeSizeItem() {
        return largeSize;
    }

    public void setDisableUndoItem(boolean condition) {
        undoItem.setDisable(condition);
    }

    public void setDisableRedoItem(boolean condition) {
        redoItem.setDisable(condition);
    }


    public static View getInstance() {
        return instance;
    }
}
