package mathdoku.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import mathdoku.controller.Controller;
import mathdoku.model.Cell;

/**
 * Represents a winning animation.
 * Uses the same window, but different scene.
 * Uses fade transition.
 */
public class WinningAnimation {

    /**
     * Constructor that creates a winning animation
     */
    public WinningAnimation() {
        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().addAll(getNumberRotations(), getFadeTransition());
        sequentialTransition.play();
    }

    /**
     * Returns the needed fade transition
     */
    private FadeTransition getFadeTransition() {
        FadeTransition fadeTransitionOut = new FadeTransition(Duration.seconds(1));
        fadeTransitionOut.setNode(View.getInstance().getPane());
        fadeTransitionOut.setFromValue(1);
        fadeTransitionOut.setToValue(0);
        fadeTransitionOut.setOnFinished(actionEvent -> loadWinScene());

        return fadeTransitionOut;
    }

    /**
     * Loads the win scene with fade transition
     */ 
    private void loadWinScene() {
        StackPane winPane = new StackPane();
        winPane.setOpacity(0);
        //Play an intro fade transition
        FadeTransition fadeTransitionIn = new FadeTransition(Duration.seconds(1));
        fadeTransitionIn.setNode(winPane);
        fadeTransitionIn.setFromValue(0);
        fadeTransitionIn.setToValue(1);
        fadeTransitionIn.play();
        //Load the pane
        String family = "Helvetica";
        double size = 120;

        //Makes a colorful text
        TextFlow textFlow = new TextFlow();
        textFlow.setLayoutX(40);
        textFlow.setLayoutY(40);
        Text text1 = new Text("You ");
        text1.setFont(Font.font(family, size));
        text1.setFill(Color.GREEN);
        Text text2 = new Text("Win");
        text2.setFill(Color.ORANGE);
        text2.setFont(Font.font(family, FontWeight.BOLD, size));
        Text text3 = new Text(" !");
        text3.setFill(Color.RED);
        text3.setFont(Font.font(family, size));
        textFlow.getChildren().addAll(text1, text2, text3);

        //Sets the new scene in the same stage
        winPane.getChildren().add(new Group(textFlow));
        Scene winScene = new Scene(winPane, 1200, 650);
        Stage winStage = (Stage) View.getInstance().getScene().getWindow();
        winStage.setScene(winScene);
    }

    /**
     * Returns a parallel transition of rotations.
     * Rotates all of the numbers before transiting.
     */
    private ParallelTransition getNumberRotations() {
        ParallelTransition allRotations = new ParallelTransition();
        for (int i = 0; i < Controller.getTable().getSize(); i++)
            for (Cell cell : Controller.getTable().getCellTable()[i]) {
                RotateTransition transition = new RotateTransition(Duration.seconds(3), cell.getTextLabel());
                transition.setByAngle(1800);
                //transition.setCycleCount(10);
                allRotations.getChildren().add(transition);
            }
        return allRotations;
    }

}
